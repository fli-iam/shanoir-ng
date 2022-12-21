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
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

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

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class QualityExaminationRule extends AbstractEntity {

	private Integer tag;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="rule_id")
	private List<StudyCardCondition> conditions;

	@NotNull
	private int type; // examination, acquisition, dataset
	
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public void apply(Examination examination, Attributes examinationDicomAttributes, QualityCardResult result) {
	    ExaminationData examData = convert(examination);
	    apply(examData, examinationDicomAttributes, result);
    }

    public void apply(ExaminationData examination, Attributes examinationDicomAttributes, QualityCardResult result) {
        if (this.getConditions() == null || this.getConditions().isEmpty() 
                || conditionsfulfilled(examinationDicomAttributes, examination, result)) {
            result.addUpdatedSubjectStudies( 
                    setTagToSubjectStudies(examination.getSubjectStudies()));
        }
    }
    
    /**
     * 
     * @param subjectStudies
     * @return list of updated subject studies, ignoring those which tag is unchanged
     */
    private List<SubjectStudy> setTagToSubjectStudies(List<SubjectStudy> subjectStudies) {
        if (subjectStudies == null) throw new IllegalArgumentException("subjectStudies can't be null");
        List<SubjectStudy> updatedList = new ArrayList<>();
        for (SubjectStudy subjectStudy : subjectStudies) {
            if (!getQualityTag().equals(subjectStudy.getQualityTag())) {
                subjectStudy.setQualityTag(getQualityTag());
                updatedList.add(subjectStudy);
            }
        }
        return updatedList;
    }

    @SuppressWarnings("unused")
    private boolean conditionsfulfilled(Attributes dicomAttributes, ExaminationData examination, QualityCardResult result) {
        boolean fulfilled = true;
        Collections.sort(conditions, new ConditionComparator()); // sort by level
        for (StudyCardCondition condition : getConditions()) {
            String errorMsg = null;
            if (condition instanceof StudyCardDICOMCondition) {
                fulfilled &= ((StudyCardDICOMCondition) condition).fulfilled(dicomAttributes, errorMsg);
            } else if (condition instanceof ExaminationMetadataConditionOnAcquisitions) {
                fulfilled &= ((ExaminationMetadataConditionOnAcquisitions) condition).fulfilled(examination.getDatasetAcquisitions(), errorMsg);
            } else if (condition instanceof ExaminationMetadataConditionOnDatasets) {
                fulfilled &= ((ExaminationMetadataConditionOnDatasets) condition).fulfilled(examination.getDatasetAcquisitions(), errorMsg);
            } else {
                throw new IllegalStateException("There might be an unimplemented condition type here. Condition class : " + condition.getClass());
            }
            if (!fulfilled) {
                if (errorMsg != null && result != null) {
                    QualityCardResultEntry resultEntry = initResult(examination);
                    resultEntry.setMessage(errorMsg);
                    result.add(resultEntry);
                }
                break; // don't go further if one condition is false
            }
        }
       return fulfilled;
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
        examData.setSubjectStudies(examination.getStudy().getSubjectStudyList());
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
}
