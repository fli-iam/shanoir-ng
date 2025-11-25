/**
 * (The MIT license)
 *
 * Copyright (c) 2012 MIPT (mr.santak@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
