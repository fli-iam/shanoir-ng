package org.shanoir.uploader.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.ExportData;
import org.shanoir.uploader.model.dto.EquipmentDicom;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.model.dto.StudyDTO;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.model.dto.SubjectRemoteResultDTO;
import org.shanoir.uploader.service.wsdl.ServiceConfiguration;
import org.shanoir.uploader.service.wsdl.ShanoirUploaderServiceClient;

/**
 * @author atouboul
 *
 */
public class SoapWebService implements IWebService {

	/** Instance unique pré-initialisée */
	private static SoapWebService INSTANCE = new SoapWebService();

	private static Logger logger = Logger.getLogger(SoapWebService.class);

	private ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();

	private ShanoirUploaderServiceClient shanoirUploaderService;

	public static IWebService getInstance() {
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.uploader.service.WebServiceItf#init()
	 */
	public Integer init() {
		serviceConfiguration.setUsername(ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.user.name"));
		serviceConfiguration
				.setPassword(ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.user.password"));
		serviceConfiguration.setShanoirUploaderServiceURI(
				ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.uploader.service.qname.namespace.uri"));
		serviceConfiguration.setShanoirUploaderServiceLocalPart(
				ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.uploader.service.qname.local.part"));
		URL lURL = null;
		try {
			lURL = new URL(ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.uploader.service.url"));
		} catch (MalformedURLException e) {
			logger.error("Property defined in shanoir.server.uploader.service.url (File shanoir_server.properties in .su folder) is not properly configured", e);
			return -3;
		}
		serviceConfiguration.setShanoirUploaderServiceURL(lURL);
		try {
			this.shanoirUploaderService = new ShanoirUploaderServiceClient(serviceConfiguration.getShanoirUploaderServiceURI(),
					serviceConfiguration.getShanoirUploaderServiceLocalPart(), serviceConfiguration.getShanoirUploaderServiceURL());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return -3;
		}
		return 0;
	}

	public ShanoirUploaderServiceClient getShanoirUploaderService() {
		return shanoirUploaderService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.uploader.service.WebServiceItf#testConnection()
	 */
	public Integer testConnection() {
		try {
			Boolean loginSuccess;
			try {
				loginSuccess = shanoirUploaderService.login(serviceConfiguration.getUsername(),
						serviceConfiguration.getPassword());
			} catch (Exception e1) {
				logger.error("proxy issue", e1);
				return -2;
			}
			if (!loginSuccess) {
				return -1;
			} else {
				return 0;
			}
		} catch (WebServiceException e2) {
			logger.error("web service issue", e2);
			return -3;
		} catch (Exception e) {
			logger.error(ShUpConfig.resourceBundle
					.getString("shanoir.uploader.systemErrorDialog.error.wsdl.credentialService"), e);
			return -1;
		}
	}

	public WebServiceResponse<List<StudyDTO>> findStudies(EquipmentDicom equipment) {
		WebServiceResponse<List<StudyDTO>> response = new WebServiceResponse<List<StudyDTO>>();
		try {
			response.setObj(shanoirUploaderService.findStudiesWithStudyCards());
		} catch (Exception e) {
			response.setStatusCode(-1);
			response.setStatus(e.toString());
		}
		return response;
	}

	/**
	 * 
	 * @param exportData
	 * @return WebServiceResponse Case OK : WebServiceResponse.statusCode = 0 Case
	 *         WebService not accessible : WebServiceResponse.statusCode = -1 or -2
	 *         Case Login Error : WebServiceResponse.statusCode = -3 Case adding
	 *         subject to study Error : WebServiceResponse.statusCode = -4 Case
	 *         COMMON_NAME_ALREADY_USE Error : WebServiceResponse.statusCode = -5
	 *         Case SUBJECT_PERSIST_FAIL Error : WebServiceResponse.statusCode = -6
	 * 
	 */
	public WebServiceResponse<Long> createSubject(ExportData exportData) {
		WebServiceResponse<Long> response = new WebServiceResponse<Long>();
		SubjectRemoteResultDTO subjectRemoteResult = null;

		try {
//			shanoirUploaderService.createSubject(true, studyId, studyCardId,
//					subjectDTO.getName(), subjectDTO.getIdentifier(), subjectDTO.getBirthDate(), subjectDTO.getSex(),
//					subjectDTO.getImagedObjectCategory(), subjectDTO.getLanguageHemisphericDominance(), subjectDTO.getManualHemisphericDominance(),
//					refSubjectTypeId, subjectStudyIdentifier, isPhysicallyInvolved);
		} catch (Exception e1) {
			response.setStatusCode(-1);
			response.setStatus(e1.toString());
			return response;
		}
		response.setStatus(subjectRemoteResult.getResult());
		if (subjectRemoteResult.getResult().equals("SUCCESS")) {
			response.setObj(subjectRemoteResult.getSubject().getId());
		} else {
			if (subjectRemoteResult.getResult().equals("LOGIN_FAIL")) {
				response.setStatusCode(-3);
			} else if (subjectRemoteResult.getResult().equals("ADD_SUBJECT_TO_STUDY_FAIL")) {
				response.setStatusCode(-4);
			} else if (subjectRemoteResult.getResult().equals("COMMON_NAME_ALREADY_USE")) {
				response.setStatusCode(-5);
			} else if (subjectRemoteResult.getResult().equals("SUBJECT_PERSIST_FAIL")) {
				response.setStatusCode(-6);
			}
		}
		return response;
	}
	
	public org.shanoir.ws.generated.uploader.SubjectDTO createSubject(final Long studyId, final Long studyCardId, final boolean modeSubjectCommonName,
			final org.shanoir.ws.generated.uploader.SubjectDTO subjectDTO) {
		return shanoirUploaderService.createSubject(studyId, studyCardId, modeSubjectCommonName, subjectDTO);
	}

	public WebServiceResponse<Long> createExamination(ExportData exportData, Long subjectId) {
		WebServiceResponse<Long> response = new WebServiceResponse<Long>();
		ExaminationDTO examination = null;
		try {
//			examination = entityCreatorService.createExaminationFromShup(exportData, subjectId);
		} catch (Exception e1) {
			response.setStatusCode(-1);
			response.setStatus(e1.toString());
			return response;
		}
		response.setObj(examination.getId());
		return response;
	}

	public WebServiceResponse<SubjectDTO> findSubjectByIdentifier(String subjectIdentifier) {
		WebServiceResponse<SubjectDTO> response = new WebServiceResponse<SubjectDTO>();
		SubjectDTO subject = null;
		try {
			subject = shanoirUploaderService.findSubjectBySubjectIdentifier(subjectIdentifier);
		} catch (Exception e) {
			response.setStatusCode(-1);
			response.setStatus(e.toString());
			return response;
		}
		response.setObj(subject);
		return response;
	}

	public WebServiceResponse<List<ExaminationDTO>> findExaminationsBySubjectId(Long subjectId) {
		WebServiceResponse<List<ExaminationDTO>> response = new WebServiceResponse<List<ExaminationDTO>>();
		List<ExaminationDTO> examinationDTOs = null;
		try {
			examinationDTOs = shanoirUploaderService.findExaminationsBySubjectId(subjectId);
		} catch (Exception e) {
			response.setStatusCode(-1);
			response.setStatus(e.toString());
			return response;
		}
		response.setObj(examinationDTOs);
		response.setStatus("SUCCESS");
		return response;
	}

	public void uploadFile(File folder, File file) throws Exception {
		String result = shanoirUploaderService.uploadFile(folder.getName(), file);
		if (!"200".equals(result)) {
			logger.error(result);
			throw new Exception("File upload error occured!");
		}
	}

	public WebServiceResponse<Long> updateSubject(ExportData exportData) {
		return null;
	}

}
