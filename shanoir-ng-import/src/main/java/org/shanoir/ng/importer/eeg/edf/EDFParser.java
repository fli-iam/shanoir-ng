/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.eeg.edf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is an EDFParser which is capable of parsing files in the formats EDF and
 * EDF+.
 *
 * For information about EDF or EDF+ see http://www.edfplus.info/
 */
public final class EDFParser {

    private EDFParser() { }

    /**
     * Parse the InputStream which should be at the start of an EDF-File. The
     * method returns an object containing the complete content of the EDF-File.
     *
     * @param is the InputStream to the EDF-File
     * @return the parsed result
     * @throws EDFParserException if there is an error during parsing
     */
    public static EDFParserResult parseEDF(InputStream is) throws EDFParserException {
        EDFParserResult result = parseHeader(is);
        parseSignal(is, result);

        return result;
    }

    /**
     * Parse the InputStream which should be at the start of an EDF-File. The
     * method returns an object containing the complete header of the EDF-File
     *
     * @param is the InputStream to the EDF-File
     * @return the parsed result
     * @throws EDFParserException if there is an error during parsing
     */
    public static EDFParserResult parseHeader(InputStream is) throws EDFParserException {
        try {
            EDFHeader header = new EDFHeader();
            EDFParserResult result = new EDFParserResult();
            result.setHeader(header);

            header.setIdCode(ParseUtils.readASCIIFromStream(is, EDFConstants.IDENTIFICATION_CODE_SIZE));
            if (!header.getIdCode().trim().equals("0")) {
                throw new EDFParserException();
            }
            header.setSubjectID(ParseUtils.readASCIIFromStream(is, EDFConstants.LOCAL_SUBJECT_IDENTIFICATION_SIZE));
            header.setRecordingID(ParseUtils.readASCIIFromStream(is, EDFConstants.LOCAL_REOCRDING_IDENTIFICATION_SIZE));
            header.setStartDate(ParseUtils.readASCIIFromStream(is, EDFConstants.START_DATE_SIZE));
            header.setStartTime(ParseUtils.readASCIIFromStream(is, EDFConstants.START_TIME_SIZE));
            header.setBytesInHeader(Integer.parseInt(ParseUtils.readASCIIFromStream(is, EDFConstants.HEADER_SIZE).trim()));
            header.setFormatVersion(ParseUtils.readASCIIFromStream(is, EDFConstants.DATA_FORMAT_VERSION_SIZE));
            header.setNumberOfRecords(Integer.parseInt(
                    ParseUtils.readASCIIFromStream(is, EDFConstants.NUMBER_OF_DATA_RECORDS_SIZE).trim()));
            header.setDurationOfRecords(Double.parseDouble(
                    ParseUtils.readASCIIFromStream(is, EDFConstants.DURATION_DATA_RECORDS_SIZE).trim()));
            header.setNumberOfChannels(Integer.parseInt(
                    ParseUtils.readASCIIFromStream(is, EDFConstants.NUMBER_OF_CHANELS_SIZE).trim()));

            parseChannelInformation(is, result);

            return result;
        } catch (IOException e) {
            throw new EDFParserException(e);
        }
    }

    /**
     * Parse only data EDF file. This method should be invoked only after
     * parseHeader method. It will be populated in result parameter.
     *
     * @param is stream with EDF file.
     * @param result results from {parseHeader(is) parseHeader} method
     * @throws EDFParserException throws if parser don't recognized EDF (EDF+)
     * format in stream.
     */
    private static void parseSignal(InputStream is, EDFParserResult result) throws EDFParserException {
        try {
            EDFSignal signal = new EDFSignal();
            EDFHeader header = result.getHeader();

            signal.setUnitsInDigit(new Double[header.getNumberOfChannels()]);
            for (int i = 0; i < signal.getUnitsInDigit().length; i++) {
                signal.getUnitsInDigit()[i] = (header.getMaxInUnits()[i] - header.getMinInUnits()[i])
                        / (header.getDigitalMax()[i] - header.getDigitalMin()[i]);
            }

            signal.setDigitalValues(new short[header.getNumberOfChannels()][]);
            signal.setValuesInUnits(new double[header.getNumberOfChannels()][]);
            for (int i = 0; i < header.getNumberOfChannels(); i++) {
                signal.getDigitalValues()[i] = new short[header.getNumberOfRecords() * header.getNumberOfSamples()[i]];
                signal.getValuesInUnits()[i] = new double[header.getNumberOfRecords() * header.getNumberOfSamples()[i]];
            }

            int samplesPerRecord = 0;
            for (int nos : header.getNumberOfSamples()) {
                samplesPerRecord += nos;
            }

            try (ReadableByteChannel ch = Channels.newChannel(is)) {
                ByteBuffer bytebuf = ByteBuffer.allocate(samplesPerRecord * 2);
                bytebuf.order(ByteOrder.LITTLE_ENDIAN);

                for (int i = 0; i < header.getNumberOfRecords(); i++) {
                    bytebuf.rewind();
                    ch.read(bytebuf);
                    bytebuf.rewind();
                    for (int j = 0; j < header.getNumberOfChannels(); j++) {
                        for (int k = 0; k < header.getNumberOfSamples()[j]; k++) {
                            int s = header.getNumberOfSamples()[j] * i + k;
                            signal.getDigitalValues()[j][s] = bytebuf.getShort();
                            signal.getValuesInUnits()[j][s] = signal.getDigitalValues()[j][s] * signal.getUnitsInDigit()[j];
                        }
                    }
                }

                result.setAnnotations(parseAnnotation(header, signal));

                result.setSignal(signal);
            }
        } catch (IOException e) {
            throw new EDFParserException(e);
        }
    }

    private static List<EDFAnnotation> parseAnnotation(EDFHeader header, EDFSignal signal) {

        if (!header.getFormatVersion().startsWith("EDF+")) {
            return Collections.emptyList();
        }

        int annotationIndex = -1;
        for (int i = 0; i < header.getNumberOfChannels(); i++) {
            if ("EDF Annotations".equals(header.getChannelLabels()[i].trim())) {
                annotationIndex = i;
                break;
            }
        }
        if (annotationIndex == -1) {
            return Collections.emptyList();
        }

        short[] s = signal.getDigitalValues()[annotationIndex];
        byte[] b = new byte[s.length * 2];
        for (int i = 0; i < s.length * 2; i += 2) {
            b[i] = (byte) (s[i / 2] % 256);
            b[i + 1] = (byte) (s[i / 2] / 256 % 256);
        }

        removeAnnotationSignal(header, signal, annotationIndex);

        return parseAnnotations(b);

    }

    private static List<EDFAnnotation> parseAnnotations(byte[] b) {
        List<EDFAnnotation> annotations = new ArrayList<>();
        int onSetIndex = 0;
        int durationIndex = -1;
        int annotationIndex = -2;
        int endIndex = -3;
        for (int i = 0; i < b.length - 1; i++) {
            boolean dontKeepOnGoing = false;
            if (b[i] == 21) {
                durationIndex = i;
                dontKeepOnGoing = true;
            } else if (b[i] == 20 && onSetIndex > annotationIndex) {
                annotationIndex = i;
                dontKeepOnGoing = true;
            } else if (b[i] == 20 && b[i + 1] == 0) {
                endIndex = i;
                dontKeepOnGoing = true;
            }
            if (dontKeepOnGoing) {
                continue;
            }
            if (b[i] != 0 && onSetIndex < endIndex) {

                String onSet;
                String duration;
                if (durationIndex > onSetIndex) {
                    onSet = new String(b, onSetIndex, durationIndex - onSetIndex);
                    duration = new String(b, durationIndex, annotationIndex - durationIndex);
                } else {
                    onSet = new String(b, onSetIndex, annotationIndex - onSetIndex);
                    duration = "";
                }
                String annotation = new String(b, annotationIndex, endIndex - annotationIndex);
                annotations.add(new EDFAnnotation(onSet, duration, annotation.split("[\u0014]")));
                onSetIndex = i;
            }
        }
        return annotations;
    }

    private static void removeAnnotationSignal(EDFHeader header, EDFSignal signal, int annotationIndex) {
        header.setNumberOfChannels(header.getNumberOfChannels() - 1);
        ParseUtils.removeElement(header.getChannelLabels(), annotationIndex);
        ParseUtils.removeElement(header.getTransducerTypes(), annotationIndex);
        ParseUtils.removeElement(header.getDimensions(), annotationIndex);
        ParseUtils.removeElement(header.getMinInUnits(), annotationIndex);
        ParseUtils.removeElement(header.getMaxInUnits(), annotationIndex);
        ParseUtils.removeElement(header.getDigitalMin(), annotationIndex);
        ParseUtils.removeElement(header.getDigitalMax(), annotationIndex);
        ParseUtils.removeElement(header.getPrefilterings(), annotationIndex);
        ParseUtils.removeElement(header.getNumberOfSamples(), annotationIndex);
        ParseUtils.removeElement(header.getReserveds(), annotationIndex);

        ParseUtils.removeElement(signal.getDigitalValues(), annotationIndex);
        ParseUtils.removeElement(signal.getUnitsInDigit(), annotationIndex);
        ParseUtils.removeElement(signal.getValuesInUnits(), annotationIndex);
    }

    private static void parseChannelInformation(InputStream is, EDFParserResult result) throws EDFParserException {
        try {
            EDFHeader header = result.getHeader();
            int numberOfChannels = header.getNumberOfChannels();
            header.setChannelLabels(ParseUtils
                    .readBulkASCIIFromStream(is, EDFConstants.LABEL_OF_CHANNEL_SIZE, numberOfChannels));
            header.setTransducerTypes(ParseUtils
                    .readBulkASCIIFromStream(is, EDFConstants.TRANSDUCER_TYPE_SIZE, numberOfChannels));
            header.setDimensions(ParseUtils
                    .readBulkASCIIFromStream(is, EDFConstants.PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, numberOfChannels));
            header.setMinInUnits(ParseUtils
                    .readBulkDoubleFromStream(is, EDFConstants.PHYSICAL_MIN_IN_UNITS_SIZE, numberOfChannels));
            header.setMaxInUnits(ParseUtils
                    .readBulkDoubleFromStream(is, EDFConstants.PHYSICAL_MAX_IN_UNITS_SIZE, numberOfChannels));
            header.setDigitalMin(ParseUtils.readBulkIntFromStream(is, EDFConstants.DIGITAL_MIN_SIZE, numberOfChannels));
            header.setDigitalMax(ParseUtils.readBulkIntFromStream(is, EDFConstants.DIGITAL_MAX_SIZE, numberOfChannels));
            header.setPrefilterings(ParseUtils
                    .readBulkASCIIFromStream(is, EDFConstants.PREFILTERING_SIZE, numberOfChannels));
            header.setNumberOfSamples(ParseUtils
                    .readBulkIntFromStream(is, EDFConstants.NUMBER_OF_SAMPLES_SIZE, numberOfChannels));
            header.setReserveds(new byte[numberOfChannels][]);
            for (int i = 0; i < header.getReserveds().length; i++) {
                header.getReserveds()[i] = new byte[EDFConstants.RESERVED_SIZE];
                is.read(header.getReserveds()[i]);
            }
        } catch (IOException e) {
            throw new EDFParserException(e);
        }
    }
}
