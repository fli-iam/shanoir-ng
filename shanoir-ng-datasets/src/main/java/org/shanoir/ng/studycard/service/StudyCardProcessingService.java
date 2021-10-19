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

package org.shanoir.ng.studycard.service;

import java.math.BigDecimal;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.shanoir.ng.studycard.model.Operation;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardAssignment;
import org.shanoir.ng.studycard.model.StudyCardCondition;
import org.shanoir.ng.studycard.model.StudyCardRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StudyCardProcessingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyCardProcessingService.class);

	
	public void applyStudyCard(DatasetAcquisition acquisition, StudyCard studyCard, Attributes dicomAttributes) {
		if (studyCard.getRules() != null) {
			for (StudyCardRule rule : studyCard.getRules()) {
				applyStudyCardRule(acquisition, rule, dicomAttributes);
			}
		}
		acquisition.setStudyCard(studyCard);
		acquisition.setStudyCardTimestamp(studyCard.getLastEditTimestamp());
	}

	
	private void applyStudyCardRule(DatasetAcquisition acquisition, StudyCardRule rule, Attributes dicomAttributes) {
		if (rule.getConditions() == null || rule.getConditions().isEmpty() || conditionsFullfiled(rule.getConditions(), dicomAttributes)) {
			if (rule.getAssignments() != null) applyAssignments(acquisition, rule.getAssignments());
		}
	}


	private boolean conditionsFullfiled(List<StudyCardCondition> conditions, Attributes dicomAttributes) {
		for (StudyCardCondition condition : conditions) {
			if (!conditionFullfiled(condition, dicomAttributes)) return false;
		}
		return true;
	}


	private boolean conditionFullfiled(StudyCardCondition condition, Attributes dicomAttributes) {
		VR tagVr = StandardElementDictionary.INSTANCE.vrOf(condition.getDicomTag());
		DicomTagType tagType = DicomTagType.valueOf(tagVr);
		if (condition.getDicomValue() == null) throw new IllegalArgumentException("A condition value cannot be null");
		
		if (tagType.isNumerical()) {
			if (!condition.getOperation().isNumerical()) {
				throw new IllegalArgumentException("Study card processing : operation " + condition.getOperation() + " is not compatible with dicom tag " 
						+ condition.getDicomTag() + " of type " + tagType + "(condition id : " + condition.getId() + ")");
			}
			
			BigDecimal scValue = new BigDecimal(condition.getDicomValue());
			Integer comparison = null;
			
			if (DicomTagType.Float.equals(tagType)) {
				Float dicomValue = dicomAttributes.getFloat(condition.getDicomTag(), Float.MIN_VALUE);			
				comparison = BigDecimal.valueOf(dicomValue).compareTo(scValue);
			}
			
			// There is no dicomAttributes.getLong() !
			else if (DicomTagType.Double.equals(tagType) || DicomTagType.Long.equals(tagType)) {
				Double dicomValue = dicomAttributes.getDouble(condition.getDicomTag(), Double.MIN_VALUE);			
				comparison = BigDecimal.valueOf(dicomValue).compareTo(scValue);
			}
			
			else if (DicomTagType.Integer.equals(tagType)) {
				Integer dicomValue = dicomAttributes.getInt(condition.getDicomTag(), Integer.MIN_VALUE);
				comparison = BigDecimal.valueOf(dicomValue).compareTo(scValue);
			}
			
			return comparison != null && numericalCompare(condition.getOperation(), comparison);
		}
		
		else if (tagType.isTextual()) {
			if (!condition.getOperation().isTextual()) {
				throw new IllegalArgumentException("Study card processing : operation " + condition.getOperation() + " is not compatible with dicom tag " 
						+ condition.getDicomTag() + " of type " + tagType + "(condition id : " + condition.getId() + ")");
			}
			
			String dicomValue = dicomAttributes.getString(condition.getDicomTag());
			if (dicomValue == null) {
				LOG.warn("Could not find a value in the dicom for the tag " + condition.getDicomTag());
				return false;
			}
			
			return textualCompare(condition.getOperation(), dicomValue, condition.getDicomValue());
			
		}
		return false;
	}
	
	
	private boolean numericalCompare(Operation operation, int comparison) {
		if (Operation.BIGGER_THAN.equals(operation)) {
			return comparison >= 1;
		} else if (Operation.EQUALS.equals(operation)) {
			return comparison == 0;
		} else if (Operation.SMALLER_THAN.equals(operation)) {
			return comparison <= 1;
		}
		throw new IllegalArgumentException("Cannot use this method for non-numerical operations (" + operation + ")");
	}
	
	
	private boolean textualCompare(Operation operation, String dicomStr, String studycardStr) {
		if (Operation.EQUALS.equals(operation)) {
			return dicomStr.equals(studycardStr);
		}
		else if (Operation.CONTAINS.equals(operation)) {
			return dicomStr.contains(studycardStr);
		}
		else if (Operation.STARTS_WITH.equals(operation)) {
			return dicomStr.startsWith(studycardStr);
		}
		else if (Operation.ENDS_WITH.equals(operation)) {
			return dicomStr.endsWith(studycardStr);
		}
		throw new IllegalArgumentException("Cannot use this method for non-textual operations (" + operation + ")");
	}
	
	
	private void applyAssignments(DatasetAcquisition acquisition, List<StudyCardAssignment> assignments) {
		for (StudyCardAssignment assignment : assignments) {
			applyAssignment(acquisition, assignment);
		}
	}


	private void applyAssignment(DatasetAcquisition acquisition, StudyCardAssignment assignment) {
		try {
			assignment.getField().update(acquisition, assignment.getValue());			
		} catch (IllegalArgumentException e) {
			LOG.error("Error in studycard processing : ", e);
		}
	}
	
}
