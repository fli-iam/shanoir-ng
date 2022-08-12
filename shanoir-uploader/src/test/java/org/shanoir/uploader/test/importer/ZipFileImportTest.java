package org.shanoir.uploader.test.importer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.model.rest.importer.Patient;
import org.shanoir.uploader.model.rest.importer.Serie;
import org.shanoir.uploader.model.rest.importer.Study;
import org.shanoir.uploader.test.AbstractTest;

public class ZipFileImportTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(ZipFileImportTest.class);
	
	@Test
	public void importDicomZipTest() throws Exception {
		ImportJob importJob = step1UploadDicom("acr_phantom_t1.zip");
		if (CollectionUtils.isNotEmpty(importJob.getPatients())) {
			selectAllSeriesForImport(importJob);
			for (int i = 0; i < 1000; i++) {
				Subject subject = step2CreateSubject(importJob);				
				Examination examination = step3CreateExamination(subject);
			}
		}
	}

	private Examination step3CreateExamination(Subject subject) {
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
		}			
		subject.setLanguageHemisphericDominance(HemisphericDominance.Left);
		subject.setManualHemisphericDominance(HemisphericDominance.Left);
		subject.setSubjectStudyList(new ArrayList<SubjectStudy>());
		return shUpClient.createSubject(subject, true, Long.valueOf(1));
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
		    logger.error("Error while reading file", e);
		}
		return null;
	}

}
