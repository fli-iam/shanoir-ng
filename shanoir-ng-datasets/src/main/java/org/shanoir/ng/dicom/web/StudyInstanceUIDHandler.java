package org.shanoir.ng.dicom.web;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyInstanceUIDHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyInstanceUIDHandler.class);

	private static final String WADO_URI_STUDY_UID_SERIES_UID = "studyUID=(.*?)\\&seriesUID";
	
	private static final String WADO_RS_STUDY_UID_SERIES_UID = "/studies/(.*?)/series/";

	@Autowired
	private ExaminationService examinationService;
	
	private HashMap<Long, String> examinationIdToStudyInstanceUIDCache;
	
	@PostConstruct
	public void init() {
		examinationIdToStudyInstanceUIDCache = new HashMap<Long, String>(1000);
		LOG.info("DICOMWeb cache created: examinationIdToStudyInstanceUIDCache");
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
	 * 
	 * @param examination
	 * @return
	 */
	private String findStudyInstanceUID(Examination examination) {
		List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
		if (!acquisitions.isEmpty()) {
			DatasetAcquisition acquisition = acquisitions.get(0);
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
								String path = file.getPath();
								return findStudyInstanceUID(path);
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
	
}
