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

package org.shanoir.ng.dataset.dto;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.tag.model.StudyTagDTO;

import java.time.LocalDate;
import java.util.List;


/**
 * DTO for dataset.
 *
 * @author msimon
 *
 */
public class DatasetDTO {

    @LocalDateAnnotations
    private LocalDate creationDate;

    private Long groupOfSubjectsId;

    private Long id;

    private DatasetMetadataDTO originMetadata;

    private Long studyId;

    private Long subjectId;
    
    private Long centerId;

    private DatasetMetadataDTO updatedMetadata;
    
    private String name;
    
    private String type;

    private boolean inPacs;
    
    private List<StudyTagDTO> tags;

    private Long source;

    private List<Long> copies;


    /**
     * @return the creationDate
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the groupOfSubjectsId
     */
    public Long getGroupOfSubjectsId() {
        return groupOfSubjectsId;
    }

    /**
     * @param groupOfSubjectsId
     *            the groupOfSubjectsId to set
     */
    public void setGroupOfSubjectsId(Long groupOfSubjectsId) {
        this.groupOfSubjectsId = groupOfSubjectsId;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the originMetadata
     */
    public DatasetMetadataDTO getOriginMetadata() {
        return originMetadata;
    }

    /**
     * @param originMetadata
     *            the originMetadata to set
     */
    public void setOriginMetadata(DatasetMetadataDTO originMetadata) {
        this.originMetadata = originMetadata;
    }

    /**
     * @return the studyId
     */
    public Long getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            the studyId to set
     */
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    /**
     * @return the subjectId
     */
    public Long getSubjectId() {
        return subjectId;
    }

    /**
     * @param subjectId
     *            the subjectId to set
     */
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * @return the updatedMetadata
     */
    public DatasetMetadataDTO getUpdatedMetadata() {
        return updatedMetadata;
    }

    /**
     * @param updatedMetadata
     *            the updatedMetadata to set
     */
    public void setUpdatedMetadata(DatasetMetadataDTO updatedMetadata) {
        this.updatedMetadata = updatedMetadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public boolean isInPacs() {
        return inPacs;
    }

    public void setInPacs(boolean inPacs) {
        this.inPacs = inPacs;
    }

    public List<StudyTagDTO> getTags() {
        return tags;
    }

    public void setTags(List<StudyTagDTO> tags) {
        this.tags = tags;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    public List<Long> getCopies() {
        return copies;
    }

    public void setCopies(List<Long> copies) {
        this.copies = copies;
    }
}
