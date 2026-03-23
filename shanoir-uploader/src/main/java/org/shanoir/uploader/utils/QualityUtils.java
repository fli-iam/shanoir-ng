/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.utils;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.DatasetsCreatorService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.model.Dataset;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.importer.service.QualityService;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.model.mapper.SerieMapper;
import org.shanoir.uploader.model.mapper.StudyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains useful methods for Quality Control of series to be imported to the Shanoir server.
 * @author lvallet
 *
 */
public class QualityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(QualityUtils.class);

    private static QualityService qualityService = new QualityService();

    private static ImporterService importerService = new ImporterService();

    private static ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();

    private static DatasetsCreatorService datasetsCreatorService = new DatasetsCreatorService();

    public static QualityCardResult checkQualityAtImport(ImportJob importJob, boolean isImportFromPACS) throws Exception {

        QualityCardResult qualityCardResult = new QualityCardResult();
        List<QualityCard> qualityCards = new ArrayList<>();
        List<QualityCard> cardsToCheck = new ArrayList<>();
        final File importJobDir = new File(importJob.getWorkFolder());

        // Call Shanoir server to get all quality cards for the selected study
        try {
            qualityCards = ShUpOnloadConfig.getShanoirUploaderServiceClient().findQualityCardsByStudyId(importJob.getStudyId());
        } catch (Exception e) {
            LOG.error("Error while retrieving quality cards from server for study " + importJob.getStudyId() + " : " + e.getMessage());
            throw e;
        }

        // If no quality cards are found or none of them are to be checked at importfor the study we skip the quality control
        if (qualityCards == null || qualityCards.isEmpty() || qualityCards.stream().noneMatch(QualityCard::isToCheckAtImport)) {
            LOG.info("Quality Control At Import - No quality cards found or none to be checked at import for study " + importJob.getStudyId());
            return qualityCardResult;
        } else {
            cardsToCheck = qualityCards.stream()
            .filter(QualityCard::isToCheckAtImport)
            .toList();
            LOG.info("Quality Control At Import - " + qualityCards.size() + " quality card(s) found for study " + importJob.getStudyId() + ", " + cardsToCheck.size() + " to be checked at import.");
        }

        // Convert instances to images with parameter isFromShUpQualityControl set to true to keep absolute filepath for the images
        imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(importJob.getPatients(), importJobDir.getAbsolutePath(), isImportFromPACS, null, true);

        // Convert Import ms ImportJob into Datasets ms ImportJob
        //org.shanoir.ng.importer.dto.ImportJob importJobDto = convertImportJob(importJob);

        // Construct Dicom datasets from images
        // for (Patient patient : importJobDto.getPatients()) {
        //     List<Study> studies = patient.getStudies();
        //     for (Iterator<Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
        //         Study study = studiesIt.next();
        //         List<org.shanoir.ng.importer.dto.Serie> series = study.getSelectedSeries();
        //         for (Iterator<org.shanoir.ng.importer.dto.Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
        //             org.shanoir.ng.importer.dto.Serie serie = seriesIt.next();
        //             try {
        //                 serie.setDatasets(new ArrayList<org.shanoir.ng.importer.dto.Dataset>());
        //                 datasetsCreatorService.constructDicom(null, SerieMapper.INSTANCE.toDto(serie), true);
        //                 // TODO : retrieve DICOM attributes from the dto serie but the importJobDto does not contain series
        //                 AcquisitionAttributes<String> dicomAttributes = DicomProcessing.getDicomAcquisitionAttributes(serie);
        //                 qualityCardResult.merge(qualityService.checkQuality(serie, importJobDto, qualityCards));
        //             } catch (SecurityException e) {
        //                 LOG.error(e.getMessage());
        //             }
        //         }
        //     }
        // }

        for (org.shanoir.ng.importer.model.Patient patient : importJob.getPatients()) {
            List<org.shanoir.ng.importer.model.Study> studies = patient.getStudies();
            for (Iterator<org.shanoir.ng.importer.model.Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
                org.shanoir.ng.importer.model.Study study = studiesIt.next();
                List<Serie> series = study.getSelectedSeries();
                int rank = 0;
                for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
                    Serie serie = seriesIt.next();
                    try {
                        serie.setDatasets(new ArrayList<Dataset>());
                        datasetsCreatorService.constructDicom(null, serie, true);
                        org.shanoir.ng.importer.dto.Serie serieDto = SerieMapper.INSTANCE.toDto(serie);
                        AcquisitionAttributes<String> dicomAttributes = DicomProcessing.getDicomAcquisitionAttributes(serieDto);
                        DatasetAcquisition datasetAcquisition = importerService.createDatasetAcquisitionForSerie(serieDto, rank, null, convertImportJob(importJob), dicomAttributes); // Not useful ?
                        qualityCardResult = qualityService.checkQuality(datasetAcquisition, dicomAttributes, cardsToCheck);

                    } catch (SecurityException e) {
                        LOG.error(e.getMessage());
                    }
                    rank++;
                }
            }
        }
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

    // TODO : complete this method to remove from the import job the series that do not pass the quality
    public static ImportJob filterOutSeriesInError(ImportJob importJob, QualityCardResult qualityCardResult) {
        for (QualityCardResultEntry entry : qualityCardResult) {
            if (entry.isError()) {
                String seriesInstanceUID = entry.getSeriesInstanceUID();
                for (org.shanoir.ng.importer.model.Patient patient : importJob.getPatients()) {
                    List<org.shanoir.ng.importer.model.Study> studies = patient.getStudies();
                    for (Iterator<org.shanoir.ng.importer.model.Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
                        org.shanoir.ng.importer.model.Study study = studiesIt.next();
                        List<Serie> series = study.getSelectedSeries();
                        for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
                            Serie serie = seriesIt.next();
                            if (serie.getSeriesInstanceUID().equals(seriesInstanceUID)) {
                                LOG.info("Removing serie with SeriesInstanceUID " + seriesInstanceUID + " from import job because of quality control error.");
                                seriesIt.remove();
                            }
                        }
                    }
                }
            }
        }
        return null;
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

}
