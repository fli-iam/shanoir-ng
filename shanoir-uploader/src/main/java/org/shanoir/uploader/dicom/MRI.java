package org.shanoir.uploader.dicom;

import jakarta.xml.bind.annotation.XmlType;

/**
 * The Class MRI contains information about the MRI Acquisition.
 *
 * @author ifakhfakh
 */
@XmlType
public class MRI {

    private String manufacturer;
    private String stationName;
    private String magneticFieldStrength;
    private String deviceSerialNumber;
    private String manufacturersModelName;
    private String institutionName;
    private String institutionAddress;

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getManufacturersModelName() {
        return manufacturersModelName;
    }

    public void setManufacturersModelName(String manufacturersModelName) {
        this.manufacturersModelName = manufacturersModelName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getInstitutionAddress() {
        return institutionAddress;
    }

    public void setInstitutionAddress(String institutionAddress) {
        this.institutionAddress = institutionAddress;
    }

    public String getMagneticFieldStrength() {
        return magneticFieldStrength;
    }

    public void setMagneticFieldStrength(String magneticFieldStrength) {
        this.magneticFieldStrength = magneticFieldStrength;
    }

    @Override
    public String toString() {
        return "MRI [manufacturer=" + manufacturer + ", stationname = " + stationName + ", magneticFieldStrength="
                + magneticFieldStrength + ", deviceSerialNumber=" + deviceSerialNumber + ", manufacturersModelname = "
                + manufacturersModelName + ", institutionname = " + institutionName + ", institutionAddress="
                + institutionAddress + "]";
    }

}
