package org.shanoir.uploader.utils;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.model.mapper.StudyMapper;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains useful methods for Quality Control of series to be imported to the Shanoir server.
 * @author lvallet
 *
 */
public class QualityUtils {

    private static final Logger logger = LoggerFactory.getLogger(QualityUtils.class);

	private static ShanoirUploaderServiceClient shanoirUploaderServiceClient;

	private static DicomProcessing dicomProcessing;

	private static ImporterService importerService;

	private static StudyMapper studyMapper;
    

	public static QualityCardResult checkQualityAtImport(ImportJob importJob) throws Exception {

		QualityCardResult qualityCardResult = new QualityCardResult();
		
		// Call Shanoir server to get all quality cards for the selected study
		List<QualityCard> qualityCards = shanoirUploaderServiceClient.findQualityCardsByStudyId(importJob.getStudyId());

		// Convert Import ms ImportJob into Datasets ms ImportJob
		org.shanoir.ng.importer.dto.ImportJob importJobDto = convertImportJob(importJob);

		qualityCardResult = importerService.checkQuality(null, importJobDto, qualityCards);


		// use ImporterService.checkQuality() directly, convert SHUP Examination into ExaminationData (or let ExamData = null for now and check only with DicomMetadata) and Import ImportJob into Datasets ImportJob (mapstruct in shup)

		// update SubjectStudy quality tag in ImporterService on server side using quality tag stored in ImportJob and SubjectStudy found with examination already created.

		// Apply quality card rules on the dicom attributes of importJob.getSelectedSeries()
		// Get dicomAttributes
		// Check quality (w/ datasets method or directly w/ isfulfilled() method)
		// QualityCardResult contains the list of all updated subjectStudies that contain their new qualityTag

			// for (QualityCard qualityCard : qualityCards) {
			// 	//If the qualitycard is to be checked at import, then execute quality control
			// 	if (qualityCard.isToCheckAtImport()) {
			// 		logger.info("Quality card " + qualityCard.getId() + " is to be checked at import for the study " + importJob.getStudyId());
			// 		org.shanoir.ng.importer.dto.ImportJob importJobDtst = new org.shanoir.ng.importer.dto.ImportJob();

			// 		ExaminationAttributes<String> dicomAttributes = dicomProcessing.getDicomExaminationAttributes(importJob.getFirstStudy());
					
			// 	} else {
			// 	}
			// }
		

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
		studies.add(studyMapper.toDto(importJob.getStudy()));
		patient.setStudies(studies);
		patients.add(patient);
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
}
