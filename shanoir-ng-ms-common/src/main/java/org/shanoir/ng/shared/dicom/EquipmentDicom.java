package org.shanoir.ng.shared.dicom;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class EquipmentDicom {

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty("manufacturerModelName")
    private String manufacturerModelName;

    @JsonProperty("deviceSerialNumber")
    private String deviceSerialNumber;

    @JsonProperty("stationName")
    private String stationName;

    @JsonProperty("magneticFieldStrength")
    private String magneticFieldStrength;

    @JsonProperty("modality")
    private String modality;

    // Keep this empty constructor to avoid Jackson deserialization exceptions
    public EquipmentDicom() { }

    public EquipmentDicom(String manufacturer, String manufacturerModelName, String modality, String deviceSerialNumber, String stationName, String magneticFieldStrength) {
        this.manufacturer = manufacturer;
        this.manufacturerModelName = manufacturerModelName;
        this.modality = modality;
        this.deviceSerialNumber = deviceSerialNumber;
        this.stationName = stationName;
        this.magneticFieldStrength = magneticFieldStrength;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturerModelName() {
        return manufacturerModelName;
    }

    public void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getMagneticFieldStrength() {
        return magneticFieldStrength;
    }

    public void setMagneticFieldStrength(String magneticFieldStrength) {
        this.magneticFieldStrength = magneticFieldStrength;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    @JsonIgnore
    public boolean isComplete() {
        return StringUtils.isNotEmpty(this.manufacturer)
            && StringUtils.isNotEmpty(this.manufacturerModelName)
            && StringUtils.isNotEmpty(this.deviceSerialNumber)
            && StringUtils.isNotEmpty(this.modality);
    }

    @Override
    public String toString() {
        return "EquipmentDicom [manufacturer=" + manufacturer + ", manufacturerModelname = " + manufacturerModelName
                + ", deviceSerialNumber=" + deviceSerialNumber + ", stationname = " + stationName
                + ", modality=" + modality
                + ", magneticFieldStrength=" + magneticFieldStrength + "]";
    }

}
