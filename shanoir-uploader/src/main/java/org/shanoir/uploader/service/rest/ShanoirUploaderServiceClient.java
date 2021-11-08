package org.shanoir.uploader.service.rest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
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
public class ShanoirUploaderServiceClient {

	private static Logger logger = Logger.getLogger(ShanoirUploaderServiceClient.class);
	
	private static final String SHANOIR_SERVER_URL = "shanoir.server.url";

	private static final String SERVICE_STUDIES_NAMES_CENTERS = "service.studies.names.centers";

	private static final String SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS = "service.studycards.find.by.study.ids";

	private static final String SERVICE_ACQUISITION_EQUIPMENTS = "service.acquisition.equipments";
	
	private static final String SERVICE_SUBJECTS_FIND_BY_IDENTIFIER = "service.subjects.find.by.identifier";

	private static final String SERVICE_DATASETS = "service.datasets";
	
	private static final String SERVICE_SUBJECTS_CREATE = "service.subjects.create";
	
	private static final String SERVICE_EXAMINATIONS_CREATE = "service.examinations.create";
	
	private static final String SERVICE_IMPORTER_CREATE_TEMP_DIR = "service.importer.create.temp.dir";
	
	private static final String SERVICE_IMPORTER_START_IMPORT_JOB = "service.importer.start.import.job";

	private static final String SERVICE_EXAMINATIONS_BY_SUBJECT_ID = "service.examinations.find.by.subject.id";

	private static final String SERVICE_SUBJECTS_BY_STUDY_ID = "service.subjects.by.study.id";

	private HttpService httpService;
	
	private String serverURL;

	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLStudyCardsByStudyIds;
	
	private String serviceURLAcquisitionEquipments;
	
	private String serviceURLSubjectsFindByIdentifier;
	
	private String serviceURLDatasets;
	
	private String serviceURLSubjectsCreate;
	
	private String serviceURLExaminationsCreate;
	
	private String serviceURLImporterCreateTempDir;
	
	private String serviceURLImporterStartImportJob;

	private String serviceURLExaminationsBySubjectId;

	private String serviceURLSubjectsByStudyId;

	private Map<Integer, String> apiResponseMessages;

	public ShanoirUploaderServiceClient() {
		
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
		this.serviceURLAcquisitionEquipments = this.serverURL
				+ ShUpConfig.profileProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENTS);
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
		
		this.httpService = new HttpService(this.serverURL);

		logger.info("ShanoirUploaderService successfully initialized.");
	}
	
	public String loginWithKeycloakForToken(String username, String password) throws JSONException {
		String keycloakURL = this.serverURL + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
		try {
			final StringBuilder postBody = new StringBuilder();
			postBody.append("client_id=shanoir-uploader");
			postBody.append("&grant_type=password");
			postBody.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
			postBody.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
			postBody.append("&scope=offline_access");
			CloseableHttpResponse response = httpService.post(keycloakURL, postBody.toString(), true);
			if(response == null) {
				logger.error("Error while asking authentification token from: " + keycloakURL);
				return null;
			}
			HttpEntity httpEntity = response.getEntity();
			String responseEntityString = EntityUtils.toString(httpEntity);
			final int statusCode = response.getCode();
			if (HttpStatus.SC_OK == statusCode) {
				JSONObject responseEntityJson = new JSONObject(responseEntityString);
				String refreshToken = responseEntityJson.getString("refresh_token");
				refreshToken(keycloakURL, refreshToken);
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
	
	/**
	 * Start job, that refreshes the access token every 240 seconds.
	 * The default access token lifetime of Keycloak is 5 min (300 secs),
	 * we update after 4 min (240 secs) to use the time frame, but not to
	 * be to close to the end.
	 */
	private void refreshToken(String keycloakURL, String refreshToken) {
		final StringBuilder postBody = new StringBuilder();
		postBody.append("client_id=shanoir-uploader");
		postBody.append("&grant_type=refresh_token");
		postBody.append("&refresh_token=").append(refreshToken);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable task = () -> {
			try {
				CloseableHttpResponse response = httpService.post(keycloakURL, postBody.toString(), true);
				String responseEntityString = EntityUtils.toString(response.getEntity());
				final int statusCode = response.getCode();
				if (HttpStatus.SC_OK == statusCode) {
					JSONObject responseEntityJson = new JSONObject(responseEntityString);
					String newAccessToken = responseEntityJson.getString("access_token");
					if (newAccessToken != null) {
						ShUpOnloadConfig.setTokenString(newAccessToken);
					} else {
						logger.info("ERROR: with access token refresh.");
					}
					logger.info("Access token has been refreshed.");
				} else {
					logger.info("ERROR: Access token could NOT be refreshed: HttpStatus-" + statusCode);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		};
		executor.scheduleAtFixedRate(task, 0, 240, TimeUnit.SECONDS);
	}
	
	public List<Study> findStudiesNamesAndCenters() {
		long startTime = System.currentTimeMillis();
		CloseableHttpResponse response = httpService.get(this.serviceURLStudiesNamesAndCenters);
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    logger.info("findStudiesNamesAndCenters: " + elapsedTime + "ms");
		int code = response.getCode();
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
			long startTime = System.currentTimeMillis();
			CloseableHttpResponse response = httpService.post(this.serviceURLStudyCardsByStudyIds, json, false);
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    logger.info("findStudyCardsByStudyIds: " + elapsedTime + "ms");
			int code = response.getCode();
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
		CloseableHttpResponse response = httpService.get(this.serviceURLSubjectsFindByIdentifier + URLEncoder.encode(subjectIdentifier, "UTF-8"));
		int code = response.getCode();
		if (code == HttpStatus.SC_OK) {
			Subject subjectDTO = Util.getMappedObject(response, Subject.class);
			return subjectDTO;
		} else {
			return null;
		}
	}
	
	public String createTempDir() throws Exception {
		CloseableHttpResponse response = httpService.get(this.serviceURLImporterCreateTempDir);
		int code = response.getCode();
		if (code == HttpStatus.SC_OK) {
			String importTempDirId = Util.getMappedObject(response, String.class);
			return importTempDirId;
		} else {
			return null;
		}
	}
		
	public List<Examination> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			CloseableHttpResponse response = httpService.get(this.serviceURLExaminationsBySubjectId + subjectId);
			int code = response.getCode();
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
			URIBuilder b = new URIBuilder(this.serviceURLSubjectsByStudyId + studyId + "/allSubjects");
			b.addParameter("preclinical",  "null");
			URL url = b.build().toURL();
			CloseableHttpResponse response = httpService.get(url.toString());
			int code = response.getCode();
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
			CloseableHttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId);
			int code = response.getCode();
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
			CloseableHttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId + "/study/" + studyId);
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<Long> datasetIds = Util.getMappedList(response, Long.class);
				return datasetIds;
			} else {
				logger.error("Could not get dataset ids from subject id " + subjectId + " and study id " + studyId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public List<AcquisitionEquipment> findAcquisitionEquipments() throws Exception {
		long startTime = System.currentTimeMillis();
		CloseableHttpResponse response = httpService.get(this.serviceURLAcquisitionEquipments);
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    logger.info("findAcquisitionEquipments: " + elapsedTime + "ms");
		int code = response.getCode();
		if (code == HttpStatus.SC_OK) {
			List<AcquisitionEquipment> acquisitionEquipments = Util.getMappedList(response, AcquisitionEquipment.class);
			return acquisitionEquipments;
		}
		return null;
	}
	
	public void uploadFile(String tempDirId, File file) throws Exception {
		CloseableHttpResponse response = httpService.postFile(this.serviceURLImporterCreateTempDir, tempDirId, file);
		int code = response.getCode();
		if (code == HttpStatus.SC_OK) {
		} else {
			logger.error("Error in uploadFile: with tempDirId " + tempDirId + " with file (path: " + file.getAbsolutePath()
			+ ", size in bytes: " + Files.size(file.toPath()) + "), status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code"));
			throw new Exception("Error in uploadFile");
		}
	}
	
	public void startImportJob(String importJobJsonStr) throws Exception {
		CloseableHttpResponse response = httpService.post(this.serviceURLImporterStartImportJob, importJobJsonStr, false);
		int code = response.getCode();
		if (code == HttpStatus.SC_OK) {
		} else {
			logger.error("Error in startImportJob: with json " + importJobJsonStr
			+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			throw new Exception("Error in startImportJob");
		}
	}

	public CloseableHttpResponse downloadDatasetById(Long datasetId, String format) throws Exception {
		if (datasetId != null) {
			URIBuilder b = new URIBuilder(this.serviceURLDatasets + "download/" + datasetId);
			b.addParameter("format", format);
			URL url = b.build().toURL();
			CloseableHttpResponse response = httpService.get(url.toString());
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				return response;
			} else {
				logger.error("Could not get dataset id " + datasetId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public CloseableHttpResponse downloadDatasetsByIds(List<Long> datasetIds, String format) throws Exception {
		if (datasetIds != null) {
			String datasetIdsString = datasetIds.stream().map(Object::toString).collect(Collectors.joining(","));
			String url = this.serviceURLDatasets + "massiveDownload?datasetIds=" + datasetIdsString + "&format=" + format;
			CloseableHttpResponse response = httpService.get(url.toString());
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				return response;
			} else {
				logger.error("Could not get dataset ids " + datasetIds + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public CloseableHttpResponse downloadDatasetsByStudyId(Long studyId, String format) throws Exception {
		if (studyId != null) {
			URIBuilder b = new URIBuilder(this.serviceURLDatasets + "massiveDownloadByStudy");
			b.addParameter("studyId", Long.toString(studyId));
			b.addParameter("format", format);
			URL url = b.build().toURL();
			CloseableHttpResponse response = httpService.get(url.toString());
			int code = response.getCode();
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
			CloseableHttpResponse response;
			if (modeSubjectCommonNameManual) {
				response = httpService.post(this.serviceURLSubjectsCreate, json, false);
			} else {
				response = httpService.post(this.serviceURLSubjectsCreate + "?centerId=" + centerId, json, false);
			}
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				Subject subjectDTOCreated = Util.getMappedObject(response, Subject.class);
				return subjectDTOCreated;
			} else {
				logger.error("Error in createSubject: with subject " + subject.getName()
					+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
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
			CloseableHttpResponse response = httpService.put(this.serviceURLSubjectsCreate + "/" + subject.getId(), json);
			int code = response.getCode();
			if (code == HttpStatus.SC_NO_CONTENT) {
				return subject;
			} else {
				logger.error("Error in createSubjectStudy: with subject " + subject.getName()
				+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
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
			CloseableHttpResponse response = httpService.post(this.serviceURLExaminationsCreate, json, false);
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				Examination examinationDTOCreated = Util.getMappedObject(response, Examination.class);
				return examinationDTOCreated;
			} else {
				logger.error("Error in createExamination: with examinationDTO " + examinationDTO.getComment()
				+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");				
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}