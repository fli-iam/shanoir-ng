package org.shanoir.uploader.service.rest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.utils.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * Service layer for REST services of sh-ng.
 *
 * @author mkain
 *
 */
public class ShanoirUploaderServiceClientNG {

	private static Logger logger = Logger.getLogger(ShanoirUploaderServiceClientNG.class);
	
	private static final String SHANOIR_SERVER_URL = "shanoir.server.url";

	private static final String SERVICE_STUDIES_NAMES_CENTERS = "service.studies.names.centers";

	private static final String SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS = "service.studycards.find.by.study.ids";

	private static final String SERVICE_ACQUISITION_EQUIPMENT_BY_ID = "service.acquisition.equipment.find.by.id";
	
	private static final String SERVICE_SUBJECTS_FIND_BY_IDENTIFIER = "service.subjects.find.by.identifier";

	private static final String SERVICE_DATASETS = "service.datasets";
	
	private static final String SERVICE_SUBJECTS_CREATE = "service.subjects.create";
	
	private static final String SERVICE_EXAMINATIONS_CREATE = "service.examinations.create";
	
	private static final String SERVICE_IMPORTER_CREATE_TEMP_DIR = "service.importer.create.temp.dir";
	
	private static final String SERVICE_IMPORTER_START_IMPORT_JOB = "service.importer.start.import.job";

	private static final String SERVICE_IMPORTER_START_IMPORT = "service.importer.start.import";

	private static final String SERVICE_EXAMINATIONS_BY_SUBJECT_ID = "service.examinations.find.by.subject.id";

	private static final String SERVICE_SUBJECTS_BY_STUDY_ID = "service.subjects.by.study.id";

	private HttpService httpService;
	
	private String serverURL;

	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLStudyCardsByStudyIds;
	
	private String serviceURLAcquisitionEquipmentById;
	
	private String serviceURLSubjectsFindByIdentifier;
	
	private String serviceURLDatasets;
	
	private String serviceURLSubjectsCreate;
	
	private String serviceURLExaminationsCreate;
	
	private String serviceURLImporterCreateTempDir;
	
	private String serviceURLImporterStartImportJob;

	private String serviceURLImporterStartImport;

	private String serviceURLExaminationsBySubjectId;

	private String serviceURLSubjectsByStudyId;

	private Map<Integer, String> apiResponseMessages;

	public ShanoirUploaderServiceClientNG() {
		this.httpService = new HttpService();
		
		apiResponseMessages = new HashMap<Integer, String>();
		apiResponseMessages.put(200, "ok");
		apiResponseMessages.put(204, "no item found");
		apiResponseMessages.put(401, "unauthorized");
		apiResponseMessages.put(403, "forbidden");
		apiResponseMessages.put(500, "unexpected error");
		
		this.serverURL = ShUpConfig.profileProperties.getProperty(SHANOIR_SERVER_URL);
			this.serviceURLStudiesNamesAndCenters = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_STUDIES_NAMES_CENTERS);
		this.serviceURLStudyCardsByStudyIds = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS);
		this.serviceURLAcquisitionEquipmentById = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENT_BY_ID);
		this.serviceURLSubjectsFindByIdentifier = this.serverURL
			+ ShUpConfig.profileProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_IDENTIFIER);
		this.serviceURLDatasets = this.serverURL
			+ ShUpConfig.profileProperties.getProperty(SERVICE_DATASETS);
		this.serviceURLSubjectsCreate = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_SUBJECTS_CREATE);
		this.serviceURLExaminationsCreate = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_EXAMINATIONS_CREATE);
		this.serviceURLImporterCreateTempDir = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_IMPORTER_CREATE_TEMP_DIR);
		this.serviceURLImporterStartImportJob = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_IMPORTER_START_IMPORT_JOB);
		this.serviceURLExaminationsBySubjectId = this.serverURL
		+ ShUpConfig.profileProperties.getProperty(SERVICE_EXAMINATIONS_BY_SUBJECT_ID);
		this.serviceURLSubjectsByStudyId = this.serverURL
		+ ShUpConfig.profileProperties.getProperty(SERVICE_SUBJECTS_BY_STUDY_ID);
		this.serviceURLImporterStartImport = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_IMPORTER_START_IMPORT);
		logger.info("ShanoirUploaderServiceNG successfully initialized.");
	}
	
	public String loginWithKeycloakForToken(String username, String password) {
		String keycloakURL = this.serverURL + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
		try {
			final StringBuilder postBody = new StringBuilder();
			postBody.append("client_id=shanoir-uploader");
			postBody.append("&grant_type=password");
			postBody.append("&username=").append(username);
			postBody.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
			postBody.append("&scope=offline_access");
			HttpResponse response = httpService.post(keycloakURL, postBody.toString(), true);
			String responseEntityString = EntityUtils.toString(response.getEntity());
			final int statusCode = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK == statusCode) {
				JSONObject responseEntityJson = new JSONObject(responseEntityString);
				return responseEntityJson.getString("access_token");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List<Study> findStudiesNamesAndCenters() {
		HttpResponse response = httpService.get(this.serviceURLStudiesNamesAndCenters);
		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {
			List<Study> studies = Util.getMappedList(response, Study.class);
			return studies;
		} else {
			return null;
		}
	}

	public List<StudyCard> findStudyCardsByStudyIds(IdList studyIds) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(studyIds);
			HttpResponse response = httpService.post(this.serviceURLStudyCardsByStudyIds, json, false);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				List<StudyCard> studyCards = Util.getMappedList(response, StudyCard.class);
				return studyCards;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Subject findSubjectBySubjectIdentifier(String subjectIdentifier) throws Exception {
		HttpResponse response = httpService.get(this.serviceURLSubjectsFindByIdentifier + URLEncoder.encode(subjectIdentifier, "UTF-8"));
		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {
			Subject subjectDTO = Util.getMappedObject(response, Subject.class);
			return subjectDTO;
		} else {
			return null;
		}
	}
	
	public String createTempDir() throws Exception {
		HttpResponse response = httpService.get(this.serviceURLImporterCreateTempDir);
		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {
			String importTempDirId = Util.getMappedObject(response, String.class);
			return importTempDirId;
		} else {
			return null;
		}
	}
		
	public List<Examination> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLExaminationsBySubjectId + subjectId);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<Examination> examinations = Util.getMappedList(response, Examination.class);
				return examinations;
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsByStudyId(Long studyId) throws Exception {
		if (studyId != null) {
			List<Long> datasetIds = new ArrayList<Long>();
			URI url = UriBuilder.fromUri(this.serviceURLSubjectsByStudyId + studyId + "/allSubjects").queryParam("preclinical",  "null").build();
			HttpResponse response = httpService.get(url.toString());
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				List<Subject> subjects = Util.getMappedList(response, Subject.class);
				for(Subject subject : subjects) {
					List<Long> ids = findDatasetIdsBySubjectIdStudyId(subject.getId(), studyId);
					if(ids != null) {
						datasetIds.addAll(ids);
					}
				}
				return datasetIds;
			} else {
				logger.error("Could not get subjects ids from study id " + studyId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				List<Long> datasetIds = Util.getMappedList(response, Long.class);
				return datasetIds;
			} else {
				logger.error("Could not get dataset ids from subject id " + subjectId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsBySubjectIdStudyId(Long subjectId, Long studyId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId + "/study/" + studyId);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				List<Long> datasetIds = Util.getMappedList(response, Long.class);
				return datasetIds;
			} else {
				logger.error("Could not get dataset ids from subject id " + subjectId + " and study id " + studyId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public AcquisitionEquipment findAcquisitionEquipmentById(Long acquisitionEquipmentId) throws Exception {
		if (acquisitionEquipmentId != null) {
			HttpResponse response = httpService.get(this.serviceURLAcquisitionEquipmentById + acquisitionEquipmentId);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				AcquisitionEquipment acquisitionEquipment = Util.getMappedObject(response, AcquisitionEquipment.class);
				return acquisitionEquipment;
			}
		}
		return null;
	}
	
	public void uploadFile(String tempDirId, File file) throws Exception {
		HttpResponse response = httpService.postFile(this.serviceURLImporterCreateTempDir, tempDirId, file);
		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {
		} else {
			throw new Exception("Error in uploadFile.");
		}
	}
	
	public void startImportJob(String importJobJsonStr) throws Exception {
		HttpResponse response = httpService.post(this.serviceURLImporterStartImportJob, importJobJsonStr, false);
		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {
		} else {
			throw new Exception("Error in startImportJob.");
		}
	}
	
	public void startImport(String exchangeJsonStr) throws Exception {
		HttpResponse response = httpService.post(this.serviceURLImporterStartImport, exchangeJsonStr, false);
		int code = response.getStatusLine().getStatusCode();
		if (code == HttpStatus.SC_OK) {
		} else {
			throw new Exception("Error in startImport.");
		}
	}

	public HttpResponse downloadDatasetById(Long datasetId, String format) throws Exception {
		if (datasetId != null) {
			URI url = UriBuilder.fromUri(this.serviceURLDatasets + "download/" + datasetId).queryParam("format", format).build();
			HttpResponse response = httpService.get(url.toString());
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				return response;
			} else {
				logger.error("Could not get dataset id " + datasetId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public HttpResponse downloadDatasetsByIds(List<Long> datasetIds, String format) throws Exception {
		if (datasetIds != null) {
			String datasetIdsString = datasetIds.stream().map(Object::toString).collect(Collectors.joining(","));
			String url = this.serviceURLDatasets + "massiveDownload?datasetIds=" + datasetIdsString + "&format=" + format;
			HttpResponse response = httpService.get(url.toString());
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				return response;
			} else {
				logger.error("Could not get dataset ids " + datasetIds + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public HttpResponse downloadDatasetsByStudyId(Long studyId, String format) throws Exception {
		if (studyId != null) {
			URI url = UriBuilder.fromUri(this.serviceURLDatasets + "massiveDownloadByStudy").queryParam("studyId", studyId).queryParam("format", format).build();
			HttpResponse response = httpService.get(url.toString());
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				return response;
			} else {
				logger.error("Could not get dataset of study " + studyId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}
	
	/**
	 * This method creates a subject on the server.
	 * 
	 * @param studyId
	 * @param studyCardId
	 * @param modeSubjectCommonName
	 * @param subject
	 * @return boolean true, if success
	 */
	public Subject createSubject(
			final Subject subject,
			final boolean modeSubjectCommonNameManual,
			final Long centerId) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(subject);
			HttpResponse response;
			if (modeSubjectCommonNameManual) {
				response = httpService.post(this.serviceURLSubjectsCreate, json, false);
			} else {
				response = httpService.post(this.serviceURLSubjectsCreate + "?centerId=" + centerId, json, false);
			}
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				Subject subjectDTOCreated = Util.getMappedObject(response, Subject.class);
				return subjectDTOCreated;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * This method updates a subject on the server and therefore updates
	 * the rel_subject_study list too.
	 * 
	 * @param subject
	 * @return
	 */
	public Subject createSubjectStudy(
			final Subject subject) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(subject);
			HttpResponse response = httpService.put(this.serviceURLSubjectsCreate + "/" + subject.getId(), json);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_NO_CONTENT) {
				return subject;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * This method creates an examination on the server.
	 * 
	 * @param examinationDTO
	 * @return
	 */
	public Examination createExamination(final Examination examinationDTO) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(examinationDTO);
			HttpResponse response = httpService.post(this.serviceURLExaminationsCreate, json, false);
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) {
				Examination examinationDTOCreated = Util.getMappedObject(response, Examination.class);
				return examinationDTOCreated;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}