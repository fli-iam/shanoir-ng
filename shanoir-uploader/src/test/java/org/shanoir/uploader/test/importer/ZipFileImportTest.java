package org.shanoir.uploader.test.importer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.StudyCenter;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.test.AbstractTest;
import org.shanoir.uploader.utils.Util;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ZipFileImportTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(ZipFileImportTest.class);
	
	private static final String PROFILE_NEURINFO = "Profile Neurinfo";

	private static final String IN_PROGRESS = "IN_PROGRESS";

	private static final String ACR_PHANTOM_T1_ZIP = "acr_phantom_t1.zip";

	@Test
	public void testImportWithDicomZipUpload() throws Exception {
		org.shanoir.uploader.model.rest.Study study = createStudyAndCenterAndStudyCard();
		for (int i = 0; i < 1; i++) {
			ImportJob importJob = step1UploadDicom(ACR_PHANTOM_T1_ZIP);
			if (!importJob.getPatients().isEmpty()) {
				selectAllSeriesForImport(importJob);
				Subject subject = step2CreateSubject(importJob, study);
				Examination examination = step3CreateExamination(subject);
				step4StartImport(importJob, subject, examination, study);
			}
		}
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

	private void createSubjectStudy(org.shanoir.uploader.model.rest.Study study, Subject subject) {
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(new IdName(study.getId(), study.getName()));
		subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));
		subjectStudy.setSubjectStudyIdentifier(subject.getName());
		subjectStudy.setSubjectType(SubjectType.PHANTOM);
		subjectStudy.setPhysicallyInvolved(true);
		subject.getSubjectStudyList().add(subjectStudy);
		shUpClient.createSubjectStudy(subject);
	}

	private void step4StartImport(ImportJob importJob, Subject subject, Examination examination, org.shanoir.uploader.model.rest.Study study)
			throws JsonProcessingException, Exception {
		importJob.setStudyId(study.getId());
		importJob.setStudyName(study.getName());
		StudyCard studyCard = study.getStudyCards().get(0);
		importJob.setStudyCardId(studyCard.getId());
		importJob.setStudyCardName(studyCard.getName());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipment().getId());
		importJob.setSubjectName(subject.getName());
		importJob.setExaminationId(examination.getId());
		importJob.setAnonymisationProfileToUse(PROFILE_NEURINFO); // yes we are in ShUp, but use the standard import API
		String importJobJson = Util.objectWriter.writeValueAsString(importJob);
		shUpClient.startImportJob(importJobJson);
	}

	private Examination step3CreateExamination(Subject subject) {
		Examination examination = new Examination();
		examination.setStudyId(subject.getSubjectStudyList().get(0).getStudy().getId());
		examination.setSubjectId(subject.getId());
		examination.setCenterId(Long.valueOf(1));
		examination.setExaminationDate(new Date());
		examination.setComment("ExamOfSubject"+subject.getName());
		return shUpClient.createExamination(examination);
	}

	private Subject step2CreateSubject(ImportJob importJob, org.shanoir.uploader.model.rest.Study study) {
		Patient patient = importJob.getPatients().get(0);
		Subject subject = new Subject();
		final String randomPatientName = UUID.randomUUID().toString();
		subject.setName(randomPatientName);
		subject.setIdentifier(randomPatientName);
		subject.setBirthDate(patient.getPatientBirthDate());
		if (patient.getPatientSex().compareTo(Sex.F.name()) == 0) {
			subject.setSex(Sex.F);
		} else if (patient.getPatientSex().compareTo(Sex.M.name()) == 0) {
			subject.setSex(Sex.M);
		} else {
			subject.setSex(Sex.O);
		}
		subject.setLanguageHemisphericDominance(HemisphericDominance.Left);
		subject.setManualHemisphericDominance(HemisphericDominance.Left);
		subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);
		subject.setSubjectStudyList(new ArrayList<SubjectStudy>());
		subject = shUpClient.createSubject(subject, true, Long.valueOf(1));
		createSubjectStudy(study, subject);
		subject.setImagedObjectCategory(null); // to fix server issue with incompatible mapping value
		org.shanoir.ng.importer.model.Subject subjectImportJob = new org.shanoir.ng.importer.model.Subject();
		subjectImportJob.setId(subject.getId());
		subjectImportJob.setName(subject.getName());
		patient.setSubject(subjectImportJob);
		return subject;
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
	
	private ImportJob step1UploadDicom(final String fileName) {
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
