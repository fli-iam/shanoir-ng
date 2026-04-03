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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.importer.DatasetsCreatorService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.Dataset;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.service.QualityService;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.model.mapper.SerieMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class contains useful methods for Quality Control of series to be imported to the Shanoir server.
 * @author lvallet
 *
 */
public class QualityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(QualityUtils.class);

    private static final QualityService qualityService = new QualityService();

    private static final ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer = new ImagesCreatorAndDicomFileAnalyzerService();

    private static final DatasetsCreatorService datasetsCreatorService = new DatasetsCreatorService();

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
            LOG.info("Quality Control At Import - " + qualityCards.size() + " quality card(s) found for study id " + importJob.getStudyId() + ", " + cardsToCheck.size() + " to be checked at import.");
        }

        // Convert instances to images with parameter isFromShUpQualityControl set to true to keep absolute filepath for the images
        imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(importJob.getPatients(), importJobDir.getAbsolutePath(), isImportFromPACS, null, true);

        for (org.shanoir.ng.importer.model.Patient patient : importJob.getPatients()) {
            List<org.shanoir.ng.importer.model.Study> studies = patient.getStudies();
            for (Iterator<org.shanoir.ng.importer.model.Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
                org.shanoir.ng.importer.model.Study study = studiesIt.next();
                List<Serie> series = study.getSelectedSeries();
                for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
                    Serie serie = seriesIt.next();
                    try {
                        serie.setDatasets(new ArrayList<>());
                        datasetsCreatorService.constructDicom(null, serie, true);
                        org.shanoir.ng.importer.dto.Serie serieDto = SerieMapper.INSTANCE.toDto(serie);
                        AcquisitionAttributes<String> dicomAttributes = DicomProcessing.getDicomAcquisitionAttributes(serieDto);

                        DatasetAcquisition datasetAcquisition = generateDatasetAcquisitionForQualityCheck(study, serie);

                        QualityCardResult serieQualityCardResult = qualityService.checkQuality(datasetAcquisition, dicomAttributes, cardsToCheck);
                        // We retrieve the worst quality tag associated with the datasetAcquisition
                        QualityTag worstTagSet = serieQualityCardResult.getUpdatedDatasetAcquisitions().get(0).getQualityTag();
                        // if quality card result contains an ERROR tag, we remove the serie from the selection
                        if (!serieQualityCardResult.isEmpty() && serieQualityCardResult.hasError()) {
                            serie.setSelected(false);
                            importJob.getSelectedSeries().remove(serie);
                            LOG.info("Quality Control At Import - Serie with description " + serie.getSeriesDescription() + " did not pass quality control and will not be imported.");
                        // Handle the case where the serie passes quality control : we set the quality tag to the serie
                        // Even if a rule with a VALID tag is fulfilled, if a failed valid is found we don't set the VALID tag
                        } else if (!serieQualityCardResult.hasFailedValid() || QualityTag.WARNING.equals(worstTagSet)) {
                            serie.setQualityTag(worstTagSet);
                        }
                        qualityCardResult.merge(serieQualityCardResult);
                    } catch (SecurityException e) {
                        LOG.error(e.getMessage());
                    }
                }
            }
        }
        return qualityCardResult;
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
                // We set two return lines to separate the different quality card entries
                qualityCardReport = qualityCardReport + entry.getMessage() + "\n" + "\n";
            }
        }

        return qualityCardReport;
    }

    public static Long seriesInstanceUIDToLong(String seriesInstanceUID) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(seriesInstanceUID.getBytes(StandardCharsets.UTF_8));
    
        // We take only the first 8 bytes of the hash to convert it to a long value
        ByteBuffer buffer = ByteBuffer.wrap(hash, 0, 8);
        return buffer.getLong();
    }

    private static DatasetAcquisition generateDatasetAcquisitionForQualityCheck(org.shanoir.ng.importer.model.Study study, Serie serie) throws NoSuchAlgorithmException {
        DatasetAcquisition datasetAcquisition = new GenericDatasetAcquisition();
        datasetAcquisition.setId(seriesInstanceUIDToLong(serie.getSeriesInstanceUID()));
        datasetAcquisition.setExamination(new Examination());
        datasetAcquisition.getExamination().setExaminationDate(study.getStudyDate());
        datasetAcquisition.getExamination().setComment(study.getStudyDescription());
        return datasetAcquisition;
    }

}
