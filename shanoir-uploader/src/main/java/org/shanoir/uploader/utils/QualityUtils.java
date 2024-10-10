package org.shanoir.uploader.utils;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.shanoir.ng.importer.DatasetsCreatorService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.model.Dataset;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.service.QualityService;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpConfig;
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

	private static QualityService qualityService = new QualityService();

	private static ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();

	private static DatasetsCreatorService datasetsCreatorService = new DatasetsCreatorService();

	public static QualityCardResult checkQualityAtImport(ImportJob importJob) throws Exception {

		ExaminationData examinationData = new ExaminationData();
		SubjectStudy subjectStudy = new SubjectStudy();
		QualityCardResult qualityCardResult = new QualityCardResult();
		final File importJobDir = new File(importJob.getWorkFolder());
		
		// Call Shanoir server to get all quality cards for the selected study
		List<QualityCard> qualityCards = ShUpOnloadConfig.getShanoirUploaderServiceClient().findQualityCardsByStudyId(importJob.getStudyId());

		// If no quality cards are found for the study we skip the quality control
		if (qualityCards.isEmpty()) {
			logger.info("Quality Control At Import - No quality cards found for study " + importJob.getStudyId());

			return qualityCardResult;
		}
		
		// Convert instances to images with parameter isFromShUpQualityControl set to true to keep absolute filepath for the images
		imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(importJob.getPatients(), importJobDir.getAbsolutePath(), false, null, true);

		// Construct Dicom datasets from images
		for (org.shanoir.ng.importer.model.Patient patient : importJob.getPatients()) {
			List<org.shanoir.ng.importer.model.Study> studies = patient.getStudies();
			for (Iterator<org.shanoir.ng.importer.model.Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
				org.shanoir.ng.importer.model.Study study = studiesIt.next();
				List<Serie> series = study.getSelectedSeries();
				for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
					Serie serie = seriesIt.next();
					try {
						serie.setDatasets(new ArrayList<Dataset>());
						datasetsCreatorService.constructDicom(null, serie, true);
					} catch (SecurityException e) {
						logger.error(e.getMessage());
					}
				}
			}
		}

		// Convert Import ms ImportJob into Datasets ms ImportJob
		org.shanoir.ng.importer.dto.ImportJob importJobDto = convertImportJob(importJob);

		examinationData.setStudyId(importJob.getStudyId());
		// Set an Id to the subjectStudy to retrieve the qualityTag
		subjectStudy.setId(importJob.getSubject().getId());
		examinationData.setSubjectStudy(subjectStudy);

		qualityCardResult = qualityService.checkQuality(examinationData, importJobDto, qualityCards);
	
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
				//We set two return lines to separate the different quality card entries
				qualityCardReport = qualityCardReport + entry.getMessage() + "\n" + "\n";
			}
		}

		return qualityCardReport;
	}

	public static JScrollPane getQualityControlreportScrollPane(QualityCardResult qualityControlResult) {
		String message = new String();
		if (qualityControlResult.hasError()) {
			message = ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.failed.message");
		} else if (qualityControlResult.hasWarning() || qualityControlResult.hasFailedValid()) {
			message = ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.warning.message");
		}
		JTextArea textArea = new JTextArea(message + getQualityControlreport(qualityControlResult));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
		textArea.setPreferredSize(new Dimension(800, 300));
		JScrollPane scrollPane = new JScrollPane(textArea);

		return scrollPane;
	}
}
