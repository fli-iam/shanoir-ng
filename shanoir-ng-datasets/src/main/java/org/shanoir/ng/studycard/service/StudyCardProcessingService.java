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
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.mail.MessagingException;

import org.apache.commons.collections4.CollectionUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.StandardElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONReader;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.repository.SubjectStudyRepository;
import org.shanoir.ng.studycard.dto.StudyCardOnStudyResult;
import org.shanoir.ng.studycard.dto.SubjectStudyStudyCardTag;
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
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyCardProcessingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyCardProcessingService.class);
	
	@Autowired
	private StudyCardService studyCardService;
	
	@Autowired
	private SubjectStudyRepository subjectStudyRepository;
	
	@Autowired
	private ExaminationService examinationService;
	
	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;
	
	@Autowired
	private WADODownloaderService downloader;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Application during import, when dicoms are present in tmp directory.
	 * @param acquisition
	 * @param studyCard
	 * @param dicomAttributes
	 */
	public void applyStudyCard(DatasetAcquisition acquisition, StudyCard studyCard, Attributes dicomAttributes) {
		if (studyCard.getRules() != null) {
			for (StudyCardRule rule : studyCard.getRules()) {
				applyStudyCardRule(acquisition, rule, dicomAttributes);
			}
		}
		acquisition.setStudyCard(studyCard);
		acquisition.setStudyCardTimestamp(studyCard.getLastEditTimestamp());
	}	

	/**
	 * Re-application on using web GUI and list of dataset acquisitions.
	 * @param studyCardApplyObject
	 */
	public void applyStudyCard(StudyCardApply studyCardApplyObject) {
		StudyCard studyCard = studyCardService.findById(studyCardApplyObject.getStudyCardId());
		LOG.debug("re-apply studycard n° " + studyCard.getId());
		List<DatasetAcquisition> acquisitions = datasetAcquisitionService.findById(studyCardApplyObject.getDatasetAcquisitionIds());
		boolean changeInAtLeastOneAcquisition = false;
		for (DatasetAcquisition acquisition : acquisitions) {
			if (CollectionUtils.isNotEmpty(acquisition.getDatasets())) {
				Dataset refDataset = acquisition.getDatasets().get(0);
				Attributes dicomAttributes = getDicomAttributesForDataset(refDataset);
				if (studyCard.getRules() != null) {
					for (StudyCardRule rule : studyCard.getRules()) {
						applyStudyCardRule(acquisition, rule, dicomAttributes);
					}
					acquisition.setStudyCard(studyCard);
					acquisition.setStudyCardTimestamp(studyCard.getLastEditTimestamp());
					changeInAtLeastOneAcquisition = true;
				}
			}
		}
		if (changeInAtLeastOneAcquisition) { // no need to update, if nothing happened
			datasetAcquisitionService.update(acquisitions);
		}
	}

	/**
	 * Study cards for quality control: apply on entire study.
	 * 
	 * @param studyCard
	 * @throws MicroServiceCommunicationException 
	 */
	public List<StudyCardOnStudyResult> applyStudyCardOnStudy(StudyCard studyCard) throws MicroServiceCommunicationException {
		final List<StudyCardRule> rules = studyCard.getRules();
		if (CollectionUtils.isNotEmpty(rules)) {
			final List<StudyCardOnStudyResult> studyCardOnStudyResultList = new ArrayList<StudyCardOnStudyResult>();
			final List<SubjectStudyStudyCardTag> subjectStudyStudyCardTagList = new ArrayList<SubjectStudyStudyCardTag>();
			final List<SubjectStudy> subjectStudyList = subjectStudyRepository.findByStudyId(studyCard.getStudyId());
			for (SubjectStudy subjectStudy : subjectStudyList) {
				final List<Examination> examinations = examinationService.findBySubjectIdStudyId(subjectStudy.getSubject().getId(), studyCard.getStudyId());
				LOG.info(examinations.size() + " examinations found for subject: " + subjectStudy.getSubject().getName());
				for (Examination examination : examinations) {
					StudyCardOnStudyResult result = new StudyCardOnStudyResult();
					result.setSubjectName(subjectStudy.getSubject().getName());
					result.setExaminationDate(examination.getExaminationDate().toString());
					result.setExaminationDate(examination.getComment());
					List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
					// today study cards are only used for MR modality
					acquisitions = acquisitions.stream().filter(a -> a instanceof MrDatasetAcquisition).collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(acquisitions)) {
						LOG.info(acquisitions.size() + " acquisitions found for examination with id: " + examination.getId());
						LOG.info(rules.size() + " rules found for study card with id: " + studyCard.getId() + " and name: " + studyCard.getName());
						boolean allRulesFulFilled = true;
						SubjectStudyStudyCardTag subjectStudyStudyCardTag = new SubjectStudyStudyCardTag();
						subjectStudyStudyCardTag.setId(subjectStudy.getId());
						for (StudyCardRule rule : rules) {
							if (rule.getType() == StudyCardRuleType.EXAMINATION.getId()) {
								if (!conditionsFulfilledOnAtLeastOneAcquisition(rule.getConditions(), acquisitions, result)) {
									allRulesFulFilled = false;
								}
							} else if (rule.getType() == StudyCardRuleType.ACQUISITION.getId()) {
								if (!conditionsFulfilledOnAllAcquisitions(rule.getConditions(), acquisitions, result)) {
									allRulesFulFilled = false;
								}								
							} else if (rule.getType() == StudyCardRuleType.DATASET.getId()) {
								if (!conditionsFulfilledOnAllDatasets(rule.getConditions(), acquisitions, result)) {
									allRulesFulFilled = false;
								}							
							}
						}
						if (allRulesFulFilled) {
							subjectStudyStudyCardTag.setType(1);
						} else {
							subjectStudyStudyCardTag.setType(3);
						}
						studyCardOnStudyResultList.add(result);
						subjectStudyStudyCardTagList.add(subjectStudyStudyCardTag);
					}				
				}
			}
			try {
				rabbitTemplate.convertAndSend(RabbitMQConfiguration.STUDIES_SUBJECT_STUDY_STUDY_CARD_TAG,
						objectMapper.writeValueAsString(subjectStudyStudyCardTagList));
			} catch (AmqpException | JsonProcessingException e) {
				throw new MicroServiceCommunicationException("Error while communicating with MS studies to send study card tags.");
			}
			return studyCardOnStudyResultList;
		} else {
			throw new RestClientException("Study card used with emtpy rules.");
		}
	}
	
	private void applyStudyCardRule(DatasetAcquisition acquisition, StudyCardRule rule, Attributes dicomAttributes) {
		if (rule.getConditions() == null || rule.getConditions().isEmpty() || conditionsFulfilled(rule.getConditions(), dicomAttributes, acquisition)) {
			if (rule.getAssignments() != null) applyAssignments(acquisition, rule.getAssignments());
		}
	}

	private boolean conditionsFulfilled(List<StudyCardCondition> conditions, Attributes dicomAttributes, DatasetAcquisition acquisition) {
		for (StudyCardCondition condition : conditions) {
			if (!dicomConditionFulfilled(condition, dicomAttributes)) return false;
		}
		return true;
	}

	private boolean conditionsFulfilledOnAtLeastOneAcquisition(List<StudyCardCondition> conditions, List<DatasetAcquisition> acquisitions, StudyCardOnStudyResult result) {
		for (StudyCardCondition condition : conditions) {
			boolean conditionVerifiedOnAtLeastOneAcquisition = false;
			for (DatasetAcquisition acquisition: acquisitions) {
				int getDicomTagOrField = condition.getDicomTagOrField();
				// A) check for a dicom tag using a metadata call to the pacs
				if (Field.getEnum(getDicomTagOrField) == null) {
					if (CollectionUtils.isNotEmpty(acquisition.getDatasets())) {
						Dataset refDataset = acquisition.getDatasets().get(0);
						Attributes dicomAttributes = getDicomAttributesForDataset(refDataset);
						if (dicomConditionFulfilled(condition, dicomAttributes)) {
							conditionVerifiedOnAtLeastOneAcquisition = true;
						}
					}
				// B) check for a field in the database, using entity model
				} else {
					if (entityConditionFulfilled(condition, acquisition)) {
						conditionVerifiedOnAtLeastOneAcquisition = true;
					}
				}
			}	
			if (!conditionVerifiedOnAtLeastOneAcquisition) {
				result.setResultExaminationLevel("Error with condition: " + condition.getDicomTagOrField() + ", " + condition.getOperation() + ", " + condition.getValues().toString());
				return false;
			}
		}
		return true;
	}
	
	private boolean conditionsFulfilledOnAllAcquisitions(List<StudyCardCondition> conditions, List<DatasetAcquisition> acquisitions, StudyCardOnStudyResult result) {
		for (StudyCardCondition condition : conditions) {
			for (DatasetAcquisition acquisition: acquisitions) {
				int getDicomTagOrField = condition.getDicomTagOrField();
				// A) check for a dicom tag using a metadata call to the pacs
				if (Field.getEnum(getDicomTagOrField) == null) {
					if (CollectionUtils.isNotEmpty(acquisition.getDatasets())) {
						Dataset refDataset = acquisition.getDatasets().get(0);
						Attributes dicomAttributes = getDicomAttributesForDataset(refDataset);
						if (!dicomConditionFulfilled(condition, dicomAttributes)) {
							result.setResultAcquisitionLevel("Error with condition: " + condition.getDicomTagOrField() + ", " + condition.getOperation() + ", " + condition.getValues().toString());
							return false;
						}
					}
				// B) check for a field in the database, using entity model
				} else {
					if (!entityConditionFulfilled(condition, acquisition)) {
						result.setResultAcquisitionLevel("Error with condition: " + condition.getDicomTagOrField() + ", " + condition.getOperation() + ", " + condition.getValues().toString());						
						return false;
					}
				}
			}	
		}
		return true;
	}
	
	private boolean conditionsFulfilledOnAllDatasets(List<StudyCardCondition> conditions, List<DatasetAcquisition> acquisitions, StudyCardOnStudyResult result) {
		for (StudyCardCondition condition : conditions) {
			for (DatasetAcquisition acquisition: acquisitions) {
				if (CollectionUtils.isNotEmpty(acquisition.getDatasets())) {
					for (Dataset dataset : acquisition.getDatasets()) {
						int getDicomTagOrField = condition.getDicomTagOrField();
						// A) check for a dicom tag using a metadata call to the pacs
						if (Field.getEnum(getDicomTagOrField) == null) {
							Attributes dicomAttributes = getDicomAttributesForDataset(dataset);
							if (!dicomConditionFulfilled(condition, dicomAttributes)) {
								result.setResultDatasetLevel("Error with condition: " + condition.getDicomTagOrField() + ", " + condition.getOperation() + ", " + condition.getValues().toString());														
								return false;
							}
						// B) check for a field in the database, using entity model
						} else {
							if (!entityConditionFulfilled(condition, acquisition)) {
								result.setResultDatasetLevel("Error with condition: " + condition.getDicomTagOrField() + ", " + condition.getOperation() + ", " + condition.getValues().toString());														
								return false;
							}
						}					
					}
				}
			}	
		}
		return true;
	}

	private Attributes getDicomAttributesForDataset(Dataset dataset) {
		List<URL> urls = new ArrayList<>();
		try {
			DatasetUtils.getDatasetFilePathURLs(dataset, urls, DatasetExpressionFormat.DICOM);
			if (!urls.isEmpty()) {
				String jsonMetadataStr = downloader.downloadDicomMetadataForURL(urls.get(0));
				JsonParser parser = Json.createParser(new StringReader(jsonMetadataStr));
				Attributes dicomAttributes = new JSONReader(parser).readDataset(null);
				if (dicomAttributes != null) {
					return dicomAttributes;
				} else {
					LOG.error("Could not find dicom attributes for dataset with id: " + dataset.getId());
				}
			} else {
				LOG.error("Could not find dicom attributes for dataset with id: " + dataset.getId()
				+ " : no pacs url for this dataset");
			}
		} catch (IOException | MessagingException | RestClientException e) {
			throw new RestClientException("Can not get dicom attributes for dataset " + dataset.getId(), e);
		}
		return null;
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
