package org.shanoir.ng.dicom.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataDTO {
    
    @JsonProperty("SOPInstanceUID")
    private String sopInstanceUID;
    
    @JsonProperty("SeriesInstanceUID")
    private String seriesInstanceUID;
    
    @JsonProperty("SeriesDate")
    private String seriesDate;

    @JsonProperty("StudyInstanceUID")
    private String studyInstanceUID;

    @JsonProperty("InstanceNumber")
    private Integer instanceNumber;

    @JsonProperty("SOPClassUID")
    private String sopClassUID;
    
    @JsonProperty("Modality")
    private String modality;
    
    @JsonProperty("Columns")
    private Integer columns;
    
    @JsonProperty("Rows")
    private Integer rows;
    
    @JsonProperty("FrameOfReferenceUID")
    private String frameOfReferenceUID;

    @JsonProperty("PhotometricInterpretation")
    private String photometricInterpretation;
    
    @JsonProperty("BitsAllocated")
    private Integer bitsAllocated;
    
    @JsonProperty("BitsStored")
    private Integer bitsStored;

    @JsonProperty("PixelRepresentation")
    private Integer pixelRepresentation;
    
    @JsonProperty("SamplesPerPixel")
    private Integer samplesPerPixel;
    
    @JsonProperty("PixelSpacing")
    private List<Float> pixelSpacing;
        
    @JsonProperty("HighBit")
    private Integer highBit;
    
    @JsonProperty("ImageOrientationPatient")
    private List<Integer> imageOrientationPatient;
    
    @JsonProperty("ImagePositionPatient")
    private List<Integer> imagePositionPatient;
    
    @JsonProperty("ImageType")
    private List<String> imageType;
    
    @JsonProperty("WindowCenter")
    private Integer windowCenter;
    
    @JsonProperty("WindowWidth")
    private Integer windowWidth;

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public String getSeriesDate() {
        return seriesDate;
    }

    public void setSeriesDate(String seriesDate) {
        this.seriesDate = seriesDate;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public Integer getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(Integer instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public void setSopClassUID(String sopClassUID) {
        this.sopClassUID = sopClassUID;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getFrameOfReferenceUID() {
        return frameOfReferenceUID;
    }

    public void setFrameOfReferenceUID(String frameOfReferenceUID) {
        this.frameOfReferenceUID = frameOfReferenceUID;
    }

    public String getPhotometricInterpretation() {
        return photometricInterpretation;
    }

    public void setPhotometricInterpretation(String photometricInterpretation) {
        this.photometricInterpretation = photometricInterpretation;
    }

    public Integer getBitsAllocated() {
        return bitsAllocated;
    }

    public void setBitsAllocated(Integer bitsAllocated) {
        this.bitsAllocated = bitsAllocated;
    }

    public Integer getBitsStored() {
        return bitsStored;
    }

    public void setBitsStored(Integer bitsStored) {
        this.bitsStored = bitsStored;
    }

    public Integer getPixelRepresentation() {
        return pixelRepresentation;
    }

    public void setPixelRepresentation(Integer pixelRepresentation) {
        this.pixelRepresentation = pixelRepresentation;
    }

    public Integer getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public void setSamplesPerPixel(Integer samplesPerPixel) {
        this.samplesPerPixel = samplesPerPixel;
    }

    public List<Float> getPixelSpacing() {
        return pixelSpacing;
    }

    public void setPixelSpacing(List<Float> pixelSpacing) {
        this.pixelSpacing = pixelSpacing;
    }

    public Integer getHighBit() {
        return highBit;
    }

    public void setHighBit(Integer highBit) {
        this.highBit = highBit;
    }

    public List<Integer> getImageOrientationPatient() {
        return imageOrientationPatient;
    }

    public void setImageOrientationPatient(List<Integer> imageOrientationPatient) {
        this.imageOrientationPatient = imageOrientationPatient;
    }

    public List<Integer> getImagePositionPatient() {
        return imagePositionPatient;
    }

    public void setImagePositionPatient(List<Integer> imagePositionPatient) {
        this.imagePositionPatient = imagePositionPatient;
    }

    public List<String> getImageType() {
        return imageType;
    }

    public void setImageType(List<String> imageType) {
        this.imageType = imageType;
    }

    public Integer getWindowCenter() {
        return windowCenter;
    }

    public void setWindowCenter(Integer windowCenter) {
        this.windowCenter = windowCenter;
    }

    public Integer getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(Integer windowWidth) {
        this.windowWidth = windowWidth;
    }

}
