package org.shanoir.uploader.service.soap;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.dto.CenterDTO;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.model.dto.InvestigatorDTO;
import org.shanoir.uploader.model.dto.StudyCardDTO;
import org.shanoir.uploader.model.dto.StudyDTO;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.model.dto.SubjectStudyDTO;
import org.shanoir.uploader.utils.ProxyUtil;
import org.shanoir.uploader.utils.Util;

/**
 * 
 * Service layer for org.shanoir.ws.generated.uploader.ShanoirUploaderService.
 *
 * @author mkain
 *
 */
public class ShanoirUploaderServiceClient {

	private static Logger logger = Logger.getLogger(ShanoirUploaderServiceClient.class);

	private org.shanoir.ws.generated.uploader.ShanoirUploaderService shanoirUploaderService;

	public ShanoirUploaderServiceClient(final String serviceURI, final String serviceLocalPart, final URL serviceURL) {
		final QName qname = new QName(serviceURI, serviceLocalPart);
		final org.shanoir.ws.generated.uploader.ShanoirUploaderService_Service shanoirUploaderService_Service
			= new org.shanoir.ws.generated.uploader.ShanoirUploaderService_Service(serviceURL, qname);
		shanoirUploaderService = shanoirUploaderService_Service.getShanoirUploaderServicePort();
		BindingProvider bindingProvider = (BindingProvider) shanoirUploaderService;
		bindingProvider.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		ProxyUtil.setProxyForBinding(bindingProvider);
		logger.info("ShanoirUploaderService successfully initialized.");
	}

	public boolean login() {
		String userName = ShUpConfig.profileProperties.getProperty("shanoir.server.user.name");
		String password = ShUpConfig.profileProperties.getProperty("shanoir.server.user.password");
		return shanoirUploaderService.login(userName, password);
	}
	
	public boolean login(final String userName, final String password) {
		return shanoirUploaderService.login(userName, password);
	}

	public List<StudyDTO> findStudiesWithStudyCards() {
		List<org.shanoir.ws.generated.uploader.StudyDTO> studyList = shanoirUploaderService.findStudiesWithStudyCards();
		if (studyList != null) {
			List<StudyDTO> studyDTOList = new ArrayList<StudyDTO>();
			for (org.shanoir.ws.generated.uploader.StudyDTO s : studyList) {
				List<CenterDTO> centerList = new ArrayList<CenterDTO>();
				List<StudyCardDTO> studyCardList = new ArrayList<StudyCardDTO>();
				// Centers part
				if (s.getCenters() != null) {
					for (org.shanoir.ws.generated.uploader.CenterDTO c : s.getCenters()) {
						if (c.getInvestigators() != null) {
							List<InvestigatorDTO> investigatorList = new ArrayList<InvestigatorDTO>();
							for (org.shanoir.ws.generated.uploader.InvestigatorDTO i : c.getInvestigators()) {
								investigatorList.add(new InvestigatorDTO(i.getId(), i.getName()));
							}
							centerList.add(new CenterDTO((long) c.getId(), c.getName(), investigatorList));
						} else {
							centerList.add(new CenterDTO((long) c.getId(), c.getName()));
						}
					}
				}
				// StudyCards part
				if (s.getStudyCards() != null) {
					for (org.shanoir.ws.generated.uploader.StudyCardDTO sc : s.getStudyCards()) {
						studyCardList.add(new StudyCardDTO((long) sc.getId(), sc.getName(), (long) sc.getCenterId(),
								sc.getCenterName(), sc.getAcqEquipmentManufacturer(),
								sc.getAcqEquipmentManufacturerModel(), sc.getAcqEquipmentSerialNumber()));
					}
				}
				// Study part
				studyDTOList.add(new StudyDTO((long) s.getId(), s.getName(), studyCardList, centerList));
			}
			return studyDTOList;
		}
		return null;
	}

	public SubjectDTO findSubjectBySubjectIdentifier(String subjectIdentifier) throws Exception {
		org.shanoir.ws.generated.uploader.SubjectDTO subject = shanoirUploaderService
				.findSubjectBySubjectIdentifier(subjectIdentifier);
		if (subject != null) {
			List<SubjectStudyDTO> subjectStudyListDTO = new ArrayList<SubjectStudyDTO>();
			if (subject.getSubjectStudyDTOList() != null) {
				for (org.shanoir.ws.generated.uploader.SubjectStudyDTO ssd : subject.getSubjectStudyDTOList()) {
					subjectStudyListDTO.add(new SubjectStudyDTO(ssd.getId(), ssd.getStudyId(),
							ssd.isPhysicallyInvolved(), ssd.getSubjectStudyIdentifier(), ssd.getSubjectType()));
				}
			}
			SubjectDTO subjectDTO = new SubjectDTO(subject.getId(), subject.getBirthDate(), subject.getName(),
					subject.getSex(), subject.getImagedObjectCategory(), subject.getLanguageHemisphericDominance(),
					subject.getManualHemisphericDominance(), subjectStudyListDTO, subject.getIdentifier());
			subjectDTO.setSubjectStudyList(subjectStudyListDTO);
			return subjectDTO;
		} else {
			return null;
		}
	}

	public List<ExaminationDTO> findExaminationsBySubjectId(Long subjectId) throws Exception {
		if (subjectId != null) {
			List<org.shanoir.ws.generated.uploader.ExaminationDTO> examinations = shanoirUploaderService
					.findExaminationsBySubjectId(subjectId);
			List<ExaminationDTO> examinationDTOs = new ArrayList<ExaminationDTO>();
			for (org.shanoir.ws.generated.uploader.ExaminationDTO e : examinations) {
				examinationDTOs.add(new ExaminationDTO(e.getId(), e.getExaminationDate(), e.getComment()));
			}
			return examinationDTOs;
		} else {
			return null;
		}
	}
	
	public String uploadFile(final String folderName, final File file) throws Exception {
		final FileDataSource fDS = new FileDataSource(file);
		final DataHandler dataHandler = new DataHandler(fDS);
		final String result = shanoirUploaderService.uploadFile(dataHandler, folderName, file.getName());
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
			final Long studyCardId,
			final boolean modeSubjectCommonName,
			final org.shanoir.ws.generated.uploader.SubjectDTO subjectDTO) {
		return shanoirUploaderService.createSubject(studyCardId, modeSubjectCommonName, subjectDTO);
	}
	
	/**
	 * This method puts subjects into studies.
	 * 
	 * @param subjectStudyDTO
	 * @return
	 */
	public org.shanoir.ws.generated.uploader.SubjectStudyDTO createSubjectStudy(
			final org.shanoir.ws.generated.uploader.SubjectStudyDTO subjectStudyDTO) {
		return shanoirUploaderService.createSubjectStudy(subjectStudyDTO);
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
		return shanoirUploaderService.createExamination(studyId, subjectId, centerId, investigatorId, examinationDateAsXMLGregorianCalendar, examinationComment);
	}

}