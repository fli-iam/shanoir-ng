package org.shanoir.uploader.service.rest;

import java.io.ByteArrayInputStream;
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
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.json.JSONObject;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.model.dto.StudyCardOnStudyResultDTO;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.Manufacturer;
import org.shanoir.uploader.model.rest.ManufacturerModel;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * 
 * Service layer for REST services of sh-ng.
 *
 * @author mkain
 *
 */
@Component
public class ShanoirUploaderServiceClient {

	private static final Logger logger = LoggerFactory.getLogger(ShanoirUploaderServiceClient.class);
	
	private static final String SHANOIR_SERVER_URL = "shanoir.server.url";
	
	private static final String SERVICE_STUDIES_CREATE = "service.studies.create";

	private static final String SERVICE_STUDIES_NAMES_CENTERS = "service.studies.names.centers";
	
	private static final String SERVICE_STUDYCARDS_CREATE = "service.studycards.create";

	private static final String SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS = "service.studycards.find.by.study.ids";

	private static final String SERVICE_STUDYCARDS_APPLY_ON_STUDY = "service.studycards.apply.on.study";

	private static final String SERVICE_QUALITYCARDS_FIND_BY_STUDY_ID = "service.qualitycards.find.by.study.id";
	
	private static final String SERVICE_CENTERS_CREATE = "service.centers.create";

	private static final String SERVICE_CENTERS_FIND_OR_CREATE_BY_INSTITUTION_DICOM = "service.centers.find.or.create.by.institution.dicom";

	private static final String SERVICE_ACQUISITION_EQUIPMENTS = "service.acquisition.equipments";
	
	private static final String SERVICE_ACQUISITION_EQUIPMENTS_BY_SERIAL_NUMBER = "service.acquisition.equipments.by.serial.number";

	private static final String SERVICE_ACQUISITION_EQUIPMENTS_BY_EQUIPMENT_DICOM = "service.acquisition.equipments.find.or.create.by.equipment.dicom";

	private static final String SERVICE_MANUFACTURER_MODELS = "service.manufacturer.models";
	
	private static final String SERVICE_MANUFACTURERS = "service.manufacturers";
	
	private static final String SERVICE_SUBJECTS_FIND_BY_IDENTIFIER = "service.subjects.find.by.identifier";

	private static final String SERVICE_SUBJECTS_FIND_BY_NAME_AND_STUDY = "service.subjects.find.by.identifier";

	private static final String SERVICE_DATASETS = "service.datasets";
	
	private static final String SERVICE_DATASETS_DICOM_WEB_STUDIES = "service.datasets.dicom.web.studies";
	
	private static final String SERVICE_SUBJECTS_CREATE = "service.subjects.create";
	
	private static final String SERVICE_EXAMINATIONS_CREATE = "service.examinations.create";
	
	private static final String SERVICE_IMPORTER_CREATE_TEMP_DIR = "service.importer.create.temp.dir";
	
	private static final String SERVICE_IMPORTER_START_IMPORT_JOB = "service.importer.start.import.job";
	
	private static final String SERVICE_IMPORTER_UPLOAD_DICOM = "service.importer.upload.dicom";

	private static final String SERVICE_EXAMINATIONS_BY_SUBJECT_ID = "service.examinations.find.by.subject.id";

	private static final String SERVICE_SUBJECTS_BY_STUDY_ID = "service.subjects.by.study.id";

	private HttpService httpService;
	
	private String serverURL;

	private String serviceURLStudiesCreate;
	
	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLStudyCardsCreate;

	private String serviceURLStudyCardsByStudyIds;

	private String serviceURLStudyCardsApplyOnStudy;

	private String serviceURLQualityCardsByStudyId;
	
	private String serviceURLCentersCreate;

	private String serviceURLCentersFindOrCreateByInstitutionDicom;

	private String serviceURLAcquisitionEquipments;
	
	private String serviceURLAcquisitionEquipmentsBySerialNumber;

	private String serviceURLAcquisitionEquipmentsFindOrCreateByEquipmentDicom;
	
	private String serviceURLManufacturerModels;
	
	private String serviceURLManufacturers;
	
	private String serviceURLSubjectsCreate;

	private String serviceURLSubjectsFindByIdentifier;

	private String serviceURLSubjectsFindBySubjectNameAndStudy;

	private String serviceURLDatasets;
	
	private String serviceURLDatasetsDicomWebStudies;
	
	private String serviceURLExaminationsCreate;
	
	private String serviceURLImporterCreateTempDir;
	
	private String serviceURLImporterStartImportJob;
	
	private String serviceURLImporterUploadDicom;

	private String serviceURLExaminationsBySubjectId;

	private String serviceURLSubjectsByStudyId;

	private Map<Integer, String> apiResponseMessages;

	public void configure() {

		apiResponseMessages = new HashMap<Integer, String>();
		apiResponseMessages.put(200, "ok");
		apiResponseMessages.put(204, "no item found");
		apiResponseMessages.put(401, "unauthorized");
		apiResponseMessages.put(403, "forbidden");
		apiResponseMessages.put(500, "unexpected error");

		this.serverURL = ShUpConfig.profileProperties.getProperty(SHANOIR_SERVER_URL);

		this.serviceURLStudiesCreate = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_STUDIES_CREATE);
		this.serviceURLStudiesNamesAndCenters = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_STUDIES_NAMES_CENTERS);
		this.serviceURLStudyCardsCreate = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_STUDYCARDS_CREATE);
		this.serviceURLStudyCardsByStudyIds = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS);
		this.serviceURLStudyCardsApplyOnStudy = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_STUDYCARDS_APPLY_ON_STUDY);
		this.serviceURLQualityCardsByStudyId = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_QUALITYCARDS_FIND_BY_STUDY_ID);
		this.serviceURLCentersCreate = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_CENTERS_CREATE);
		this.serviceURLCentersFindOrCreateByInstitutionDicom = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_CENTERS_FIND_OR_CREATE_BY_INSTITUTION_DICOM);
		this.serviceURLAcquisitionEquipments = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENTS);
		this.serviceURLAcquisitionEquipmentsBySerialNumber = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENTS_BY_SERIAL_NUMBER);
		this.serviceURLAcquisitionEquipmentsFindOrCreateByEquipmentDicom = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENTS_BY_EQUIPMENT_DICOM);
		this.serviceURLManufacturerModels = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_MANUFACTURER_MODELS);
		this.serviceURLManufacturers = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_MANUFACTURERS);
		this.serviceURLSubjectsFindByIdentifier = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_IDENTIFIER);
		this.serviceURLSubjectsFindBySubjectNameAndStudy = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_NAME_AND_STUDY);
		this.serviceURLDatasets = this.serverURL + ShUpConfig.endpointProperties.getProperty(SERVICE_DATASETS);
		this.serviceURLDatasetsDicomWebStudies = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_DATASETS_DICOM_WEB_STUDIES);
		this.serviceURLSubjectsCreate = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_SUBJECTS_CREATE);
		this.serviceURLExaminationsCreate = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_EXAMINATIONS_CREATE);
		this.serviceURLImporterCreateTempDir = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_IMPORTER_CREATE_TEMP_DIR);
		this.serviceURLImporterStartImportJob = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_IMPORTER_START_IMPORT_JOB);
		this.serviceURLImporterUploadDicom = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_IMPORTER_UPLOAD_DICOM);
		this.serviceURLExaminationsBySubjectId = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_EXAMINATIONS_BY_SUBJECT_ID);
		this.serviceURLSubjectsByStudyId = this.serverURL
				+ ShUpConfig.endpointProperties.getProperty(SERVICE_SUBJECTS_BY_STUDY_ID);

		this.httpService = new HttpService(this.serverURL);

		logger.info("ShanoirUploaderService successfully initialized.");
	}

	/**
	 * We use separate HttpService here, as the proxy test comes before the profile
	 * selection, where we learn later the final address of the Shanoir server to
	 * communicate with.
	 * 
	 * @param testURL
	 * @return
	 * @throws IOException
	 */
	public static int testProxy(String testURL) throws Exception {
		int code = 0;
		long startTime = System.currentTimeMillis();
		HttpService httpService = new HttpService(testURL);
		try (CloseableHttpResponse response = httpService.get(testURL)) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.info("Proxy test with URL: " + testURL + ", duration: " + elapsedTime + "ms.");
			if (response != null) {
				code = response.getCode();
			}
			httpService.closeHttpClient();
			return code;
		}
	}
	
 	public String loginWithKeycloakForToken(String username, String password) throws Exception {
		String keycloakURL = this.serverURL + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
		try {
			final StringBuilder postBody = new StringBuilder();
			postBody.append("client_id=shanoir-uploader");
			postBody.append("&grant_type=password");
			postBody.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
			postBody.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
			postBody.append("&scope=offline_access");
			try (CloseableHttpResponse response = httpService.post(keycloakURL, postBody.toString(), true);) {
				if(response == null) {
					logger.error("Error while asking authentification token from: " + keycloakURL);
					return null;
				}
				HttpEntity httpEntity = response.getEntity();
				if (httpEntity != null) {
					String responseEntityString = EntityUtils.toString(httpEntity);
					final int statusCode = response.getCode();
					if (HttpStatus.SC_OK == statusCode) {
						JSONObject responseEntityJson = new JSONObject(responseEntityString);
						String refreshToken = responseEntityJson.getString("refresh_token");
						refreshToken(keycloakURL, refreshToken);
						return responseEntityJson.getString("access_token");
					}				
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);				
			}
		} catch (UnsupportedEncodingException e) {
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
				try (CloseableHttpResponse response = httpService.post(keycloakURL, postBody.toString(), true)) {
					String responseEntityString = EntityUtils.toString(response.getEntity());
					final int statusCode = response.getCode();
					if (HttpStatus.SC_OK == statusCode) {
						JSONObject responseEntityJson = new JSONObject(responseEntityString);
						String newAccessToken = responseEntityJson.getString("access_token");
						if (newAccessToken != null) {
							ShUpOnloadConfig.setTokenString(newAccessToken);
						} else {
							logger.error("ERROR: with access token refresh.");
						}
						logger.debug("Access token has been refreshed.");
					} else {
						logger.error("ERROR: Access token could NOT be refreshed: HttpStatus-" + statusCode);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		};
		executor.scheduleAtFixedRate(task, 0, 240, TimeUnit.SECONDS);
	}
	
	public List<Study> findStudiesNamesAndCenters() throws Exception {
		long startTime = System.currentTimeMillis();
		try (CloseableHttpResponse response = httpService.get(this.serviceURLStudiesNamesAndCenters)) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.info("findStudiesNamesAndCenters: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<Study> studies = Util.getMappedList(response, Study.class);
				return studies;
			} else {
				logger.error("Could not get study names and centers (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				return null;
			}
		}
	}

	public List<StudyCard> findStudyCardsByStudyIds(IdList studyIds) throws Exception {
		try {
			String json = Util.objectWriter.writeValueAsString(studyIds);
			long startTime = System.currentTimeMillis();
			try (CloseableHttpResponse response = httpService.post(this.serviceURLStudyCardsByStudyIds, json, false)) {
				long stopTime = System.currentTimeMillis();
			    long elapsedTime = stopTime - startTime;
			    logger.info("findStudyCardsByStudyIds: " + elapsedTime + "ms");
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					List<StudyCard> studyCards = Util.getMappedList(response, StudyCard.class);
					return studyCards;
				} else {
					logger.error("Could not get study cards for studyIds (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Subject findSubjectBySubjectIdentifier(String subjectIdentifier) throws Exception {
		long startTime = System.currentTimeMillis();
		try (CloseableHttpResponse response = httpService.get(this.serviceURLSubjectsFindByIdentifier + URLEncoder.encode(subjectIdentifier, "UTF-8"))) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.info("findSubjectBySubjectIdentifier: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				Subject subject = Util.getMappedObject(response, Subject.class);
				return subject;
			} else if (code == HttpStatus.SC_NO_CONTENT) {
				return null; // no content, not found is fine as well
			} else {
				logger.warn("Could not find subject with identifier (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				throw new Exception(code + " Error with subjectBySubjectIdentifier search");
			}
		}
	}

	public List<Subject> findSubjectsByStudy(Long studyId) throws Exception {
		long startTime = System.currentTimeMillis();
		URIBuilder b = new URIBuilder(this.serviceURLSubjectsByStudyId + studyId + "/allSubjects");
		b.addParameter("preclinical",  "null");
		URL url = b.build().toURL();
		try (CloseableHttpResponse response = httpService.get(url.toString())) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.info("findSubjectsByStudy: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<Subject> subjects = Util.getMappedList(response, Subject.class);
				logger.info("findSubjectsByStudy: " + subjects.size() + " subjects found for study: " + studyId);
				return subjects;
			} else {
				logger.error("Could not get subjects from study id " + studyId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				return null;
			}
		}
	}
	
	public String createTempDir() throws Exception {
		try (CloseableHttpResponse response = httpService.get(this.serviceURLImporterCreateTempDir)) {
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				String importTempDirId = Util.getMappedObject(response, String.class);
				return importTempDirId;
			} else {
				logger.error("Could not create tempDir on server (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				return null;
			}
		}
	}
		
	public List<Examination> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			long startTime = System.currentTimeMillis();
			try (CloseableHttpResponse response = httpService.get(this.serviceURLExaminationsBySubjectId + subjectId)) {
				long stopTime = System.currentTimeMillis();
			    long elapsedTime = stopTime - startTime;
			    logger.info("findExaminationsBySubjectId: " + elapsedTime + "ms");
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					List<Examination> examinations = Util.getMappedList(response, Examination.class);
					logger.info("findExaminationsBySubjectId: " + examinations.size() + " examinations found for subject: " + subjectId);
					return examinations;
				} else {
					logger.warn("Could not get exam(s) for subject with id " + subjectId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");					
				}
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
			try (CloseableHttpResponse response = httpService.get(url.toString())) {
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
		}
		return null;
	}

	public List<Long> findDatasetIdsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			try (CloseableHttpResponse response = httpService.get(this.serviceURLDatasets + "subject/" + subjectId)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					List<Long> datasetIds = Util.getMappedList(response, Long.class);
					return datasetIds;
				} else {
					logger.error("Could not get dataset ids from subject id " + subjectId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		}
		return null;
	}

	public List<Long> findDatasetIdsBySubjectIdStudyId(Long subjectId, Long studyId) throws Exception {
		if (subjectId != null) {
			try (CloseableHttpResponse response = httpService
					.get(this.serviceURLDatasets + "subject/" + subjectId + "/study/" + studyId)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					List<Long> datasetIds = Util.getMappedList(response, Long.class);
					return datasetIds;
				} else {
					logger.error("Could not get dataset ids from subject id " + subjectId + " and study id " + studyId
							+ " (status code: " + code + ", message: "
							+ apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		}
		return null;
	}

	public List<AcquisitionEquipment> findAcquisitionEquipments() throws Exception {
		long startTime = System.currentTimeMillis();
		try (CloseableHttpResponse response = httpService.get(this.serviceURLAcquisitionEquipments)) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.info("findAcquisitionEquipments: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<AcquisitionEquipment> acquisitionEquipments = Util.getMappedList(response,
						AcquisitionEquipment.class);
				return acquisitionEquipments;
			} else {
				logger.error("Could not find acquisition equipments (status code: " + code + ", message: "
						+ apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}
	
	public List<AcquisitionEquipment> findAcquisitionEquipmentsBySerialNumber(String serialNumber) throws Exception {
		long startTime = System.currentTimeMillis();
		try (CloseableHttpResponse response = httpService.get(this.serviceURLAcquisitionEquipmentsBySerialNumber + serialNumber)) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.info("findAcquisitionEquipmentsBySerialNumber: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<AcquisitionEquipment> acquisitionEquipments = Util.getMappedList(response,
						AcquisitionEquipment.class);
				return acquisitionEquipments;
			} else {
				logger.error("Could not find acquisition equipments by serial number (status code: " + code + ", message: "
						+ apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		}
		return null;
	}

	public List<AcquisitionEquipment> findAcquisitionEquipmentsOrCreateByEquipmentDicom(final EquipmentDicom equipmentDicom, Long centerId) {
		try {
			long startTime = System.currentTimeMillis();
			String json = Util.objectWriter.writeValueAsString(equipmentDicom);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLAcquisitionEquipmentsFindOrCreateByEquipmentDicom + centerId, json, false)) {
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info("findAcquisitionEquipmentsOrCreateByEquipmentDicom: " + elapsedTime + "ms");
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					List<AcquisitionEquipment> acquisitionEquipments = Util.getMappedList(response,
							AcquisitionEquipment.class);
					return acquisitionEquipments;
				} else {
					logger.error("Could not find acquisition equipment(s) or create by equipment dicom (status code: " + code + ", message: "
							+ apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} catch(JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public void uploadFile(String tempDirId, File file) throws Exception {
		try (CloseableHttpResponse response = httpService.postFile(this.serviceURLImporterCreateTempDir, tempDirId,
				file)) {
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
			} else {
				logger.error("Error in uploadFile: with tempDirId " + tempDirId + " with file (path: "
						+ file.getAbsolutePath() + ", size in bytes: " + Files.size(file.toPath()) + "), status code: "
						+ code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code"));
				throw new Exception("Error in uploadFile.");
			}
		}
	}
	
	public ImportJob uploadDicom(File file) throws Exception {
		try (CloseableHttpResponse response = httpService.postFile(this.serviceURLImporterUploadDicom, file)) {
			try (response) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					ImportJob importJob = Util.getMappedObject(response, ImportJob.class);
					return importJob;
				} else {
					logger.error("Error in uploadDicom: "
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
					throw new Exception("Error in uploadDicom");
				}
			}
		}		
	}
	
	public void startImportJob(String importJobJsonStr) throws Exception {
		try (CloseableHttpResponse response = httpService.post(this.serviceURLImporterStartImportJob, importJobJsonStr,
				false)) {
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				logger.info("Import job successfully started on server.");
			} else {
				logger.error("Error in startImportJob: with json " + importJobJsonStr + " (status code: " + code
						+ ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				throw new Exception("Error in startImportJob");
			}
		}
	}

	public CloseableHttpResponse downloadDatasetById(Long datasetId, String format) throws Exception {
		if (datasetId != null) {
			URIBuilder b = new URIBuilder(this.serviceURLDatasets + "download/" + datasetId);
			b.addParameter("format", format);
			URL url = b.build().toURL();
			try (CloseableHttpResponse response = httpService.get(url.toString())) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					return response;
				} else {
					logger.error("Could not get dataset id " + datasetId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		}
		return null;
	}

	public CloseableHttpResponse downloadDatasetsByIds(List<Long> datasetIds, String format) throws Exception {
		if (datasetIds != null) {
			String datasetIdsString = datasetIds.stream().map(Object::toString).collect(Collectors.joining(","));
			String url = this.serviceURLDatasets + "massiveDownload?datasetIds=" + datasetIdsString + "&format=" + format;
			try (CloseableHttpResponse response = httpService.get(url.toString())) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					return response;
				} else {
					logger.error("Could not get dataset ids " + datasetIds + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
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
			try (CloseableHttpResponse response = httpService.get(url.toString())) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					return response;
				} else {
					logger.error("Could not get dataset of study " + studyId + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		}
		return null;
	}
	
	public Study createStudy(final Study study) {
		try {
			String json = Util.objectWriter.writeValueAsString(study);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLStudiesCreate, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					Study studyCreated = Util.getMappedObject(response, Study.class);
					return studyCreated;
				} else {
					logger.error("Error in createStudy: with study " + study.getName()
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ioE) {
			logger.error(ioE.getMessage(), ioE);			
		}
		return null;
	}

	public StudyCard createStudyCard(final StudyCard studyCard) {
		try {
			String json = Util.objectWriter.writeValueAsString(studyCard);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLStudyCardsCreate, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					StudyCard studyCardCreated = Util.getMappedObject(response, StudyCard.class);
					return studyCardCreated;
				} else {
					logger.error("Error in createStudyCard: with study " + studyCard.getName()
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ioE) {
			logger.error(ioE.getMessage(), ioE);			
		}
		return null;
	}

	public Center createCenter(final Center center) {
		try {
			String json = Util.objectWriter.writeValueAsString(center);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLCentersCreate, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					Center centerCreated = Util.getMappedObject(response, Center.class);
					return centerCreated;
				} else {
					logger.error("Error in createCenter: with center " + center.getName() + " (status code: " + code
							+ ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Center findCenterOrCreateByInstitutionDicom(final InstitutionDicom institutionDicom, Long studyId) {
		try {
			String json = Util.objectWriter.writeValueAsString(institutionDicom);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLCentersFindOrCreateByInstitutionDicom + "/" + studyId, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					Center center = Util.getMappedObject(response, Center.class);
					return center;
				} else {
					logger.error("Error in findCenterOrCreateByInstitutionDicom: with institution dicom " + institutionDicom.getInstitutionName() 
						+ " (status code: " + code	+ ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public AcquisitionEquipment createEquipment(final AcquisitionEquipment equipment) {
		try {
			String json = Util.objectWriter.writeValueAsString(equipment);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLAcquisitionEquipments, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					AcquisitionEquipment equipmentCreated = Util.getMappedObject(response, AcquisitionEquipment.class);
					return equipmentCreated;
				} else {
					logger.error("Error in createEquipment: with equipment " + equipment.getSerialNumber()
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ioE) {
			logger.error(ioE.getMessage(), ioE);			
		}
		return null;
	}

	public List<Manufacturer> findManufacturers() {
		try (CloseableHttpResponse response = httpService.get(this.serviceURLManufacturers)) {
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<Manufacturer> manufacturers = Util.getMappedList(response,Manufacturer.class);
				return manufacturers;
			} else {
				logger.error("Could not find manufacturers (status code: " + code + ", message: "
						+ apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);			
		}
		return null;
	}

	public Manufacturer createManufacturer(final Manufacturer manufacturer) {
		try {
			String json = Util.objectWriter.writeValueAsString(manufacturer);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLManufacturers, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					Manufacturer manufacturerCreated = Util.getMappedObject(response, Manufacturer.class);
					return manufacturerCreated;
				} else {
					logger.error("Error in createManufacturer: with manufacturer " + manufacturer.getName()
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);			
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public ManufacturerModel createManufacturerModel(final ManufacturerModel manufacturerModel) {
		try {
			String json = Util.objectWriter.writeValueAsString(manufacturerModel);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLManufacturerModels, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					ManufacturerModel manufacturerModelCreated = Util.getMappedObject(response, ManufacturerModel.class);
					return manufacturerModelCreated;
				} else {
					logger.error("Error in createManufacturerModel: with manufacturerModel " + manufacturerModel.getName()
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);			
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
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
			String json = Util.objectWriter.writeValueAsString(subject);
			CloseableHttpResponse response;
			if (modeSubjectCommonNameManual) {
				response = httpService.post(this.serviceURLSubjectsCreate, json, false);
			} else {
				response = httpService.post(this.serviceURLSubjectsCreate + "?centerId=" + centerId, json, false);
			}
			try (response) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					Subject subjectDTOCreated = Util.getMappedObject(response, Subject.class);
					return subjectDTOCreated;
				} else {
					logger.error("Error in createSubject: with subject " + subject.getName()
						+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ioE) {
			logger.error(ioE.getMessage(), ioE);			
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
			String json = Util.objectWriter.writeValueAsString(subject);
			try (CloseableHttpResponse response = httpService.put(this.serviceURLSubjectsCreate + "/" + subject.getId(), json)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_NO_CONTENT) {
					return subject;
				} else {
					logger.error("Error in createSubjectStudy: with subject " + subject.getName()
					+ " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ioE) {
			logger.error(ioE.getMessage(), ioE);			
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
			String json = Util.objectWriter.writeValueAsString(examinationDTO);
			try (CloseableHttpResponse response = httpService.post(this.serviceURLExaminationsCreate, json, false)) {
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					Examination examinationDTOCreated = Util.getMappedObject(response, Examination.class);
					return examinationDTOCreated;
				} else {
					logger.error("Error in createExamination: with examinationDTO " + examinationDTO.getComment()
							+ " (status code: " + code + ", message: "
							+ apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception ioE) {
			logger.error(ioE.getMessage(), ioE);
		}
		return null;
	}

	public List<StudyCardOnStudyResultDTO> applyStudyCardOnStudy(Long studyCardId) throws Exception {
		logger.info("Apply studycard on study, started on server.");
		try (CloseableHttpResponse response = httpService.get(this.serviceURLStudyCardsApplyOnStudy + studyCardId)) {
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				List<StudyCardOnStudyResultDTO> results = Util.getMappedList(response, StudyCardOnStudyResultDTO.class);
				return results;
			} else {
				logger.error("Error in applyStudyCardOnStudy: (status code: " + code
						+ ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				throw new Exception("Error in applyStudyCardOnStudy");
			}
		}
	}

	public List<QualityCard> findQualityCardsByStudyId(Long studyId) throws Exception {
		logger.info("Retrieving qualitycards for the study : " + studyId);
		try {
			String studyIdentifier = URLEncoder.encode(Long.toString(studyId), "UTF-8");
			long startTime = System.currentTimeMillis(); 
			try (CloseableHttpResponse response = httpService.get(this.serviceURLQualityCardsByStudyId + studyIdentifier)) {
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info("findQualityCardsByStudyId: " + elapsedTime + "ms");
				int code = response.getCode();
				if (code == HttpStatus.SC_OK) {
					List<QualityCard> qualityCards = Util.getMappedList(response, QualityCard.class);
					return qualityCards;
				} else {
					logger.error("Could not get quality cards for studyId : " +  studyIdentifier + " (status code: " + code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code") + ")");
				}
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public void postDicomSR(File file) throws Exception {
		try (CloseableHttpResponse response = httpService.postFileMultipartRelated(this.serviceURLDatasetsDicomWebStudies, file)) {
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
			} else {
				logger.error("Error in postDicomSR: with file (path: "
						+ file.getAbsolutePath() + ", size in bytes: " + Files.size(file.toPath()) + "), status code: "
						+ code + ", message: " + apiResponseMessages.getOrDefault(code, "unknown status code"));
				throw new Exception("Error in postDicomSR");
			}
		}		
	}

	public Attributes getDicomInstance(String examinationUID, String seriesInstanceUID, String sopInstanceUID) throws Exception {
		long startTime = System.currentTimeMillis();
		URIBuilder b = new URIBuilder(this.serviceURLDatasetsDicomWebStudies
			+ "/" + examinationUID
			+ "/series/" +  seriesInstanceUID
			+ "/instances/" + sopInstanceUID);
		URL url = b.build().toURL();
		try (CloseableHttpResponse response = httpService.getDicom(url.toString())) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.debug("getDicomInstance: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String contentType = entity.getContentType();
	                byte[] rawData = EntityUtils.toByteArray(entity);
					ByteArrayDataSource ds = new ByteArrayDataSource(rawData, contentType);
					MimeMultipart multipart = new MimeMultipart(ds);
					if (multipart.getCount() > 0) {
						MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(0); // assuming single-part for now
						try (DicomInputStream din = new DicomInputStream(part.getInputStream())) {
							return din.readDataset();
						}
					} else {
						throw new RuntimeException("No parts found in multipart DICOM response.");
					}
				}
			} else {
				logger.error("Error in getDicomInstance: status code: "
					+ code + ", message: "
					+ apiResponseMessages.getOrDefault(code, "unknown status code"));
				throw new Exception("Error in getDicomFile");
			}
		}
		return null;	
	}

	// public Boolean isStudyExisting(String studyInstanceUID) throws Exception {
	// 	long startTime = System.currentTimeMillis();
	// 	URIBuilder b = new URIBuilder(this.serviceURLDatasetsDicomWebStudies + "?StudyInstanceUID=" + studyInstanceUID);
	// 	URL url = b.build().toURL();
	// 	try (CloseableHttpResponse response = httpService.get(url.toString())) {
	// 		long stopTime = System.currentTimeMillis();
	// 		long elapsedTime = stopTime - startTime;
	// 		logger.debug("isStudyExisting: " + elapsedTime + "ms");
	// 		int code = response.getCode();
	// 		if (code == HttpStatus.SC_OK) {
	// 			return true;
	// 		} else if (code == HttpStatus.SC_NO_CONTENT) {
	// 			return false;
	// 		} else {
	// 			logger.error("Error in isStudyExisting: status code: "
	// 				+ code + ", message: "
	// 				+ apiResponseMessages.getOrDefault(code, "unknown status code"));
	// 			throw new Exception("Error in isStudyExisting");
	// 		}
	// 	}
	// }

	public Boolean isStudyOnServer(String studyInstanceUID) throws Exception {
		long startTime = System.currentTimeMillis();
		URIBuilder b = new URIBuilder(this.serviceURLDatasetsDicomWebStudies + "/" + studyInstanceUID + "/instances");
		URL url = b.build().toURL();
		try (CloseableHttpResponse response = httpService.get(url.toString())) {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			logger.debug("isStudyOnServer: " + elapsedTime + "ms");
			int code = response.getCode();
			if (code == HttpStatus.SC_OK) {
				return true;
			} else if (code == HttpStatus.SC_NO_CONTENT || code == HttpStatus.SC_NOT_FOUND) {
				return false;
			} else {
				logger.error("Error in isStudyOnServer: status code: "
					+ code + ", message: "
					+ apiResponseMessages.getOrDefault(code, "unknown status code"));
				throw new Exception("Error in isStudyOnServer");
			}
		}
	}

}
