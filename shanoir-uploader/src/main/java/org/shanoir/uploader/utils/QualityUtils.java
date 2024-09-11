package org.shanoir.uploader.utils;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.model.mapper.StudyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains useful methods for Quality Control of series to be imported to the Shanoir server.
 * @author lvallet
 *
 */
public class QualityUtils {

    private static final Logger logger = LoggerFactory.getLogger(QualityUtils.class);

	private static ImporterService importerService;

	public static QualityCardResult checkQualityAtImport(ImportJob importJob) throws Exception {

		ExaminationData examinationData = new ExaminationData();
		SubjectStudy subjectStudy = new SubjectStudy();
		QualityCardResult qualityCardResult = new QualityCardResult();
		
		// Call Shanoir server to get all quality cards for the selected study
		List<QualityCard> qualityCards = ShUpOnloadConfig.getShanoirUploaderServiceClient().findQualityCardsByStudyId(importJob.getStudyId());

		// Convert Import ms ImportJob into Datasets ms ImportJob
		org.shanoir.ng.importer.dto.ImportJob importJobDto = convertImportJob(importJob);

		examinationData.setStudyId(importJob.getStudyId());
		examinationData.setSubjectStudy(subjectStudy);

		qualityCardResult = importerService.checkQuality(examinationData, importJobDto, qualityCards);

		// update SubjectStudy quality tag in ImporterService on server side using quality tag stored in ImportJob and SubjectStudy found with examination already created.

		// Apply quality card rules on the dicom attributes of importJob.getSelectedSeries()
		// Get dicomAttributes
		// Check quality (w/ datasets method or directly w/ isfulfilled() method)
		// QualityCardResult contains the list of all updated subjectStudies that contains their new qualityTag
		

		return qualityCardResult;
	}

	/**
	 * Convert ImportJob from import ms as used by Shanoir Uploader into Datasets ImportJob needed to call the ImporterService.checkQuality() method
	 * @param importJob
	 * @return
	 */
	private static org.shanoir.ng.importer.dto.ImportJob convertImportJob(ImportJob importJob) {
		org.shanoir.ng.importer.dto.ImportJob importJobDto = new org.shanoir.ng.importer.dto.ImportJob();
		List<Patient> patients = new ArrayList<>();
		Patient patient = new Patient();
		List<Study> studies = new ArrayList<>();
		// Until modifications of ImportUtils.java are done (get rid of Patients List), we browse the DICOM tree
		studies.add(StudyMapper.INSTANCE.toDto(importJob.getPatients().get(0).getStudies().get(0)));
		patient.setStudies(studies);
		patients.add(patient);
		importJobDto.setExaminationId(importJob.getExaminationId());
		importJobDto.setTimestamp(importJob.getTimestamp());
		importJobDto.setFromDicomZip(importJob.isFromDicomZip());
		importJobDto.setFromShanoirUploader(Boolean.TRUE);
		importJobDto.setFromPacs(importJob.isFromPacs());
		importJobDto.setWorkFolder(importJob.getWorkFolder());
		importJobDto.setPatients(patients);
		importJobDto.setUserId(importJob.getUserId());
		importJobDto.setUsername(importJob.getUsername());
		return importJobDto;
	}

	public static String getQualityControlreport(QualityCardResult qualityCardResult) {
		String qualityCardReport = "";

		if (!qualityCardResult.isEmpty()) {
			for (QualityCardResultEntry entry : qualityCardResult) {
				qualityCardReport = qualityCardReport + entry.getMessage() + "\n";
			}
		}

		return qualityCardReport;
	}
}
