package org.shanoir.uploader.service.rest;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.service.rest.dto.ExaminationDTO;
import org.shanoir.uploader.service.rest.dto.SubjectDTO;
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

	private static final String SERVICE_SUBJECTS_FIND_BY_IDENTIFIER = "service.subjects.find.by.identifier";

	private static final String SERVICE_STUDIES_NAMES_CENTERS = "service.studies.names.centers";
	
	private static final String SERVICE_EXAMINATIONS_BY_SUBJECT_ID = "service.examinations.find.by.subject.id";
	
	private static final String SERVICE_SUBJECTS_CREATE = "service.subjects.create";

	private HttpService httpService;
	
	private String serverURL;
	
	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLSubjectsFindByIdentifier;
	
	private String serviceURLExaminationsBySubjectId;
	
	private String serviceURLSubjectsCreate;

	public ShanoirUploaderServiceClientNG() {
		this.httpService = new HttpService();
		this.serverURL = ShUpConfig.shanoirNGServerProperties.getProperty(SHANOIR_SERVER_URL);
		this.serviceURLStudiesNamesAndCenters = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_STUDIES_NAMES_CENTERS);
		this.serviceURLSubjectsFindByIdentifier = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_IDENTIFIER);
		this.serviceURLExaminationsBySubjectId = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_EXAMINATIONS_BY_SUBJECT_ID);
		this.serviceURLSubjectsCreate = this.serverURL
				+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_SUBJECTS_CREATE);		
		logger.info("ShanoirUploaderServiceNG successfully initialized.");
	}
	
	public List<Study> findStudiesWithStudyCards() {
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

	public SubjectDTO findSubjectBySubjectIdentifier(String subjectIdentifier) throws Exception {
		HttpResponse response = httpService.get(this.serviceURLSubjectsFindByIdentifier + subjectIdentifier);
		int code = response.getStatusLine().getStatusCode();
		if (code == 200) {
			SubjectDTO subjectDTO = Util.getMappedObject(response, SubjectDTO.class);
			return subjectDTO;
		} else {
			return null;
		}
	}

	public List<ExaminationDTO> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			HttpResponse response = httpService.get(this.serviceURLExaminationsBySubjectId + subjectId);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
//				ResponseHandler<String> handler = new BasicResponseHandler();
//				String body = handler.handleResponse(response);
//				logger.info(body);
				List<ExaminationDTO> examinations = Util.getMappedList(response, ExaminationDTO.class);
				return examinations;
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
	public SubjectDTO createSubject(
			final Long studyId,
			final Long studyCardId,
			final boolean modeSubjectCommonNameManual,
			final SubjectDTO subjectDTO) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(subjectDTO);
			HttpResponse response;
			if (modeSubjectCommonNameManual) {
				response = httpService.post(this.serviceURLSubjectsCreate, json);
			} else {
				response = httpService.post(this.serviceURLSubjectsCreate + "?centerId="+ studyCardId, json);
			}
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				SubjectDTO subjectDTOCreated = Util.getMappedObject(response, SubjectDTO.class);
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
	 * @param studyId
	 * @param subjectId
	 * @param centerId
	 * @param investigatorId
	 * @param examinationDate
	 * @param examinationComment
	 * @return
	 */
	public long createExamination(final Long studyId, final Long subjectId, final Long centerId, final Long investigatorId,
			final Date examinationDate, final String examinationComment) {
		XMLGregorianCalendar examinationDateAsXMLGregorianCalendar = Util.toXMLGregorianCalendar(examinationDate);
		return 1;
	}

}