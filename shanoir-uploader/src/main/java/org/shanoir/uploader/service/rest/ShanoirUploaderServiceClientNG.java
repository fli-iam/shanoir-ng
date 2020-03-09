package org.shanoir.uploader.service.rest;

import java.io.File;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
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

	private static final String SERVICE_EXAMINATIONS_BY_SUBJECT_ID = "service.examinations.find.by.subject.id";
	
	private static final String SERVICE_SUBJECTS_CREATE = "service.subjects.create";
	
	private static final String SERVICE_EXAMINATIONS_CREATE = "service.examinations.create";	

	private HttpService httpService;
	
	private String serverURL;
	
	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLStudyCardsByStudyIds;
	
	private String serviceURLAcquisitionEquipmentById;
	
	private String serviceURLSubjectsFindByIdentifier;
	
	private String serviceURLExaminationsBySubjectId;
	
	private String serviceURLSubjectsCreate;
	
	private String serviceURLExaminationsCreate;

	public ShanoirUploaderServiceClientNG() {
		this.httpService = new HttpService();
		this.serverURL = ShUpConfig.shanoirNGServerProperties.getProperty(SHANOIR_SERVER_URL);
		this.serviceURLStudiesNamesAndCenters = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_STUDIES_NAMES_CENTERS);
		this.serviceURLStudyCardsByStudyIds = this.serverURL
				+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_STUDYCARDS_FIND_BY_STUDY_IDS);
		this.serviceURLAcquisitionEquipmentById = this.serverURL
				+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_ACQUISITION_EQUIPMENT_BY_ID);
		this.serviceURLSubjectsFindByIdentifier = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_IDENTIFIER);
		this.serviceURLExaminationsBySubjectId = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_EXAMINATIONS_BY_SUBJECT_ID);
		this.serviceURLSubjectsCreate = this.serverURL
				+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_SUBJECTS_CREATE);
		this.serviceURLExaminationsCreate = this.serverURL
				+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_EXAMINATIONS_CREATE);
		logger.info("ShanoirUploaderServiceNG successfully initialized.");
	}
	
	public List<Study> findStudiesNamesAndCenters() {
		HttpResponse response = httpService.get(this.serviceURLStudiesNamesAndCenters);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
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
			HttpResponse response = httpService.post(this.serviceURLStudyCardsByStudyIds, json);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				List<StudyCard> studyCards = Util.getMappedList(response, StudyCard.class);
				return studyCards;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public Subject findSubjectBySubjectIdentifier(String subjectIdentifier) throws Exception {
		HttpResponse response = httpService.get(this.serviceURLSubjectsFindByIdentifier + subjectIdentifier);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
			Subject subjectDTO = Util.getMappedObject(response, Subject.class);
			return subjectDTO;
		} else {
			return null;
		}
	}

	public List<Examination> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLExaminationsBySubjectId + subjectId);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<Examination> examinations = Util.getMappedList(response, Examination.class);
				return examinations;
			}
		}
		return null;
	}
	
	public AcquisitionEquipment findAcquisitionEquipmentById(Long acquisitionEquipmentId) throws Exception {
		if (acquisitionEquipmentId != null) {
			HttpResponse response = httpService.get(this.serviceURLAcquisitionEquipmentById + acquisitionEquipmentId);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				AcquisitionEquipment acquisitionEquipment = Util.getMappedObject(response, AcquisitionEquipment.class);
				return acquisitionEquipment;
			}
		}
		return null;
	}
	
	public String uploadFile(final String folderName, final File file) throws Exception {
		final FileDataSource fDS = new FileDataSource(file);
		final DataHandler dataHandler = new DataHandler(fDS);
		final String result = null;
		if (!"200".equals(result)) {
			logger.error(result);
			throw new Exception("File upload error occured!");
		}
		return result;
	}
	
	/**
	 * This method creates a subject on the server.
	 * 
	 * @param studyId
	 * @param studyCardId
	 * @param modeSubjectCommonName
	 * @param subjectDTO
	 * @return boolean true, if success
	 */
	public Subject createSubject(
			final Subject subjectDTO,
			final boolean modeSubjectCommonNameManual,
			final Long centerId) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(subjectDTO);
			HttpResponse response;
			if (modeSubjectCommonNameManual) {
				response = httpService.post(this.serviceURLSubjectsCreate, json);
			} else {
				response = httpService.post(this.serviceURLSubjectsCreate + "?centerId=" + centerId, json);
			}
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				Subject subjectDTOCreated = Util.getMappedObject(response, Subject.class);
				return subjectDTOCreated;
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
			HttpResponse response = httpService.post(this.serviceURLExaminationsCreate, json);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				Examination examinationDTOCreated = Util.getMappedObject(response, Examination.class);
				return examinationDTOCreated;
			}
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}