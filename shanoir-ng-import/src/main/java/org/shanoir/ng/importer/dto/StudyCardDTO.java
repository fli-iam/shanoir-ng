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

package org.shanoir.ng.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyCardDTO {

    /** Id.*/
    private Long id;

    /** The acquisition equipment. */
    private Long acquisitionEquipmentId;

    /** A studycard might be disabled */
    private boolean disabled;

    /** The name of the study card. */
    private String name;

    /** The nifti converter of the study card. */
    private Long niftiConverterId;

    /** The study for which is defined the study card. */
    private Long studyId;
    /**
     * @return the acquisitionEquipmentId
     */
    public Long getAcquisitionEquipmentId() {
        return acquisitionEquipmentId;
    }

    /**
     * @param acquisitionEquipmentId the acquisitionEquipmentId to set
     */
    public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
        this.acquisitionEquipmentId = acquisitionEquipmentId;
    }

    /**
     * @return the disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the niftiConverterId
     */
    public Long getNiftiConverterId() {
        return niftiConverterId;
    }

    /**
     * @param niftiConverterId the niftiConverterId to set
     */
    public void setNiftiConverterId(Long niftiConverterId) {
        this.niftiConverterId = niftiConverterId;
    }

    /**
     * @return the studyId
     */
    public Long getStudyId() {
        return studyId;
    }

    /**
     * @param studyId the studyId to set
     */
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
