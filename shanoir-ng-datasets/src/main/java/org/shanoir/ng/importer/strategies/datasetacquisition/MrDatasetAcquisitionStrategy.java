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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.BidsDataType;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocolSCMetadata;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.importer.strategies.protocol.MrProtocolStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * MR Dataset Acquisition Strategy used to create new Mr Dataset Acquisition.
 * Called by the ImportService. Requires an importJob
 *
 * Refer to Interface for more information
 *
 * @author atouboul
 *
 */
@Component
public class MrDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MrDatasetAcquisitionStrategy.class);

    @Autowired
    private MrProtocolStrategy mrProtocolStrategy;

    @Autowired
    private DatasetStrategy<MrDataset> mrDatasetStrategy;

    private static final Map<String, BidsDataType> DATA_TYPE_MAPPING;
    static {
        Map<String, BidsDataType> aMap = new HashMap<String, BidsDataType>();
        aMap.put("ANGIO_TIME", BidsDataType.ANAT);
        aMap.put("CINE", BidsDataType.ANAT);
        aMap.put("DIFFUSION", BidsDataType.DWI);
        aMap.put("FLUID_ATTENUATED", BidsDataType.ANAT);
        aMap.put("FMRI", BidsDataType.FUNC);
        aMap.put("MULTIECHO ", BidsDataType.ANAT);
        aMap.put("T1", BidsDataType.ANAT);
        aMap.put("T2", BidsDataType.ANAT);
        aMap.put("T2_STAR", BidsDataType.ANAT);
        DATA_TYPE_MAPPING = Collections.unmodifiableMap(aMap);
    }

    @Override
    public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob, AcquisitionAttributes<String> dicomAttributes) throws Exception {
        MrDatasetAcquisition mrDatasetAcquisition = new MrDatasetAcquisition();
        LOG.info("Generating DatasetAcquisition for   : {} - {} - Rank:{}", serie.getSequenceName(), serie.getProtocolName(), rank);
        mrDatasetAcquisition.setUsername(importJob.getUsername());
        mrDatasetAcquisition.setImportDate(LocalDate.now());
        mrDatasetAcquisition.setRank(rank);
        importJob.getProperties().put(ImportJob.RANK_PROPERTY, String.valueOf(rank));
        mrDatasetAcquisition.setSortingIndex(serie.getSeriesNumber());
        mrDatasetAcquisition.setSoftwareRelease(dicomAttributes.getFirstDatasetAttributes().getString(Tag.SoftwareVersions));
        MrProtocol mrProtocol = mrProtocolStrategy.generateProtocolForSerie(dicomAttributes, serie);
        mrDatasetAcquisition.setMrProtocol(mrProtocol);

        DatasetsWrapper<MrDataset> datasetsWrapper = mrDatasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
        List<Dataset> genericizedList = new ArrayList<>();
        for (Dataset dataset : datasetsWrapper.getDatasets()) {
            dataset.setDatasetAcquisition(mrDatasetAcquisition);
            genericizedList.add(dataset);
        }
        mrDatasetAcquisition.setDatasets(genericizedList);

        // total acquisition time
        if (mrDatasetAcquisition.getMrProtocol().getAcquisitionDuration() == null) {
            Double totalAcquisitionTime = null;
            if (datasetsWrapper.getFirstImageAcquisitionTime() != null && datasetsWrapper.getLastImageAcquisitionTime() != null) {
                Duration duration = Duration.between(datasetsWrapper.getLastImageAcquisitionTime(), datasetsWrapper.getFirstImageAcquisitionTime());
                totalAcquisitionTime = Double.valueOf(duration.toMillis());
                mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(totalAcquisitionTime);
            } else {
                mrDatasetAcquisition.getMrProtocol().setAcquisitionDuration(null);
            }
        }

        LocalDateTime acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(dicomAttributes.getFirstDatasetAttributes().getString(Tag.AcquisitionDate),
                dicomAttributes.getFirstDatasetAttributes().getString(Tag.AcquisitionTime));
        mrDatasetAcquisition.setAcquisitionStartTime(acquisitionStartTime);

        // Can be overridden by study cards
        String imageType = dicomAttributes.getFirstDatasetAttributes().getString(Tag.ImageType, 2);
        if (imageType != null && DATA_TYPE_MAPPING.get(imageType) != null) {
            if (mrDatasetAcquisition.getMrProtocol().getUpdatedMetadata() == null) {
                mrDatasetAcquisition.getMrProtocol().setUpdatedMetadata(new MrProtocolSCMetadata());
            }
            mrDatasetAcquisition.getMrProtocol().getUpdatedMetadata().setBidsDataType(DATA_TYPE_MAPPING.get(imageType));
        }
        return mrDatasetAcquisition;
    }
}
