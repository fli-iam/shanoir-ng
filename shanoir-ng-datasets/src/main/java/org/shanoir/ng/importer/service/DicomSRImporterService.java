package org.shanoir.ng.importer.service;

import java.io.InputStream;
import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author mkain
 *
 */
@Service
public class DicomSRImporterService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DicomSRImporterService.class);

	private static final String SR = "SR";

	@Autowired
	private StudyInstanceUIDHandler studyInstanceUIDHandler;
	
	@Autowired
	private ExaminationRepository examinationRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private DICOMWebService dicomWebService;
	
	public boolean importDicomSR(InputStream inputStream) {
		try (DicomInputStream dIS = new DicomInputStream(inputStream)) {
			Attributes attributes = dIS.readDatasetUntilPixelData();
			// check for modality: DICOM SR
			if (SR.equals(attributes.getString(Tag.Modality))) {
				// replace artificial examinationUID with real StudyInstanceUID in DICOM server
				String examinationUID = attributes.getString(Tag.StudyInstanceUID);
				String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
				attributes.setString(Tag.StudyInstanceUID, VR.valueOf(Tag.StudyInstanceUID), studyInstanceUID);
				// complete subject name, that is sent by the viewer wrongly with P-0000001 etc.
				Long examinationID = Long.valueOf(studyInstanceUID.substring(StudyInstanceUIDHandler.PREFIX.length() + 1));
				Examination examination = examinationRepository.findById(examinationID).get();
				Optional<Subject> subjectOpt = subjectRepository.findById(examination.getSubjectId());
				String subjectName = "error_subject_name_not_found_in_db";
				if (subjectOpt.isPresent()) {
					subjectName = subjectOpt.get().getName();
				}
				attributes.setString(Tag.PatientName, VR.valueOf(Tag.PatientName), subjectName);
				attributes.setString(Tag.PatientID, VR.valueOf(Tag.PatientID), subjectName);
				// set user name, as person, who created the measurement
				final String userName = KeycloakUtil.getTokenUserName();
				attributes.setString(Tag.PersonName, VR.valueOf(Tag.PersonName), userName);
				dicomWebService.sendDicomInputStreamToPacs(inputStream);
			} else {
				LOG.error("Error: importDicomSR: other modality sent then SR.");
				return false;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return true;
	}
}
