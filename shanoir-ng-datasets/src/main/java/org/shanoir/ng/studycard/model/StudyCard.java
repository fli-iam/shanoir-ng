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

package org.shanoir.ng.studycard.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.dcm4che3.data.Attributes;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.studycard.model.rule.DatasetAcquisitionRule;
import org.shanoir.ng.studycard.model.rule.DatasetRule;
import org.shanoir.ng.studycard.model.rule.StudyCardRule;

import java.util.List;

/**
 * Study card.
 *
 * @author msimon
 *
 */
@SuppressWarnings("deprecation")
@Entity
@Table(name = "study_cards")
@JsonPropertyOrder({ "_links", "id", "name", "isDisabled" })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyCard extends HalEntity implements Card {

    /**
     * UID
     */
    private static final long serialVersionUID = 1751168445500120935L;

    /** The acquisition equipment. */
    private Long acquisitionEquipmentId;

    /** A studycard might be disabled */
    private boolean disabled;

    /** The name of the study card. */
    @NotBlank
    @Column(unique = true)
    @Unique
    private String name;

    /** The nifti converter of the study card. */
    private Long niftiConverterId;

    /** The study for which is defined the study card. */
    private Long studyId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "study_card_id")
    private List<StudyCardRule<?>> rules;

    private Long lastEditTimestamp;

    /**
     * Init HATEOAS links
     */
    @PostLoad
    public void initLinks() {
        this.addLink(Links.REL_SELF, "studycard/" + getId());
    }

    public Long getAcquisitionEquipmentId() {
        return acquisitionEquipmentId;
    }

    public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
        this.acquisitionEquipmentId = acquisitionEquipmentId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNiftiConverterId() {
        return niftiConverterId;
    }

    public void setNiftiConverterId(Long niftiConverterId) {
        this.niftiConverterId = niftiConverterId;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public List<StudyCardRule<?>> getRules() {
        return rules;
    }

    public void setRules(List<StudyCardRule<?>> rules) {
        this.rules = rules;
    }

    public Long getLastEditTimestamp() {
        return lastEditTimestamp;
    }

    public void setLastEditTimestamp(Long lastEditTimestamp) {
        this.lastEditTimestamp = lastEditTimestamp;
    }

    /**
    * Application during import, when dicoms are present in tmp directory.
    * @param acquisition
    * @param dicomAttributes
    * @return true if the application had any effect on acquisitions
    */
    public boolean apply(DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes) {
        boolean changeInAtLeastOneAcquisition = false;
        if (this.getRules() != null) {
            for (StudyCardRule<?> rule : this.getRules()) {
                if (rule instanceof DatasetAcquisitionRule) {
                    changeInAtLeastOneAcquisition = true;
                    ((DatasetAcquisitionRule) rule).apply(acquisition, dicomAttributes);
                } else if (rule instanceof DatasetRule && acquisition.getDatasets() != null) {
                    for (Dataset dataset : acquisition.getDatasets()) {
                        changeInAtLeastOneAcquisition = true;
                        Attributes attributes;
                        if (String.class.equals(dicomAttributes.getParametrizedType())) {
                            // @SuppressWarnings("unchecked") doesn't work ...
                            attributes = ((AcquisitionAttributes<String>)dicomAttributes).getDatasetAttributes(dataset.getSOPInstanceUID());
                        } else if (Long.class.equals(dicomAttributes.getParametrizedType())) {
                            attributes = ((AcquisitionAttributes<Long>)dicomAttributes).getDatasetAttributes(dataset.getId());
                        } else {
                            throw new IllegalStateException("the parametrized type of AcquisitionAttributes is not implemented, use String or Long");
                        }
                        ((DatasetRule) rule).apply(dataset, attributes);
                    }
                } else {
                    throw new IllegalStateException("unknown type of rule");
                }
            }
        }
        acquisition.setStudyCard(this);
        acquisition.setStudyCardTimestamp(this.getLastEditTimestamp());
        return changeInAtLeastOneAcquisition;
    }

}
