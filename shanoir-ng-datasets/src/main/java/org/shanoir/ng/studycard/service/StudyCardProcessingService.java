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

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.mail.MessagingException;

import org.apache.commons.collections4.CollectionUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONReader;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.studycard.model.DicomTagType;
import org.shanoir.ng.studycard.model.Field;
import org.shanoir.ng.studycard.model.Operation;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardApply;
import org.shanoir.ng.studycard.model.StudyCardAssignment;
import org.shanoir.ng.studycard.model.StudyCardCondition;
import org.shanoir.ng.studycard.model.StudyCardConditionValue;
import org.shanoir.ng.studycard.model.StudyCardRule;
import org.shanoir.ng.studycard.model.StudyCardRuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class StudyCardProcessingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyCardProcessingService.class);
	
	@Autowired
	private StudyCardService studyCardService;
	
	@Autowired
	private ExaminationService examinationService;
	
	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;
	
	@Autowired
	private WADODownloaderService downloader;

	public void applyStudyCard(DatasetAcquisition acquisition, StudyCard studyCard, Attributes dicomAttributes) {
		if (studyCard.getRules() != null) {
			for (StudyCardRule rule : studyCard.getRules()) {
				applyStudyCardRule(acquisition, rule, dicomAttributes);
			}
		}
		acquisition.setStudyCard(studyCard);
		acquisition.setStudyCardTimestamp(studyCard.getLastEditTimestamp());
	}	

	public void applyStudyCard(StudyCardApply studyCardApplyObject) {
		StudyCard studyCard = studyCardService.findById(studyCardApplyObject.getStudyCardId());
		LOG.debug("apply studycard n° " + studyCard.getId());
		List<DatasetAcquisition> acquisitions = datasetAcquisitionService.findById(studyCardApplyObject.getDatasetAcquisitionIds());
		for (DatasetAcquisition acquisition : acquisitions) {
			Attributes dicomAttributes = getDicomAttributesForAcquisition(acquisition);
			if (studyCard.getRules() != null) {
				for (StudyCardRule rule : studyCard.getRules()) {
					applyStudyCardRule(acquisition, rule, dicomAttributes);
				}
			}
			acquisition.setStudyCard(studyCard);
			acquisition.setStudyCardTimestamp(studyCard.getLastEditTimestamp());
		}
		datasetAcquisitionService.update(acquisitions);
	}
	
	private Attributes getDicomAttributesForAcquisition(DatasetAcquisition acquisition) {
		if (CollectionUtils.isNotEmpty(acquisition.getDatasets())) {
			List<URL> urls = new ArrayList<>();
			try {
				DatasetUtils.getDatasetFilePathURLs(acquisition.getDatasets().get(0), urls, DatasetExpressionFormat.DICOM);
				if (!urls.isEmpty()) {
					String jsonMetadataStr = downloader.downloadDicomMetadataForURL(urls.get(0));
					JsonParser parser = Json.createParser(new StringReader(jsonMetadataStr));
					Attributes dicomAttributes = new JSONReader(parser).readDataset(null);
					if (dicomAttributes != null) {
						return dicomAttributes;
					} else {
						LOG.error("Could not apply studycard on dataset acquisition " + acquisition.getId() 
								+ " : dicom attributes are empty");
					}
				} else {
					LOG.error("Could not apply studycard on dataset acquisition " + acquisition.getId() 
					+ " : no pacs url for this acquisition");
				}
			} catch (IOException | MessagingException | RestClientException e) {
				throw new RestClientException("Can not get dicom attributes for acquisition " + acquisition.getId(), e);
			}
		}
		return null;
	}

	public void applyStudyCardOnStudy(StudyCard studyCard) {
		if (studyCard.getRules() != null) {
			final List<Examination> examinations = examinationService.findByStudyId(studyCard.getStudyId());
			LOG.info(examinations.size() + " examinations found for studyId: " + studyCard.getStudyId());
			for (Examination examination : examinations) {
				final List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
				if (CollectionUtils.isNotEmpty(acquisitions)) {
					LOG.info(acquisitions.size() + " acquisitions found for examination with id: " + examination.getId());
					final List<StudyCardRule> rules = studyCard.getRules();
					LOG.info(rules.size() + " rules found for study card with id: " + studyCard.getId());
					for (StudyCardRule rule : rules) {
						if (rule.getType() == StudyCardRuleType.EXAMINATION.getId()) {
							boolean check = checkStudyCardRule(rule, acquisitions);
							if (!check) {
								LOG.error("Examination found, not valid: id: " + examination.getId() + ", comment: " + examination.getComment());							
							}
						}
					}
				}				
			}
		}
	}
	
	private void applyStudyCardRule(DatasetAcquisition acquisition, StudyCardRule rule, Attributes dicomAttributes) {
		if (rule.getConditions() == null || rule.getConditions().isEmpty() || conditionsFulfilled(rule.getConditions(), dicomAttributes, acquisition)) {
			if (rule.getAssignments() != null) applyAssignments(acquisition, rule.getAssignments());
		}
	}

	private boolean checkStudyCardRule(StudyCardRule rule, List<DatasetAcquisition> acquisitions) {
		return conditionsFulfilled(rule.getConditions(), acquisitions);
	}

	private boolean conditionsFulfilled(List<StudyCardCondition> conditions, Attributes dicomAttributes, DatasetAcquisition acquisition) {
		for (StudyCardCondition condition : conditions) {
			if (!dicomConditionFulfilled(condition, dicomAttributes)) return false;
		}
		return true;
	}

	private boolean conditionsFulfilled(List<StudyCardCondition> conditions, List<DatasetAcquisition> acquisitions) {
		for (StudyCardCondition condition : conditions) {
			boolean conditionVerifiedOnAtLeastOneAcquisition = false;
			for (DatasetAcquisition acquisition: acquisitions) {
				int getDicomTagOrField = condition.getDicomTagOrField();
				// A) check for a dicom tag using a QIDO call to the pacs
				if (Field.getEnum(getDicomTagOrField) == null) {
					Attributes dicomAttributes = getDicomAttributesForAcquisition(acquisition);
					if (dicomConditionFulfilled(condition, dicomAttributes)) {
						conditionVerifiedOnAtLeastOneAcquisition = true;
					}
				// B) check for a field in the database, using entity model
				} else {
					if (entityConditionFulfilled(condition, acquisition)) {
						conditionVerifiedOnAtLeastOneAcquisition = true;
					}
				}
			}	
			if (!conditionVerifiedOnAtLeastOneAcquisition) return false;
		}
		return true;
	}

	private boolean entityConditionFulfilled(StudyCardCondition condition, DatasetAcquisition acquisition) {
		int fieldId = condition.getDicomTagOrField();
		Field field = Field.getEnum(fieldId);
		if (field != null) {
			String valueFromDb = field.get(acquisition);
			if (valueFromDb != null) {
				// get all possible values, that can fulfill the condition
				for (StudyCardConditionValue value : condition.getValues()) {
					if (value.getValue() == null) throw new IllegalArgumentException("A condition value cannot be null.");
					LOG.info("operation: " + condition.getOperation().name()
						+ ", valueFromDb: " + valueFromDb + ", valueFromSC: " + value.getValue());
					if (textualCompare(condition.getOperation(), valueFromDb, value.getValue())) {
						LOG.info("condition fulfilled: acq.name=" + valueFromDb + ", value=" + value.getValue());
						return true; // as condition values are combined by OR: return if one is true
					}	
				}
			}
		}
		return false;
	}

	private boolean dicomConditionFulfilled(StudyCardCondition condition, Attributes dicomAttributes) {
		LOG.info("conditionFulfilled: " + condition.getId() + " processing one condition with all its values: ");
		condition.getValues().stream().forEach(s -> LOG.info(s.getValue()));
		VR tagVr = StandardElementDictionary.INSTANCE.vrOf(condition.getDicomTagOrField());
		DicomTagType tagType = DicomTagType.valueOf(tagVr);
		// get all possible values, that can fulfill the condition
		for (StudyCardConditionValue value : condition.getValues()) {
			if (value.getValue() == null) throw new IllegalArgumentException("A condition value cannot be null.");
			if (tagType.isNumerical()) {
				if (!condition.getOperation().isNumerical()) {
					throw new IllegalArgumentException("Study card processing : operation " + condition.getOperation() + " is not compatible with dicom tag " 
							+ condition.getDicomTagOrField() + " of type " + tagType + "(condition id : " + condition.getId() + ")");
				}
				BigDecimal scValue = new BigDecimal(value.getValue());
				Integer comparison = null;
				if (DicomTagType.Float.equals(tagType)) {
					Float floatValue = dicomAttributes.getFloat(condition.getDicomTagOrField(), Float.MIN_VALUE);			
					comparison = BigDecimal.valueOf(floatValue).compareTo(scValue);
				// There is no dicomAttributes.getLong() !
				}	else if (DicomTagType.Double.equals(tagType) || DicomTagType.Long.equals(tagType)) {
					Double doubleValue = dicomAttributes.getDouble(condition.getDicomTagOrField(), Double.MIN_VALUE);			
					comparison = BigDecimal.valueOf(doubleValue).compareTo(scValue);
				} else if (DicomTagType.Integer.equals(tagType)) {
					Integer integerValue = dicomAttributes.getInt(condition.getDicomTagOrField(), Integer.MIN_VALUE);
					comparison = BigDecimal.valueOf(integerValue).compareTo(scValue);
				}
				if (comparison != null && numericalCompare(condition.getOperation(), comparison)) {
					return true; // as condition values are combined by OR: return if one is true
				}
			} else if (tagType.isTextual()) {
				if (!condition.getOperation().isTextual()) {
					throw new IllegalArgumentException("Study card processing : operation " + condition.getOperation() + " is not compatible with dicom tag " 
							+ condition.getDicomTagOrField() + " of type " + tagType + "(condition id : " + condition.getId() + ")");
				}	
				String stringValue = dicomAttributes.getString(condition.getDicomTagOrField());
				if (stringValue == null) {
					LOG.warn("Could not find a value in the dicom for the tag " + condition.getDicomTagOrField());
					return false;
				}				
				if (textualCompare(condition.getOperation(), stringValue, value.getValue())) {
					return true; // as condition values are combined by OR: return if one is true
				}
			}
		}
		return false;
	}
	
	private boolean numericalCompare(Operation operation, int comparison) {
		if (Operation.BIGGER_THAN.equals(operation)) {
			return comparison > 0;
		} else if (Operation.EQUALS.equals(operation)) {
			return comparison == 0;
		} else if (Operation.SMALLER_THAN.equals(operation)) {
			return comparison < 0;
		}
		throw new IllegalArgumentException("Cannot use this method for non-numerical operations (" + operation + ")");
	}
	
	private boolean textualCompare(Operation operation, String original, String studycardStr) {
		if (original != null) {
			if (Operation.EQUALS.equals(operation)) {
				return original.equals(studycardStr);
			} else if (Operation.CONTAINS.equals(operation)) {
				return original.contains(studycardStr);
			} else if (Operation.STARTS_WITH.equals(operation)) {
				return original.startsWith(studycardStr);
			} else if (Operation.ENDS_WITH.equals(operation)) {
				return original.endsWith(studycardStr);
			}
		} else {
			LOG.error("Error in studycard processing: tag (from pacs) or field (from database) null.");
			return false;
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
			LOG.debug("apply assignment : " + assignment);
			LOG.debug("on acquisition : " + acquisition);
			Field field = assignment.getField();
			if (field != null) {
				field.update(acquisition, assignment.getValue());
			} else {
				LOG.error("Error in studycard processing: field null");
			}			
		} catch (IllegalArgumentException e) {
			LOG.error("Error in studycard processing: ", e);
		}
	}

}
