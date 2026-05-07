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

package org.shanoir.ng.studycard.model.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.condition.ExamMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.ExamMetadataCondOnDatasets;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMConditionOnDatasets;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class QualityCardRule extends AbstractEntity {

    private Integer tag;

    @NotNull
    private boolean orConditions;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // there is a join table because a rule_id fk would lead to an ambiguity and
    // bugs
    // because it could refer to a study card or quality card rule
    @JoinTable(name = "quality_card_condition_join", joinColumns = {
            @JoinColumn(name = "quality_card_rule_id") }, inverseJoinColumns = { @JoinColumn(name = "condition_id") })
    private List<StudyCardCondition> conditions;

    public QualityTag getQualityTag() {
        return QualityTag.get(tag);
    }

    public void setQualityTag(QualityTag tag) {
        this.tag = tag != null ? tag.getId() : null;
    }

    public List<StudyCardCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<StudyCardCondition> conditions) {
        this.conditions = conditions;
    }

    public boolean isOrConditions() {
        return orConditions;
    }

    public void setOrConditions(boolean orConditions) {
        this.orConditions = orConditions;
    }

    public void apply(DatasetAcquisition datasetAcquisition, QualityCardResult result, WADODownloaderService downloader)
            throws PacsException {
        apply(datasetAcquisition, null, result, downloader);
    }

    /**
     *
     * @param examinationDicomAttributes if null conditions will be checked on the
     *                                   examination data and dicom data will be
     *                                   fetched from pacs.
     *                                   Else conditions will be checked on the
     *                                   looping on the given dicom attributes
     * @param examination
     * @param result
     * @param downloader
     * @throws PacsException
     */

    public void apply(DatasetAcquisition datasetAcquisition, AcquisitionAttributes<?> acquisitionDicomAttributes,
            QualityCardResult result, WADODownloaderService downloader) throws PacsException {
        // if applied at import and not from ShUp then acquisitionDicomAttributes should
        // not be null, otherwise we fetch DICOM acquisition attributes.
        if (acquisitionDicomAttributes == null) {
            acquisitionDicomAttributes = downloader.getDicomAttributesForAcquisition(datasetAcquisition);
        }
        // In case a rule was added without condition (= set as Always in gui)
        if (this.getConditions() == null || this.getConditions().isEmpty()) {
            QualityCardResultEntry resultEntry = initResult(datasetAcquisition);
            resultEntry.setTagSet(getQualityTag());
            resultEntry.setMessage(
                    "Tag " + getQualityTag().name() + " was set by the quality card rule without any condition.");
            result.add(resultEntry);
            result.addUpdatedDatasetAcquisition(setTagToDatasetAcquisition(datasetAcquisition));
        } else {
            ConditionResult conditionResult = conditionsfulfilled(acquisitionDicomAttributes, datasetAcquisition,
                    result);
            if (conditionResult.isFulfilled()) {
                result.addUpdatedDatasetAcquisition(setTagToDatasetAcquisition(datasetAcquisition));
            }
            // if conditions not fulfilled for a VALID tag
            // or if conditions fulfilled for a ERROR or WARNING tag
            // then add an entry to the report
            if ((conditionResult.isFulfilled() && !getQualityTag().equals(QualityTag.VALID))
                    || (!conditionResult.isFulfilled() && getQualityTag().equals(QualityTag.VALID))) {
                QualityCardResultEntry resultEntry = initResult(datasetAcquisition);
                resultEntry.setFailedValid(QualityTag.VALID.equals(getQualityTag()) && !conditionResult.isFulfilled());
                resultEntry.setTagSet(getQualityTag());
                if (conditionResult.isFulfilled()) {
                    resultEntry.setMessage(
                            "Tag " + getQualityTag().name() + " was set because those conditions were fulfilled : "
                                    + StringUtils.join(conditionResult.getFulfilledConditionsMsgList(), ", "));
                } else {
                    resultEntry.setMessage(
                            "Tag " + getQualityTag().name() + " could not be set because those conditions failed : "
                                    + StringUtils.join(conditionResult.getUnfulfilledConditionsMsgList(), ", "));
                }
                result.add(resultEntry);
            }
        }
    }

    /**
     *
     * @param datasetAcquisitionId
     * @return an updated dataset acquisition
     */
    private DatasetAcquisition setTagToDatasetAcquisition(DatasetAcquisition datasetAcquisition) {
        //DatasetAcquisition datasetAcquisitionCopy = new GenericDatasetAcquisition();
        //datasetAcquisitionCopy.setId(datasetAcquisitionId);
        //datasetAcquisitionCopy.setQualityTag(getQualityTag());
        datasetAcquisition.setQualityTag(getQualityTag());
        return datasetAcquisition;
    }

    /**
     *
     * @param dicomAttributes if null conditions will be checked on the examination
     *                        data and dicom data will be fetched from pacs.
     *                        Else conditions will be checked on the looping on the
     *                        given dicom attributes
     * @param examination
     * @param result
     * @return
     */
    private ConditionResult conditionsfulfilled(AcquisitionAttributes<?> dicomAttributes, DatasetAcquisition da,
            QualityCardResult result) {
        boolean allFulfilled = true;
        ConditionResult condResult = new ConditionResult();
        Collections.sort(conditions, new ConditionComparator()); // sort by level
        // We create a list containing only the acquisition we want to apply the rule
        // on, to use the same condition fulfillment methods as for study card
        // conditions on datasets or acquisitions list.
        List<DatasetAcquisition> acquisitionList = new ArrayList<>();
        acquisitionList.add(da);

        for (StudyCardCondition condition : getConditions()) {
            StringBuffer msg = new StringBuffer();
            boolean fulfilled = true;
            if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                fulfilled = ((StudyCardDICOMConditionOnDatasets) condition).fulfilled(dicomAttributes, msg);
            } else if (condition instanceof ExamMetadataCondOnAcq) {
                fulfilled = ((ExamMetadataCondOnAcq) condition).fulfilled(acquisitionList, msg);
            } else if (condition instanceof ExamMetadataCondOnDatasets) {
                fulfilled = ((ExamMetadataCondOnDatasets) condition).fulfilled(acquisitionList, msg);
            } else {
                throw new IllegalStateException(
                        "There might be an unimplemented condition type here. Condition class : "
                                + condition.getClass());
            }

            if (fulfilled) {
                condResult.addFulfilledConditionsMsg(msg.toString());
            } else {
                condResult.addUnfulfilledConditionsMsg(msg.toString());
            }

            if (isOrConditions() && fulfilled) {
                allFulfilled = true;
                break;
            } else {
                allFulfilled &= fulfilled;
            }
        }
        condResult.setFulfilled(allFulfilled);
        return condResult;
    }

    private QualityCardResultEntry initResult(DatasetAcquisition datasetAcquisition) {
        QualityCardResultEntry result = new QualityCardResultEntry();
        result.setSubjectName(datasetAcquisition.getExamination().getSubject() != null ? datasetAcquisition.getExamination().getSubject().getName() : null);
        result.setDatasetAcquisitionId(datasetAcquisition.getId());
        result.setExaminationDate(datasetAcquisition.getExamination().getExaminationDate());
        result.setExaminationComment(datasetAcquisition.getExamination().getComment());
        return result;
    }

    public class ConditionComparator implements Comparator<StudyCardCondition> {
        @Override
        public int compare(StudyCardCondition cond1, StudyCardCondition cond2) {
            return priority(cond1) - priority(cond2);
        }

        /**
         * the higher the priority, the higher is the returned number.
         */
        private int priority(StudyCardCondition condition) {
            if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                return 1;
            } else if (condition instanceof ExamMetadataCondOnAcq) {
                return 3;
            } else if (condition instanceof ExamMetadataCondOnDatasets) {
                return 2;
            } else {
                return 0;
            }
        }
    }

    public class ConditionResult {

        private boolean fulfilled;
        private List<String> fulfilledConditionsMsgList = new ArrayList<>();
        private List<String> unfulfilledConditionsMsgList = new ArrayList<>();

        public boolean isFulfilled() {
            return fulfilled;
        }

        public void setFulfilled(boolean fulfilled) {
            this.fulfilled = fulfilled;
        }

        public List<String> getFulfilledConditionsMsgList() {
            return fulfilledConditionsMsgList;
        }

        public void addFulfilledConditionsMsg(String msg) {
            this.fulfilledConditionsMsgList.add(msg);
        }

        public List<String> getUnfulfilledConditionsMsgList() {
            return unfulfilledConditionsMsgList;
        }

        public void addUnfulfilledConditionsMsg(String msg) {
            this.unfulfilledConditionsMsgList.add(msg);
        }
    }

    public boolean hasDicomConditions() {
        if (getConditions() != null) {
            for (StudyCardCondition condition : getConditions()) {
                if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                    return true;
                }
            }
        }
        return false;
    }
}
