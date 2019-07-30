package org.shanoir.uploader.service;

import java.io.File;
import java.util.List;

import org.shanoir.uploader.model.ExportData;
import org.shanoir.uploader.model.dto.EquipmentDicom;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.model.dto.StudyDTO;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.service.wsdl.ShanoirUploaderServiceClient;


public interface IWebService {
	
	Integer init();
	
	Integer testConnection();
	
	ShanoirUploaderServiceClient getShanoirUploaderService(); // remove and add login method
	
	WebServiceResponse<List<StudyDTO>> findStudies(EquipmentDicom equipment);
	
	
	/**
	 * 
	 * @param exportData
	 * @return WebServiceResponse
	 * Case OK :  WebServiceResponse.statusCode = 0
	 * Case WebService not accessible : WebServiceResponse.statusCode = -1 or -2
	 * Case Login Error : WebServiceResponse.statusCode = -3
	 * Case adding subject to study Error :  WebServiceResponse.statusCode = -4
	 * Case COMMON_NAME_ALREADY_USE Error :  WebServiceResponse.statusCode = -5
	 * Case SUBJECT_PERSIST_FAIL Error :  WebServiceResponse.statusCode = -6
	 * 
	 */
	WebServiceResponse<Long> createSubject(ExportData exportData);
	
	/**
	 * 
	 * @param exportData
	 * @return WebServiceResponse
	 * Case OK :  WebServiceResponse.statusCode = 0
	 * Case WebService not accessible : WebServiceResponse.statusCode = -1 or -2
	 * Case Login Error : WebServiceResponse.statusCode = -3
	 * Case adding subject to study Error :  WebServiceResponse.statusCode = -4
	 * Case COMMON_NAME_ALREADY_USE Error :  WebServiceResponse.statusCode = -5
	 * Case SUBJECT_PERSIST_FAIL Error :  WebServiceResponse.statusCode = -6
	 * Case UNABLE_TO_RETRIEVE_SUBJECT :  WebServiceResponse.statusCode = -7
	 * 
	 */
	WebServiceResponse<Long> updateSubject(ExportData exportData);
	
	WebServiceResponse<Long> createExamination(ExportData exportData, Long subjectId);
	
	WebServiceResponse<SubjectDTO> findSubjectByIdentifier(String subjectIdentifier);
	
	WebServiceResponse<List<ExaminationDTO>> findExaminationsBySubjectId(Long subjectId);
	
	void uploadFile(File file, File folder) throws Exception;

}
