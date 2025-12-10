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

/**
 * This class represents the complete header of an EDF-File.
 */
public class EDFHeader {

    private String idCode = null;
    private String subjectID = null;
    private String recordingID = null;
    private String startDate = null;
    private String startTime = null;
    private int bytesInHeader = 0;
    private String formatVersion = null;
    private int numberOfRecords = 0;
    private double durationOfRecords = 0;
    private int numberOfChannels = 0;
    private String[] channelLabels = null;
    private String[] transducerTypes = null;
    private String[] dimensions = null;
    private Double[] minInUnits = null;
    private Double[] maxInUnits = null;
    private Integer[] digitalMin = null;
    private Integer[] digitalMax = null;
    private String[] prefilterings = null;
    private Integer[] numberOfSamples = null;
    private byte[][] reserveds = null;

    public String getIdCode() {
        return idCode;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public String getRecordingID() {
        return recordingID;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getBytesInHeader() {
        return bytesInHeader;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public double getDurationOfRecords() {
        return durationOfRecords;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public String[] getChannelLabels() {
        return channelLabels;
    }

    public String[] getTransducerTypes() {
        return transducerTypes;
    }

    public String[] getDimensions() {
        return dimensions;
    }

    public Double[] getMinInUnits() {
        return minInUnits;
    }

    public Double[] getMaxInUnits() {
        return maxInUnits;
    }

    public Integer[] getDigitalMin() {
        return digitalMin;
    }

    public Integer[] getDigitalMax() {
        return digitalMax;
    }

    public String[] getPrefilterings() {
        return prefilterings;
    }

    public Integer[] getNumberOfSamples() {
        return numberOfSamples;
    }

    public byte[][] getReserveds() {
        return reserveds;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public void setRecordingID(String recordingID) {
        this.recordingID = recordingID;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setBytesInHeader(int bytesInHeader) {
        this.bytesInHeader = bytesInHeader;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public void setDurationOfRecords(double durationOfRecords) {
        this.durationOfRecords = durationOfRecords;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public void setChannelLabels(String[] channelLabels) {
        this.channelLabels = channelLabels;
    }

    public void setTransducerTypes(String[] transducerTypes) {
        this.transducerTypes = transducerTypes;
    }

    public void setDimensions(String[] dimensions) {
        this.dimensions = dimensions;
    }

    public void setMinInUnits(Double[] minInUnits) {
        this.minInUnits = minInUnits;
    }

    public void setMaxInUnits(Double[] maxInUnits) {
        this.maxInUnits = maxInUnits;
    }

    public void setDigitalMin(Integer[] digitalMin) {
        this.digitalMin = digitalMin;
    }

    public void setDigitalMax(Integer[] digitalMax) {
        this.digitalMax = digitalMax;
    }

    public void setPrefilterings(String[] prefilterings) {
        this.prefilterings = prefilterings;
    }

    public void setNumberOfSamples(Integer[] numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public void setReserveds(byte[][] reserveds) {
        this.reserveds = reserveds;
    }



}
