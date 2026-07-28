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
package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(GenericDatasetAcquisitionStrategy.class);

    @Autowired
    private DatasetStrategy<GenericDataset> genericDatasetStrategy;

    @Override
    public DatasetAcquisition generateDeepDatasetAcquisitionForSerie(String userName, Long subjectId, Serie serie, int rank, AcquisitionAttributes<String> dicomAttributes) throws Exception {
        GenericDatasetAcquisition datasetAcquisition = (GenericDatasetAcquisition) generateFlatDatasetAcquisitionForSerie(
                userName, serie, rank, dicomAttributes.getFirstDatasetAttributes());
        DatasetsWrapper<GenericDataset> datasetsWrapper = genericDatasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, subjectId);
        List<Dataset> genericizedList = new ArrayList<>();
        for (Dataset dataset : datasetsWrapper.getDatasets()) {
            dataset.setDatasetAcquisition(datasetAcquisition);
            genericizedList.add(dataset);
        }
        datasetAcquisition.setDatasets(genericizedList);
        return datasetAcquisition;
    }

    @Override
    public DatasetAcquisition generateFlatDatasetAcquisitionForSerie(String userName, Serie serie, int rank,
            Attributes attributes) throws Exception {
        LOG.info("Generating GenericDatasetAcquisition for: {} - {} - {} - Rank: {}",
                serie.getSeriesDescription(), serie.getProtocolName(),  serie.getSequenceName(), rank);
        GenericDatasetAcquisition datasetAcquisition = new GenericDatasetAcquisition();
        datasetAcquisition.setUsername(userName);
        datasetAcquisition.setImportDate(LocalDate.now());
        datasetAcquisition.setSeriesInstanceUID(serie.getSeriesInstanceUID());
        datasetAcquisition.setRank(rank);
        datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
        datasetAcquisition.setSoftwareRelease(attributes.getString(Tag.SoftwareVersions));
        datasetAcquisition.setAcquisitionStartTime(parseAcquisitionStartTime(attributes));
        return datasetAcquisition;
    }

    /**
     * This strategy is the default one, so it also receives the non-image IODs accepted
     * by DicomUtils.checkSerieIsIgnored: RTSTRUCT, RTDOSE and RTPLAN. Those have no
     * AcquisitionDate/AcquisitionTime (0008,0022 / 0008,0032) at all, as their IOD does
     * not define them, so fall back to the closest equivalent timestamps.
     *
     * The acquisition start time stays optional: when no usable date/time is found, null
     * is returned instead of failing the import, as done by the MR, CT, PET and XA
     * strategies through DicomProcessing.parseAcquisitionStartTime.
     *
     * @param attributes the DICOM attributes of the serie
     * @return the acquisition start time, or null if none could be determined
     */
    private LocalDateTime parseAcquisitionStartTime(Attributes attributes) {
        LocalDateTime acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(
                attributes.getString(Tag.AcquisitionDate), attributes.getString(Tag.AcquisitionTime));
        if (acquisitionStartTime == null) {
            // RTSTRUCT: Structure Set Date/Time (3006,0008 / 3006,0009)
            acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(
                    attributes.getString(Tag.StructureSetDate), attributes.getString(Tag.StructureSetTime));
        }
        if (acquisitionStartTime == null) {
            acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(
                    attributes.getString(Tag.ContentDate), attributes.getString(Tag.ContentTime));
        }
        if (acquisitionStartTime == null) {
            acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(
                    attributes.getString(Tag.SeriesDate), attributes.getString(Tag.SeriesTime));
        }
        return acquisitionStartTime;
    }

    @Override
    public Dataset generateFlatDataset(Serie serie, org.shanoir.ng.importer.dto.Dataset dataset, int datasetIndex, Long subjectId, Attributes attributes) throws Exception {
        return genericDatasetStrategy.generateSingleDataset(attributes, serie, dataset, datasetIndex, subjectId);
    }

}
