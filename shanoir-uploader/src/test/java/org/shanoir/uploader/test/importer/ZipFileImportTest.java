package org.shanoir.uploader.test.importer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.model.rest.importer.Patient;
import org.shanoir.uploader.model.rest.importer.Serie;
import org.shanoir.uploader.model.rest.importer.Study;
import org.shanoir.uploader.test.AbstractTest;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ZipFileImportTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(ZipFileImportTest.class);
	
	@Test
	public void importDicomZipTest() throws Exception {
		org.shanoir.uploader.model.rest.Study study = new org.shanoir.uploader.model.rest.Study();
		study.setId(Long.valueOf(1));
		study.setName("DemoStudy");
		for (int i = 0; i < 100; i++) {
			ImportJob importJob = step1UploadDicom("acr_phantom_t1.zip");
			if (CollectionUtils.isNotEmpty(importJob.getPatients())) {
				selectAllSeriesForImport(importJob);
				Subject subject = step2CreateSubject(importJob);
				importJob.getPatients().get(0).setPatientBirthDate(null);
				step3CreateSubjectStudy(study, subject);
				Examination examination = step4CreateExamination(subject);
				step5StartImport(importJob, subject, examination);
			}
		}
	}

	private void step3CreateSubjectStudy(org.shanoir.uploader.model.rest.Study study, Subject subject) {
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(new IdName(study.getId(), study.getName()));
		subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));
		subjectStudy.setSubjectStudyIdentifier(subject.getName());
		subjectStudy.setSubjectType(SubjectType.PHANTOM);
		subjectStudy.setPhysicallyInvolved(true);
		subject.getSubjectStudyList().add(subjectStudy);
		shUpClient.createSubjectStudy(subject);
	}

	private void step5StartImport(ImportJob importJob, Subject subject, Examination examination)
			throws JsonProcessingException, Exception {
		importJob.setStudyId(Long.valueOf(1));
		importJob.setStudyCardId(Long.valueOf(1));
		importJob.setStudyCardName("StudyCard1");
		importJob.setAcquisitionEquipmentId(Long.valueOf(1));
		importJob.setSubjectName(subject.getName());
		importJob.setExaminationId(examination.getId());
		importJob.setConverterId(Long.valueOf(6));
		String importJobJson = Util.objectWriter.writeValueAsString(importJob);
		shUpClient.startImportJob(importJobJson);
	}

	private Examination step4CreateExamination(Subject subject) {
		Examination examination = new Examination();
		examination.setStudyId(Long.valueOf(1));
		examination.setSubjectId(subject.getId());
		examination.setCenterId(Long.valueOf(1));
		examination.setExaminationDate(new Date());
		examination.setComment("ExamOfSubject"+subject.getName());
		return shUpClient.createExamination(examination);
	}

	private Subject step2CreateSubject(ImportJob importJob) {
		Patient patient = importJob.getPatients().get(0);
		final Subject subject = new Subject();
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
		return shUpClient.createSubject(subject, true, Long.valueOf(1));
	}

	private void selectAllSeriesForImport(ImportJob importJob) {
		List<Patient> patients = importJob.getPatients();
		for (Patient patient : patients) {
			List<Study> studies = patient.getStudies();
			for (Study study : studies) {
				study.setStudyDate(null);
				List<Serie> series = study.getSeries();
				for (Serie serie : series) {
					serie.setSelected(true);
					serie.setSeriesDate(null);
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
		    logger.error("Error while reading file", e);
		}
		return null;
	}

}
