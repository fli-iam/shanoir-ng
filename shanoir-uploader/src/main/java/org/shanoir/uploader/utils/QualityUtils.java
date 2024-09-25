package org.shanoir.uploader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
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
import org.shanoir.ng.utils.Utils;
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
		Set<DatasetAcquisition> generatedAcquisitions = new HashSet<>();
		final File importJobDir = new File(importJob.getWorkFolder());
		int rank = 0;
		
		// Call Shanoir server to get all quality cards for the selected study
		List<QualityCard> qualityCards = ShUpOnloadConfig.getShanoirUploaderServiceClient().findQualityCardsByStudyId(importJob.getStudyId());
		
		// Convert instances to images
		imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(importJob.getPatients(), importJobDir.getAbsolutePath(), false, null, importJob.isFromShanoirUploader());

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

		// datasetsCreatorService.createDatasets(patient, importJobDir, importJob);

		// Convert Import ms ImportJob into Datasets ms ImportJob
		org.shanoir.ng.importer.dto.ImportJob importJobDto = convertImportJob(importJob);

		// for (Patient patient : importJobDto.getPatients()) {
        //     for (Study study : patient.getStudies()) {
        //         for (Serie serie : study.getSelectedSeries() ) {
		// 			AcquisitionAttributes<String> dicomAttributes = null;
        //             try {
        //                 dicomAttributes = DicomProcessing.getDicomAcquisitionAttributes(serie, serie.getIsEnhanced());
        //             } catch (PacsException e) {
        //                 throw new ShanoirException("Unable to retrieve dicom attributes in file " + serie.getFirstDatasetFileForCurrentSerie().getPath(), e);
        //             }
                    
                    // Generate acquisition object with all sub objects : datasets, protocols, expressions, ...
                    //DatasetAcquisition acquisition = ImporterService.createDatasetAcquisitionForSerie(serie, rank, null, importJobDto, dicomAttributes);
					
					// add acq to collection
                    // if (acquisition != null) {
                    //     generatedAcquisitions.add(acquisition);
                    // }
		// 			rank++;
		// 		}
		// 	}
		// }

		examinationData.setStudyId(importJob.getStudyId());
		examinationData.setSubjectStudy(subjectStudy);
		//examinationData.setDatasetAcquisitions(Utils.toList(generatedAcquisitions));

		qualityCardResult = qualityService.checkQuality(examinationData, importJobDto, qualityCards);

		// update SubjectStudy quality tag in ImporterService on server side using quality tag stored in ImportJob and SubjectStudy found with examination already created.
	
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
