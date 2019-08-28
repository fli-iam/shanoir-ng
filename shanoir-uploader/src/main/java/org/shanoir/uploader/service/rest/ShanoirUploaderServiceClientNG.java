package org.shanoir.uploader.service.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.service.rest.dto.SubjectDTO;
import org.shanoir.uploader.utils.Util;

/**
 * 
 * Service layer for org.shanoir.ws.generated.uploader.ShanoirUploaderService.
 *
 * @author mkain
 *
 */
public class ShanoirUploaderServiceClientNG {

	private static Logger logger = Logger.getLogger(ShanoirUploaderServiceClientNG.class);
	
	private static final String SHANOIR_SERVER_URL = "shanoir.server.url";

	private static final String SERVICE_SUBJECTS_FIND_BY_IDENTIFIER = "service.subjects.find.by.identifier";

	private static final String SERVICE_STUDIES_NAMES_CENTERS = "service.studies.names.centers";

	private HttpService httpService;
	
	private String serverURL;
	
	private String serviceURLStudiesNamesAndCenters;
	
	private String serviceURLSubjectsFindByIdentifier;

	public ShanoirUploaderServiceClientNG() {
		this.httpService = new HttpService();
		this.serverURL = ShUpConfig.shanoirNGServerProperties.getProperty(SHANOIR_SERVER_URL);
		this.serviceURLStudiesNamesAndCenters = this.serverURL
			+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_STUDIES_NAMES_CENTERS);
		this.serviceURLSubjectsFindByIdentifier = this.serverURL
				+ ShUpConfig.shanoirNGServerProperties.getProperty(SERVICE_SUBJECTS_FIND_BY_IDENTIFIER);	
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
			List<org.shanoir.ws.generated.uploader.ExaminationDTO> examinations = null;
			List<ExaminationDTO> examinationDTOs = new ArrayList<ExaminationDTO>();
			return examinationDTOs;
		} else {
			return null;
		}
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
	public org.shanoir.ws.generated.uploader.SubjectDTO createSubject(
			final Long studyId,
			final Long studyCardId,
			final boolean modeSubjectCommonName,
			final org.shanoir.ws.generated.uploader.SubjectDTO subjectDTO) {
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