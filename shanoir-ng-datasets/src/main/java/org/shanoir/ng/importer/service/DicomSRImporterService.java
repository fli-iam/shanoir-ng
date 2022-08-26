package org.shanoir.ng.importer.service;

import java.io.InputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
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

	@Autowired
	private StudyInstanceUIDHandler studyInstanceUIDHandler;
	
	@Autowired
	private ExaminationRepository examinationRepository;
	
	@Autowired
	private DICOMWebService dicomWebService;
	
	public boolean importDicomSR(InputStream inputStream) {
		try (DicomInputStream dIS = new DicomInputStream(inputStream)) {
			Attributes attributes = dIS.readDatasetUntilPixelData();
			// check for modality: DICOM SR
			// replace artificial examinationUID with real StudyInstanceUID in dicom server
			String examinationUID = attributes.getString(Tag.StudyInstanceUID);
			String studyInstanceUID = studyInstanceUIDHandler.findStudyInstanceUIDFromCacheOrDatabase(examinationUID);
			attributes.setString(Tag.StudyInstanceUID, VR.valueOf(Tag.StudyInstanceUID), studyInstanceUID);
			// get examination from db: to complete subject name
			Long examinationID = Long.valueOf(studyInstanceUID.substring(StudyInstanceUIDHandler.PREFIX.length() + 1));
			Examination examination = examinationRepository.findById(examinationID).get();
			Long subjectId = examination.getSubjectId();
			String patientName = attributes.getString(Tag.PatientName);
			dicomWebService.sendDicomInputStreamToPacs(inputStream);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return true;
	}
}
