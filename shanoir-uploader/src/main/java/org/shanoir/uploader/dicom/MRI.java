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
