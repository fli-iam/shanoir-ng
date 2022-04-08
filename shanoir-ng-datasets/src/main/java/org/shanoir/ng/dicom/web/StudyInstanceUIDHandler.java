package org.shanoir.ng.dicom.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class StudyInstanceUIDHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyInstanceUIDHandler.class);

	private static final String WADO_URI_STUDY_UID_SERIES_UID = "studyUID=(.*?)\\&seriesUID";
	
	private static final String WADO_RS_STUDY_UID_SERIES_UID = "/studies/(.*?)/series/";

	private static final String DICOM_TAG_STUDY_INSTANCE_UID = "0020000D";

	private static final String DICOM_TAG_RETRIEVE_URL = "00081190";

	private static final String VALUE = "Value";

	private static final String RETRIEVE_URL_SERIE_LEVEL = "/studies/(.*)/series/";

	private static final String RETRIEVE_URL_STUDY_LEVEL = "/studies/(.*)";

	private static final String STUDIES = "/studies/";

	private static final String SERIES = "/series/";
	
	public static final String PREFIX = UIDGeneration.ROOT + ".";

	@Autowired
	private ExaminationService examinationService;
	
	private HashMap<Long, String> examinationIdToStudyInstanceUIDCache;
	
	@PostConstruct
	public void init() {
		examinationIdToStudyInstanceUIDCache = new HashMap<Long, String>(1000);
		LOG.info("DICOMWeb cache created: examinationIdToStudyInstanceUIDCache");
	}
	
	@Scheduled(cron = "0 0 6 * * *", zone="Europe/Paris")
	public void clearExaminationIdToStudyInstanceUIDCache() {
		examinationIdToStudyInstanceUIDCache.clear();
		LOG.info("DICOMWeb cache cleared: examinationIdToStudyInstanceUIDCache");
	}
	
	/**
	 * This method replaces StudyInstanceUIDs returned from the PACS with IDs
	 * of examinations in Shanoir, in the Json returned.
	 * 
	 * @param root
	 * @param examinationId
	 * @param studyLevel
	 */
	public void replaceStudyInstanceUIDsWithExaminationIds(JsonNode root, Long examinationId, boolean studyLevel) {
		if (root.isObject()) {
			// find attribute: StudyInstanceUID
			JsonNode studyInstanceUIDNode = root.get(DICOM_TAG_STUDY_INSTANCE_UID);
			if (studyInstanceUIDNode != null) {
				ArrayNode studyInstanceUIDArray = (ArrayNode) studyInstanceUIDNode.path(VALUE);
				for (int i = 0; i < studyInstanceUIDArray.size(); i++) {
					studyInstanceUIDArray.remove(i);
					studyInstanceUIDArray.insert(i,  PREFIX + examinationId.toString());
				}				
			}
			// find attribute: RetrieveURL
			JsonNode retrieveURLNode = root.get(DICOM_TAG_RETRIEVE_URL);
			if (retrieveURLNode != null) {
				ArrayNode retrieveURLArray = (ArrayNode) retrieveURLNode.path(VALUE);
				for (int i = 0; i < retrieveURLArray.size(); i++) {
					JsonNode arrayElement = retrieveURLArray.get(i);
					String retrieveURL = arrayElement.asText();
					if (studyLevel) { // study level
						retrieveURL = retrieveURL.replaceFirst(RETRIEVE_URL_STUDY_LEVEL, STUDIES + PREFIX + examinationId);
						retrieveURLArray.remove(i);
						retrieveURLArray.insert(i, retrieveURL);
					} else { // serie level
						retrieveURL = retrieveURL.replaceFirst(RETRIEVE_URL_SERIE_LEVEL, STUDIES + PREFIX + examinationId + SERIES);
						retrieveURLArray.remove(i);
						retrieveURLArray.insert(i, retrieveURL);
					}
				}				
			}
		} else if (root.isArray()) {
			ArrayNode arrayNode = (ArrayNode) root;
			for (int i = 0; i < arrayNode.size(); i++) {
				JsonNode arrayElement = arrayNode.get(i);
				replaceStudyInstanceUIDsWithExaminationIds(arrayElement, examinationId, studyLevel);
			}
		}
	}

	/**
	 * This method returns the corresponding StudyInstanceUID, that is generated during the import in Shanoir
	 * with the pseudonymization module and present in the PACS, either from a local cache to accelerate the
	 * request response time or from the database, in table dataset_file.
	 * 
	 * @param examinationId
	 * @return
	 */
	public String findStudyInstanceUIDFromCacheOrDatabase(Long examinationId) {
		String studyInstanceUID = examinationIdToStudyInstanceUIDCache.get(examinationId);
		if (studyInstanceUID == null) {
			Examination examination = examinationService.findById(examinationId);
			if (examination != null) {
				studyInstanceUID = findStudyInstanceUID(examination);
				if (studyInstanceUID != null) {
					examinationIdToStudyInstanceUIDCache.put(examination.getId(), studyInstanceUID);
					LOG.info("DICOMWeb cache adding: " + examinationId + ", " + studyInstanceUID);
					LOG.info("DICOMWeb cache, size: " + examinationIdToStudyInstanceUIDCache.size());
				}
			}
		}
		return studyInstanceUID;
	}
	
	/**
	 * This method walks down the information model in Shanoir to read the StudyInstanceUID
	 * from the table dataset_file.path, that contains the WADO link.
	 * Only DICOM related dataset acquisition types are considered: MR, CT, PET.
	 * 
	 * @param examination
	 * @return
	 */
	private String findStudyInstanceUID(Examination examination) {
		List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
		for (DatasetAcquisition acquisition : acquisitions) {
			if (acquisition instanceof MrDatasetAcquisition
				|| acquisition instanceof CtDatasetAcquisition
				|| acquisition instanceof PetDatasetAcquisition) {
				List<Dataset> datasets = acquisition.getDatasets();
				if (!datasets.isEmpty()) {
					Dataset dataset = datasets.get(0);
					List<DatasetExpression> expressions = dataset.getDatasetExpressions();
					if (!expressions.isEmpty()) {
						for (DatasetExpression expression : expressions) {
							// only DICOM is of interest here
							if (expression.getDatasetExpressionFormat().equals(DatasetExpressionFormat.DICOM)) {
								List<DatasetFile> files = expression.getDatasetFiles();
								if (!files.isEmpty()) {
									DatasetFile file = files.get(0);
									if (file.isPacs()) {
										String path = file.getPath();
										return findStudyInstanceUID(path);
									}
								}
							}
						}
					}
				}				
			}
		}
		return null;
	}

	/**
	 * This method extracts the StudyInstanceUID from a WADO string.
	 * It tries first WADO-URI, and then WADO-RS, in case of nothing
	 * could be found for WADO-URI.
	 * 
	 * @param path
	 */
	private String findStudyInstanceUID(String path) {
		Pattern p = Pattern.compile(WADO_URI_STUDY_UID_SERIES_UID);
		Matcher m = p.matcher(path);
		while (m.find()) {
			return m.group(1);
		}
		p = Pattern.compile(WADO_RS_STUDY_UID_SERIES_UID);
		m = p.matcher(path);
		while (m.find()) {
			return m.group(1);
		}
		return null;
	}
	
	public Long extractExaminationId(String examinationIdWithPrefix) {
		String prefix = UIDGeneration.ROOT + ".";
		String examinationIdWithoutPrefix = examinationIdWithPrefix.substring(prefix.length());
		Long id = Long.parseLong(examinationIdWithoutPrefix);
		return id;
	}
	
}
