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

package org.shanoir.uploader.model.rest;

import org.shanoir.uploader.ShUpConfig;

public class StudyCard {

    private Long id;

    /** The acquisition equipment id. */
    private Long acquisitionEquipmentId;

    /**
     * Real object, searched and set with infos from other microservice.
     */
    private AcquisitionEquipment acquisitionEquipment;

    /** The center id of the study card. */
    private Long centerId;

    /** A studycard might be disabled */
    private boolean disabled;

    /** The name of the study card. */
    private String name;

    /** The nifti converter id of the study card. */
    private Long niftiConverterId;

    /** The study for which is defined the study card. */
    private Long studyId;

    private Boolean compatible;

    public Boolean getCompatible() {
        return compatible;
    }

    public void setCompatible(Boolean compatible) {
        this.compatible = compatible;
    }

    public Long getAcquisitionEquipmentId() {
        return acquisitionEquipmentId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getName() {
        return name;
    }

    public Long getNiftiConverterId() {
        return niftiConverterId;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
        this.acquisitionEquipmentId = acquisitionEquipmentId;
    }

    public AcquisitionEquipment getAcquisitionEquipment() {
        return acquisitionEquipment;
    }

    public void setAcquisitionEquipment(AcquisitionEquipment acquisitionEquipment) {
        this.acquisitionEquipment = acquisitionEquipment;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNiftiConverterId(Long niftiConverterId) {
        this.niftiConverterId = niftiConverterId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public String toString() {
        String displayString = this.getName();
        if (acquisitionEquipment != null) {
            displayString = displayString + " (" + acquisitionEquipment.toString() + ")";
        } else {
            displayString = displayString + " (missing equipment)";
        }
        if (compatible != null && compatible) {
            return ShUpConfig.resourceBundle.getString("shanoir.uploader.import.compatible") + " " + displayString;
        } else {
            return displayString;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
