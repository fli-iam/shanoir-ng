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


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


/**
 * This builder is capable of building an EDFHeader for an EDF+ file
 * which will contain annotations.
 *
 * The annotations has to be available in an array according to the EDF+ specification.
 * Changed for issue #3 from Github: https://github.com/MIOB/EDF4J/issues/3
 */
public class EDFAnnotationFileHeaderBuilder {

    private String recordingId;
    private String recordingStartDate;
    private String startDate;
    private String startTime;
    private double durationOfRecord;
    private Integer numberOfChannels;
    private Integer numberOfRecords;

    private String patientCode = "X";
    private String patientSex = "X";
    private String patientBirthdate = "X";
    private String patientName = "X";
    private String recordingHospital = "X";
    private String recordingTechnician = "X";
    private String recordingEquipment = "X";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");
    private final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH.mm.ss");

    private String[] channelLabels;
    private String[] transducerTypes;
    private String[] dimensions;
    private Double[] minInUnits;
    private Double[] maxInUnits;
    private Integer[] digitalMin;
    private Integer[] digitalMax;
    private String[] prefilterings;
    private Integer[] numberOfSamples;
    private byte[][] reserveds;

    public EDFAnnotationFileHeaderBuilder recordingId(String recordingId) {
        if (recordingId == null) {
            throw new IllegalArgumentException("Invalid statement: recordingId is null");
        }
        this.recordingId = nonSpaceString(recordingId);
        return this;
    }

    public EDFAnnotationFileHeaderBuilder startOfRecording(Date startOfRecording) {
        if (startOfRecording == null) {
            throw new IllegalArgumentException("Invalid statement: startOfRecording is null");
        }
        recordingStartDate = new SimpleDateFormat("dd-MMM-yyyy").format(startOfRecording).toUpperCase();
        startDate = simpleDateFormat.format(startOfRecording);
        startTime = simpleTimeFormat.format(startOfRecording);
        return this;
    }

    public EDFAnnotationFileHeaderBuilder durationOfRecord(double val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Invalid statement: durationOfRecord is <= 0");
        }
        durationOfRecord = val;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder patientCode(String val) {
        if (val == null) {
            throw new IllegalArgumentException("Invalid statement: patientCode is null");
        }
        patientCode = nonSpaceString(val);
        return this;
    }

    public EDFAnnotationFileHeaderBuilder patientIsMale(boolean val) {
        patientSex = val ? "M" : "F";
        return this;
    }

    public EDFAnnotationFileHeaderBuilder patientBirthdate(Date birthdate) {
        if (birthdate == null) {
            throw new IllegalArgumentException("Invalid statement: birthdate is null");
        }
        patientBirthdate = new SimpleDateFormat("dd-MMM-yyyy").format(birthdate).toUpperCase();
        return this;
    }

    public EDFAnnotationFileHeaderBuilder patientName(String val) {
        if (val == null) {
            throw new IllegalArgumentException("Invalid statement: patientName is null");
        }
        patientName = nonSpaceString(val);
        return this;
    }

    public EDFAnnotationFileHeaderBuilder recordingHospital(String val) {
        if (val == null) {
            throw new IllegalArgumentException("Invalid statement: recordingHospital is null");
        }
        recordingHospital = nonSpaceString(val);
        return this;
    }

    public EDFAnnotationFileHeaderBuilder recordingTechnician(String val) {
        if (val == null) {
            throw new IllegalArgumentException("Invalid statement: recordingTechnician is null");
        }
        recordingTechnician = nonSpaceString(val);
        return this;
    }

    public EDFAnnotationFileHeaderBuilder recordingEquipment(String val) {
        if (val == null) {
            throw new IllegalArgumentException("Invalid statement: recordingEquipment is null");
        }
        recordingEquipment = nonSpaceString(val);
        return this;
    }

    public void numberOfChannels(int val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Invalid statement: numberOfChannels is <= 0");
        }
        numberOfChannels = val;
    }

    public void numberOfRecords(int val) {
        if (val <= 0) {
            throw new IllegalArgumentException("Invalid statement: numberOfRecords is <= 0");
        }
        numberOfRecords = val;
    }

    public EDFAnnotationFileHeaderBuilder channelLabels(String[] channelLabels) {
        this.channelLabels = channelLabels;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder transducerTypes(String[] transducerTypes) {
        this.transducerTypes = transducerTypes;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder dimensions(String[] dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder minInUnits(Double[] minInUnits) {
        this.minInUnits = minInUnits;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder maxInUnits(Double[] maxInUnits) {
        this.maxInUnits = maxInUnits;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder digitalMin(Integer[] digitalMin) {
        this.digitalMin = digitalMin;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder digitalMax(Integer[] digitalMax) {
        this.digitalMax = digitalMax;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder prefilterings(String[] prefilterings) {
        this.prefilterings = prefilterings;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder numberOfSamples(Integer[] numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
        return this;
    }

    public EDFAnnotationFileHeaderBuilder reserveds(byte[][] reserveds) {
        this.reserveds = reserveds;
        return this;
    }

    private String nonSpaceString(String val) {
        return val.replaceAll(" ", "_");
    }

    public EDFHeader build() {
        assert recordingStartDate != null;
        assert startDate != null;
        assert startTime != null;
        assert durationOfRecord > 0;

        EDFHeader header = new EDFHeader();
        header.setIdCode(createStringWithSpaces(String.valueOf(0), EDFConstants.IDENTIFICATION_CODE_SIZE));
        header.setSubjectID(createStringWithSpaces(buildPatientString(), EDFConstants.LOCAL_SUBJECT_IDENTIFICATION_SIZE));
        header.setRecordingID(recordingId != null ? recordingId : createStringWithSpaces(buildRecordingString(), EDFConstants.LOCAL_REOCRDING_IDENTIFICATION_SIZE));

        header.setStartDate(startDate != null ? startDate : simpleDateFormat.format(new Date()));
        header.setStartDate(appendSpacesToString(header.getStartDate(), EDFConstants.START_DATE_SIZE - header.getStartDate().length()));

        header.setStartTime(startTime != null ? startTime : simpleTimeFormat.format(new Date()));
        header.setStartTime(appendSpacesToString(header.getStartTime(), EDFConstants.START_TIME_SIZE - header.getStartTime().length()));

        header.setFormatVersion(createStringWithSpaces("", EDFConstants.DATA_FORMAT_VERSION_SIZE));
        header.setNumberOfRecords(numberOfRecords != null ? numberOfRecords : 1);
        header.setDurationOfRecords(durationOfRecord);
        header.setNumberOfChannels(numberOfChannels != null ? numberOfChannels : 1);
        header.setBytesInHeader(EDFConstants.HEADER_SIZE_RECORDING_INFO
                + header.getNumberOfChannels() * EDFConstants.HEADER_SIZE_PER_CHANNEL);

        header.setChannelLabels(channelLabels);
        header.setTransducerTypes(transducerTypes);
        header.setDimensions(dimensions);
        header.setMinInUnits(minInUnits);
        header.setMaxInUnits(maxInUnits);
        header.setDigitalMin(digitalMin);
        header.setDigitalMax(digitalMax);
        header.setPrefilterings(prefilterings);
        header.setNumberOfSamples(numberOfSamples);
        header.setReserveds(reserveds);

        return header;
    }

    private String createStringWithSpaces(String root, int totalSize) {
        return appendSpacesToString(root, totalSize - root.length());
    }

    private String appendSpacesToString(String original, int times) {

        char[] repeat = new char[times];
        Arrays.fill(repeat, ' ');
        return original + new String(repeat);
    }

    private String buildPatientString() {
        return patientCode + " " + patientSex + " " + patientBirthdate + " " + patientName;
    }

    private String buildRecordingString() {
        return "Startdate" + " " + recordingStartDate + " " + recordingHospital + " " + recordingTechnician
                + " " + recordingEquipment;
    }
}
