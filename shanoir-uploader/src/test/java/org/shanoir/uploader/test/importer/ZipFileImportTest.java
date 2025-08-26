package org.shanoir.uploader.test.importer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.StudyCenter;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.test.AbstractTest;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ZipFileImportTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(ZipFileImportTest.class);
	
	private static final String IN_PROGRESS = "IN_PROGRESS";

	private static final String ACR_PHANTOM_T1_ZIP = "acr_phantom_t1.zip";

	@Test
	public void testImportWithDicomZipUpload() throws Exception {
		org.shanoir.uploader.model.rest.Study study = createStudyAndCenterAndStudyCard();
		for (int i = 0; i < 0; i++) {
			ImportJob importJob = uploadDicomZip(ACR_PHANTOM_T1_ZIP);
			if (!importJob.getPatients().isEmpty()) {
				selectAllSeriesForImport(importJob);
				org.shanoir.uploader.model.rest.Subject subject = createSubject(importJob, study);
				Long examinationId = createExamination(study, importJob, subject);
				startImportJob(importJob, subject, examinationId, study);
			}
		}
	}

	private Long createExamination(org.shanoir.uploader.model.rest.Study study, ImportJob importJob,
			org.shanoir.uploader.model.rest.Subject subject) {
		Study dicomStudy = importJob.getPatients().get(0).getStudies().get(0);
		LocalDate studyDate = dicomStudy.getStudyDate();
		Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		Date studyDateDate = Date.from(studyDateInstant);
		String examinationComment = dicomStudy.getStudyDescription();
		Examination examination = ImportUtils.createExamination(study, subject, studyDateDate,
			examinationComment, study.getStudyCards().get(0).getCenterId());
		return examination.getId();
	}

	@Test
	public void testImportFromShanoirUploader() throws Exception {
		org.shanoir.uploader.model.rest.Study study = createStudyAndCenterAndStudyCard();
		for (int i = 0; i < 0; i++) {
			// if (!importJob.getPatients().isEmpty()) {
			// 	selectAllSeriesForImport(importJob);
			// 	Subject subject = step2CreateSubject(importJob, study);
			// 	Examination examination = step3CreateExamination(subject);
			// 	step4StartImport(importJob, subject, examination, study);
			// }
		}
	}
	
	private org.shanoir.uploader.model.rest.Study createStudyAndCenterAndStudyCard() {
		org.shanoir.uploader.model.rest.Study study = new org.shanoir.uploader.model.rest.Study();
		final String randomStudyName = "Study-Name-" + UUID.randomUUID().toString();
		study.setName(randomStudyName);
		study.setStudyStatus(IN_PROGRESS);
		// add center to study
		List<StudyCenter> studyCenterList = new ArrayList<StudyCenter>();
		final StudyCenter studyCenter = new StudyCenter();
		Center createdCenter = createCenter();
		Assertions.assertNotNull(createdCenter);
		studyCenter.setCenter(createdCenter);
		studyCenterList.add(studyCenter);
		study.setStudyCenterList(studyCenterList);
		// create study
		study = shUpClient.createStudy(study);
		Assertions.assertNotNull(study);
		// create equipment
		AcquisitionEquipment createdEquipment = createEquipment(createdCenter);
		Assertions.assertNotNull(createdEquipment);
		// create study card and add to study
		StudyCard studyCard = new StudyCard();
		final String randomStudyCardName = "Study-Card-Name-" + UUID.randomUUID().toString();
		studyCard.setName(randomStudyCardName);
		studyCard.setAcquisitionEquipmentId(createdEquipment.getId());
		studyCard.setAcquisitionEquipment(createdEquipment);
		studyCard.setCenterId(createdCenter.getId());
		studyCard.setStudyId(study.getId());
		shUpClient.createStudyCard(studyCard);
		Assertions.assertNotNull(studyCard);
		List<StudyCard> studyCards = new ArrayList<>();
		studyCards.add(studyCard);
		study.setStudyCards(studyCards);
		return study;
	}

	/**
	 * Attention: as we simulate for testing reason, the ZIP upload import
	 * via Web GUI, we add a pseudonymization profile, as the GUI does it.
	 * 
	 * @param importJob
	 * @param subjectREST
	 * @param examination
	 * @param study
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	private void startImportJob(ImportJob importJob, org.shanoir.uploader.model.rest.Subject subjectREST, Long examinationId, org.shanoir.uploader.model.rest.Study study)
			throws JsonProcessingException, Exception {
		importJob.setStudyId(study.getId());
		importJob.setStudyName(study.getName());
		StudyCard studyCard = study.getStudyCards().get(0);
		importJob.setStudyCardId(studyCard.getId());
		importJob.setStudyCardName(studyCard.getName());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
		importJob.setExaminationId(examinationId);
		// Profile Neurinfo
		if (ShUpConfig.isModeSubjectNameManual()) {
			importJob.setAnonymisationProfileToUse("Profile Neurinfo");
		// Profile OFSEP
		} else {
			importJob.setAnonymisationProfileToUse("Profile OFSEP");
		}
		String importJobJson = Util.objectWriter.writeValueAsString(importJob);
		shUpClient.startImportJob(importJobJson);
	}

	private org.shanoir.uploader.model.rest.Subject createSubject(ImportJob importJob, org.shanoir.uploader.model.rest.Study study) throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, ParseException {
		Patient patient = importJob.getPatients().get(0);
		final String randomPatientName = UUID.randomUUID().toString();
		Subject subject = ImportUtils.createSubjectFromPatient(patient, pseudonymizer, identifierCalculator);
		org.shanoir.uploader.model.rest.Subject subjectREST = ImportUtils.manageSubject(
			null, subject, randomPatientName, ImagedObjectCategory.LIVING_HUMAN_BEING,
			HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
			SubjectType.PATIENT, false, false, randomPatientName, study, study.getStudyCards().get(0).getAcquisitionEquipment());
		subject.setImagedObjectCategory(null); // to fix server issue with incompatible mapping value
		org.shanoir.ng.importer.model.Subject subjectForImportJob = new org.shanoir.ng.importer.model.Subject();
		subjectForImportJob.setId(subjectREST.getId());
		subjectForImportJob.setName(subjectREST.getName());
		patient.setSubject(subjectForImportJob);
		importJob.setSubjectName(subjectREST.getName());
		return subjectREST;
	}

	private void selectAllSeriesForImport(ImportJob importJob) {
		List<Patient> patients = importJob.getPatients();
		for (Patient patient : patients) {
			List<Study> studies = patient.getStudies();
			for (Study study : studies) {
				List<Serie> series = study.getSeries();
				for (Serie serie : series) {
					serie.setSelected(true);
				}
			}
		}
	}
	
	private ImportJob uploadDicomZip(final String fileName) {
		try {
		    URL resource = getClass().getClassLoader().getResource(fileName);
		    if (resource != null) {
		        File file = new File(resource.toURI());
		        return shUpClient.uploadDicom(file);
		    }
		} catch (Exception e) {
		    logger.error("Error while reading file: ", e);
		}
		return null;
	}

}
