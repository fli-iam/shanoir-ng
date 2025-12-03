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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * This class is capable of writing EDF+ data structures. Changed for fixing
 * issue #3 from Github: https://github.com/MIOB/EDF4J/issues/3
 */
public final class EDFWriter {

    private EDFWriter() { }

    public static final String SHORT_DECIMAL_FORMAT = "#0.0";
    public static final String LONG_DECIMAL_FORMAT = "#0.0####";

    /**
     * Writes the EDFHeader into the OutputStream.
     *
     * @param header The header to write
     * @param outputStream The OutputStream to write into
     * @throws IOException Will be thrown if it is not possible to write into
     * the outputStream
     */
    public static void writeIntoOutputStream(EDFHeader header, OutputStream outputStream) throws IOException {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat shortFormatter = new DecimalFormat(SHORT_DECIMAL_FORMAT, dfs);
        DecimalFormat longFormatter = new DecimalFormat(LONG_DECIMAL_FORMAT, dfs);

        ByteBuffer bb = ByteBuffer.allocate(header.getBytesInHeader());
        putIntoBuffer(bb, EDFConstants.IDENTIFICATION_CODE_SIZE, header.getIdCode());
        putIntoBuffer(bb, EDFConstants.LOCAL_SUBJECT_IDENTIFICATION_SIZE, header.getSubjectID());
        putIntoBuffer(bb, EDFConstants.LOCAL_REOCRDING_IDENTIFICATION_SIZE, header.getRecordingID());
        putIntoBuffer(bb, EDFConstants.START_DATE_SIZE, header.getStartDate());
        putIntoBuffer(bb, EDFConstants.START_TIME_SIZE, header.getStartTime());
        putIntoBuffer(bb, EDFConstants.HEADER_SIZE, header.getBytesInHeader());
        putIntoBuffer(bb, EDFConstants.DATA_FORMAT_VERSION_SIZE, header.getFormatVersion());
        putIntoBuffer(bb, EDFConstants.NUMBER_OF_DATA_RECORDS_SIZE, header.getNumberOfRecords());
        putIntoBuffer(bb, EDFConstants.DURATION_DATA_RECORDS_SIZE, header.getDurationOfRecords(), longFormatter);
        putIntoBuffer(bb, EDFConstants.NUMBER_OF_CHANELS_SIZE, header.getNumberOfChannels());

        putIntoBuffer(bb, EDFConstants.LABEL_OF_CHANNEL_SIZE, header.getChannelLabels());
        putIntoBuffer(bb, EDFConstants.TRANSDUCER_TYPE_SIZE, header.getTransducerTypes());
        putIntoBuffer(bb, EDFConstants.PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, header.getDimensions());
        putIntoBuffer(bb, EDFConstants.PHYSICAL_MIN_IN_UNITS_SIZE, header.getMinInUnits(), shortFormatter);
        putIntoBuffer(bb, EDFConstants.PHYSICAL_MAX_IN_UNITS_SIZE, header.getMaxInUnits(), shortFormatter);
        putIntoBuffer(bb, EDFConstants.DIGITAL_MIN_SIZE, header.getDigitalMin());
        putIntoBuffer(bb, EDFConstants.DIGITAL_MAX_SIZE, header.getDigitalMax());
        putIntoBuffer(bb, EDFConstants.PREFILTERING_SIZE, header.getPrefilterings());
        putIntoBuffer(bb, EDFConstants.NUMBER_OF_SAMPLES_SIZE, header.getNumberOfSamples());
        putIntoBuffer(bb, header.getReserveds());

        outputStream.write(bb.array());
    }

    /**
     * Write the signals in output stream
     *
     * @param edfSignal The signals to write
     * @param header The header of EDF file
     * @param outputStream The OutputStream to write into
     * @throws IOException Will be thrown if it is not possible to write into
     * the outputStream
     */
    public static void writeIntoOutputStream(EDFSignal edfSignal, EDFHeader header, OutputStream outputStream)
            throws IOException {

        short[] data = buildDataArray(edfSignal.getDigitalValues(), header);
        writeIntoOutputStream(data, outputStream);
    }

    /**
     * Write signals data in output stream
     *
     * @param data The signals in edf short array format
     * @param outputStream The OutputStream to write into
     * @throws IOException Will be thrown if it is not possible to write into
     * the outputStream
     */
    public static void writeIntoOutputStream(short[] data, OutputStream outputStream)
            throws IOException {

        ByteBuffer bb = ByteBuffer.allocate(data.length * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        putIntoBuffer(bb, data);
        outputStream.write(bb.array());
    }

    /**
     * Convert data signals from two dimensions format ({channels} {time,
     * samples} ) to one dimension format (channels {samples for each channel}
     * grouped by time )
     *
     * @param digitalValues The signals data in two dimensions format
     * @param header The header of edf format
     * @return The signals data in one dimensions format
     */
    public static short[] buildDataArray(short[][] digitalValues, EDFHeader header) {
        int index;
        int totalDataLength = 0;
        int previousSamples = 0;
        int timeChunkLength = 0;

        // compute the total length of the new short array
        for (short[] digitalValue : digitalValues) {
            totalDataLength += digitalValue.length;
        }
        short[] signalsData = new short[totalDataLength];

        for (Integer sample : header.getNumberOfSamples()) {
            timeChunkLength += sample;
        }

        // build the signals array, which is a short one
        for (int channel = 0; channel < digitalValues.length; channel++) {
            short[] channelValues = digitalValues[channel];
            int noOfSamples = header.getNumberOfSamples()[channel];

            for (int t = 0; t < header.getNumberOfRecords(); t++) {

                for (int sample = 0; sample < noOfSamples; sample++) {
                    short shortValue = channelValues[t * noOfSamples + sample];
                    index = t * timeChunkLength + previousSamples + sample;
                    signalsData[index] = shortValue;
                }

            }
            previousSamples += noOfSamples;
        }
        return signalsData;
    }

    private static void putIntoBuffer(ByteBuffer bb, int lengthPerValue, Double[] values, DecimalFormat df) {
        for (Double value : values) {
            putIntoBuffer(bb, lengthPerValue, value, df);
        }
    }

    private static void putIntoBuffer(ByteBuffer bb, int length, Double value, DecimalFormat df) {
        if (Math.floor(value) == value) {
            putIntoBuffer(bb, length, value.intValue());
        } else {
            putIntoBuffer(bb, length, df.format(value));
        }
    }

    private static void putIntoBuffer(ByteBuffer bb, int lengthPerValue, Integer[] values) {
        for (Integer value : values) {
            putIntoBuffer(bb, lengthPerValue, value);
        }
    }

    private static void putIntoBuffer(ByteBuffer bb, int length, int value) {
        putIntoBuffer(bb, length, String.valueOf(value));
    }

    private static void putIntoBuffer(ByteBuffer bb, int lengthPerValue, String[] values) {
        for (String value : values) {
            putIntoBuffer(bb, lengthPerValue, value);
        }
    }

    private static void putIntoBuffer(ByteBuffer bb, int length, String value) {
        ByteBuffer valueBuffer = ByteBuffer.allocate(length);
        valueBuffer.put(value.getBytes(EDFConstants.CHARSET));
        while (valueBuffer.remaining() > 0) {
            valueBuffer.put(" ".getBytes());
        }

        valueBuffer.rewind();
        bb.put(valueBuffer);
    }

    private static void putIntoBuffer(ByteBuffer bb, byte[][] values) {
        for (byte[] val : values) {
            bb.put(val);
        }
    }

    private static void putIntoBuffer(ByteBuffer bb, short[] values) {
        for (short val : values) {
            bb.putShort(val);
        }
    }
}
