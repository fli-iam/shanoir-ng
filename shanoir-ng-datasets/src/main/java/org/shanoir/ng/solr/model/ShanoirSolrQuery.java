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

package org.shanoir.ng.solr.model;

import org.shanoir.ng.shared.paging.FacetPageable;
import org.shanoir.ng.utils.Range;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yyao
 *
 */
public class ShanoirSolrQuery {

    private Collection<String> studyName;

    private Collection<Long> studyId;

    private Collection<String> subjectName;

    private Collection<String> subjectType;

    private Collection<Long> subjectId;

    private Collection<Integer> sortingIndex;

    private Collection<Long> examinationId;

    private Collection<String> examinationComment;

    private Collection<String> acquisitionEquipmentName;

    private    Collection<String> datasetName;

    private    Collection<String> centerName;

    private Collection<Long> centerId;

    private    Collection<String> tags;

    private LocalDate datasetStartDate;

    private LocalDate datasetEndDate;

    private LocalDate importStartDate;

    private LocalDate importEndDate;

    private    Collection<String> datasetType;

    private Collection<String> datasetNature;

    private String searchText;

    private boolean expertMode;

    private Range<Float> sliceThickness;

    private Range<Float> pixelBandwidth;

    private Range<Float> magneticFieldStrength;

    private Collection<Boolean> processed;

    private Collection<String> username;

    private Map<String, FacetPageable> facetPaging;

    /**
     * @return the studyName
     */
    public Collection<String> getStudyName() {
        return studyName;
    }

    /**
     * @param studyName the studyName to set
     */
    public void setStudyName(Collection<String> studyName) {
        this.studyName = studyName;
    }

    /**
     * @return the subjectName
     */
    public Collection<String> getSubjectName() {
        return subjectName;
    }

    /**
     * @param subjectName the subjectName to set
     */
    public void setSubjectName(Collection<String> subjectName) {
        this.subjectName = subjectName;
    }

    public Collection<String> getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(Collection<String> subjectType) {
        this.subjectType = subjectType;
    }

    public Collection<Long> getExaminationId() {
        return examinationId;
    }

    public void setExaminationId(Collection<Long> examinationId) {
        this.examinationId = examinationId;
    }

    /**
     * @return the examinationComment
     */
    public Collection<String> getExaminationComment() {
        return examinationComment;
    }

    /**
     * @param examinationComment the examinationComment to set
     */
    public void setExaminationComment(Collection<String> examinationComment) {
        this.examinationComment = examinationComment;
    }

    public Collection<String> getAcquisitionEquipmentName() {
        return acquisitionEquipmentName;
    }

    public void setAcquisitionEquipmentName(Collection<String> acquisitionEquipmentName) {
        this.acquisitionEquipmentName = acquisitionEquipmentName;
    }

    /**
     * @return the datasetName
     */
    public Collection<String> getDatasetName() {
        return datasetName;
    }

    /**
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(Collection<String> datasetName) {
        this.datasetName = datasetName;
    }

    public Collection<String> getCenterName() {
        return centerName;
    }

    public void setCenterName(Collection<String> centerName) {
        this.centerName = centerName;
    }

    /**
     * @return the studyId
     */
    public Collection<Long> getStudyId() {
        return studyId;
    }

    /**
     * @param studyId the studyId to set
     */
    public void setStudyId(Collection<Long> studyId) {
        this.studyId = studyId;
    }

    /**
     * @return the datasetStartDate
     */
    public LocalDate getDatasetStartDate() {
        return datasetStartDate;
    }

    /**
     * @param datasetStartDate the datasetStartDate to set
     */
    public void setDatasetStartDate(LocalDate datasetStartDate) {
        this.datasetStartDate = datasetStartDate;
    }

    /**
     * @return the datasetEndDate
     */
    public LocalDate getDatasetEndDate() {
        return datasetEndDate;
    }

    /**
     * @param datasetEndDate the datasetEndDate to set
     */
    public void setDatasetEndDate(LocalDate datasetEndDate) {
        this.datasetEndDate = datasetEndDate;
    }

    public LocalDate getImportStartDate() {
        return importStartDate;
    }

    public void setImportStartDate(LocalDate importStartDate) {
        this.importStartDate = importStartDate;
    }

    public LocalDate getImportEndDate() {
        return importEndDate;
    }

    public void setImportEndDate(LocalDate importEndDate) {
        this.importEndDate = importEndDate;
    }

    /**
     * @return the datasetType
     */
    public Collection<String> getDatasetType() {
        return datasetType;
    }

    /**
     * @param datasetType the datasetType to set
     */
    public void setDatasetType(Collection<String> datasetType) {
        this.datasetType = datasetType;
    }

    /**
     * @return the datasetNature
     */
    public Collection<String> getDatasetNature() {
        return datasetNature;
    }

    /**
     * @param datasetNature the datasetNature to set
     */
    public void setDatasetNature(Collection<String> datasetNature) {
        this.datasetNature = datasetNature;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public boolean isExpertMode() {
        return expertMode;
    }

    public boolean getExpertMode() {
        return isExpertMode();
    }

    public void setExpertMode(boolean expertMode) {
        this.expertMode = expertMode;
    }

    public Range<Float> getSliceThickness() {
        return sliceThickness;
    }

    public void setSliceThickness(Range<Float> sliceThickness) {
        this.sliceThickness = sliceThickness;
    }

    public Range<Float> getPixelBandwidth() {
        return pixelBandwidth;
    }

    public void setPixelBandwidth(Range<Float> pixelBandwidth) {
        this.pixelBandwidth = pixelBandwidth;
    }

    public Range<Float> getMagneticFieldStrength() {
        return magneticFieldStrength;
    }

    public void setMagneticFieldStrength(Range<Float> magneticFieldStrength) {
        this.magneticFieldStrength = magneticFieldStrength;
    }

    /**
     * @return the tags
     */
    public Collection<String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public Map<String, FacetPageable> getFacetPaging() {
        return facetPaging;
    }

    public void setFacetPaging(Map<String, FacetPageable> facetPaging) {
        this.facetPaging = facetPaging;
    }

    public Range<LocalDate> getDatasetDateRange() {
        return new Range<LocalDate>(getDatasetStartDate(), getDatasetEndDate());
    }
    public Range<LocalDate> getImportDateRange() {
        return new Range<LocalDate>(getImportStartDate(), getImportEndDate());
    }

    public Collection<Long> getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Collection<Long> subjectId) {
        this.subjectId = subjectId;
    }

    public Collection<Integer> getSortingIndex() {
        return sortingIndex;
    }

    public void setSortingIndex(Collection<Integer> sortingIndex) {
        this.sortingIndex = sortingIndex;
    }

    public Collection<Long> getCenterId() {
        return centerId;
    }

    public void setCenterId(Collection<Long> centerId) {
        this.centerId = centerId;
    }

    public Collection<Boolean> getProcessed() {
        return processed;
    }

    public void setProcessed(Collection<Boolean> processed) {
        this.processed = processed;
    }

    public Collection<String> getUsername() {
        return username;
    }

    public void setUsername(Collection<String> username) {
        this.username = username;
    }
}
