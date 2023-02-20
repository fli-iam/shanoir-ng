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
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.condition.ExaminationMetadataConditionOnAcquisitions;
import org.shanoir.ng.studycard.model.condition.ExaminationMetadataConditionOnDatasets;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class QualityExaminationRule extends AbstractEntity {

	private Integer tag;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	// there is a join table because a rule_id fk would lead to an ambiguity and bugs 
	// because it could refer to a study card or quality card rule
	@JoinTable(name="quality_card_condition_join", joinColumns = {@JoinColumn(name = "quality_card_rule_id")}, inverseJoinColumns = {@JoinColumn(name = "condition_id")})
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
	
	public void apply(Examination examination, Attributes examinationDicomAttributes, QualityCardResult result) {
	    ExaminationData examData = convert(examination);
	    if (examData.getSubjectStudy() == null) {
	        Logger log = LoggerFactory.getLogger(QualityExaminationRule.class);
	        log.warn("No subject study in exam " + examination.getId());
	    } else {
	        apply(examData, examinationDicomAttributes, result);	        
	    }
    }

    public void apply(ExaminationData examination, Attributes examinationDicomAttributes, QualityCardResult result) {
        if (this.getConditions() == null || this.getConditions().isEmpty()) {
            result.addUpdatedSubjectStudy( 
                    setTagToSubjectStudy(examination.getSubjectStudy()));
        } else {
            ConditionResult conditionResult = conditionsfulfilled(examinationDicomAttributes, examination, result);
            if (conditionResult.isFulfilled()) {
                result.addUpdatedSubjectStudy( 
                        setTagToSubjectStudy(examination.getSubjectStudy()));
            }            
            // if conditions not fulfilled for a VALID tag
            // or if conditions fulfilled for a ERROR or WARNING tag
            // then add an entry to the report
            if ((conditionResult.isFulfilled() && !getQualityTag().equals(QualityTag.VALID))
                    || (!conditionResult.isFulfilled() && getQualityTag().equals(QualityTag.VALID))) {
                QualityCardResultEntry resultEntry = initResult(examination);
                if (conditionResult.isFulfilled()) {
                    resultEntry.setMessage("Tag " + getQualityTag().name() + " was set because those conditions were fulfilled : " + StringUtils.join(conditionResult.getFulfilledConditionsMsgList(), ", "));                   
                } else {
                    resultEntry.setMessage("Tag " + getQualityTag().name() + " could not be set because those conditions failed : " + StringUtils.join(conditionResult.getUnfulfilledConditionsMsgList(), ", "));
                }
                result.add(resultEntry);
            }
        }
    }            
          
    
    /**
     * 
     * @param subjectStudies
     * @return list of updated subject studies
     */
    private SubjectStudy setTagToSubjectStudy(SubjectStudy subjectStudy) {
        // don't touch subjectStudy as we later will compare the original with the updated
        SubjectStudy subjectStudyCopy = new SubjectStudy();
        subjectStudyCopy.setId(subjectStudy.getId());
        subjectStudyCopy.setQualityTag(getQualityTag());
        return subjectStudyCopy;
    }

    private ConditionResult conditionsfulfilled(Attributes dicomAttributes, ExaminationData examination, QualityCardResult result) {
        boolean allFulfilled = true;
        ConditionResult condResult = new ConditionResult();
        Collections.sort(conditions, new ConditionComparator()); // sort by level
        for (StudyCardCondition condition : getConditions()) {
            StringBuffer msg = new StringBuffer();
            boolean fulfilled = true;
            if (condition instanceof StudyCardDICOMCondition) {
                fulfilled = ((StudyCardDICOMCondition) condition).fulfilled(dicomAttributes, msg);
            } else if (condition instanceof ExaminationMetadataConditionOnAcquisitions) {
                fulfilled = ((ExaminationMetadataConditionOnAcquisitions) condition).fulfilled(examination.getDatasetAcquisitions(), msg);
            } else if (condition instanceof ExaminationMetadataConditionOnDatasets) {
                fulfilled = ((ExaminationMetadataConditionOnDatasets) condition).fulfilled(examination.getDatasetAcquisitions(), msg);
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }
            if (fulfilled) {
                condResult.addFulfilledConditionsMsg(msg.toString());
            } else {
                condResult.addUnfulfilledConditionsMsg(msg.toString());
            }
            allFulfilled &= fulfilled;
        }
        condResult.setFulfilled(allFulfilled);
        return condResult;
    }
    
    private QualityCardResultEntry initResult(ExaminationData examination) {
        QualityCardResultEntry result = new QualityCardResultEntry();
        result.setSubjectName(examination.getSubjectName());
        result.setExaminationDate(examination.getExaminationDate());
        result.setExaminationComment(examination.getExaminationComment());
        return result;
    }
    
    private ExaminationData convert(Examination examination) {
        // Keep only MR acquisitions
        if (examination == null) throw new IllegalArgumentException("examination can't be null");
        if (examination.getDatasetAcquisitions() == null) throw new IllegalArgumentException("examination acquisitions can't be null");
        if (examination.getStudy() == null) throw new IllegalArgumentException("study can't be null");
        if (examination.getStudy().getSubjectStudyList() == null) throw new IllegalArgumentException("subjectStudyList can't be null");
        List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions().stream().filter(a -> a instanceof MrDatasetAcquisition).collect(Collectors.toList());
        ExaminationData examData = new ExaminationData();
        examData.setDatasetAcquisitions(acquisitions);
        examData.setExaminationComment(examination.getComment());
        examData.setExaminationDate(examination.getExaminationDate());
        examData.setSubjectName(examination.getSubject().getName());
        examData.setSubjectStudy(
                examination.getSubject().getSubjectStudyList().stream()
                    .filter(ss -> ss.getStudy().getId().equals(examination.getStudy().getId()))
                    .findFirst().orElse(null));
        return examData;
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
            if (condition instanceof StudyCardDICOMCondition) {
                return 1;
            } else if (condition instanceof ExaminationMetadataConditionOnAcquisitions) {
                return 3;
            } else if (condition instanceof ExaminationMetadataConditionOnDatasets) {
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
}
