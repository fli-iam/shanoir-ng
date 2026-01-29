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

package org.shanoir.ng.dataset.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.shanoir.ng.dataset.modality.*;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.DatasetType;
import org.shanoir.ng.datasetfile.DatasetFile;

public final class DatasetUtils {

    private DatasetUtils() { }

    /**
     * Reads all dataset files depending on the format attached to one dataset.
     * @param dataset
     * @param pathURLs
     * @throws MalformedURLException
     */
    public static void getDatasetFilePathURLs(final Dataset dataset, List<URL> pathURLs, DatasetExpressionFormat format) throws MalformedURLException {
        List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
        for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
            DatasetExpression datasetExpression = (DatasetExpression) itExpressions.next();
            if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
                List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
                for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
                    DatasetFile datasetFile = (DatasetFile) itFiles.next();
                    URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
                    pathURLs.add(url);
                }
            }
        }
    }


    public static Dataset buildDatasetFromType(String type) {

        DatasetMetadata originMetadata = new DatasetMetadata();
        Dataset dataset = null;

        switch (type) {
            case DatasetType.Names.CALIBRATION:
                dataset = new CalibrationDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.CALIBRATION_DATASET);
                break;
            case DatasetType.Names.CT:
                dataset = new CtDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.CT_DATASET);
                break;
            case DatasetType.Names.EEG:
                dataset = new EegDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
                break;
            case DatasetType.Names.MEG:
                dataset = new MegDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.MEG_DATASET);
                break;
            case DatasetType.Names.MESH:
                dataset = new MeshDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.MESH_DATASET);
                break;
            case DatasetType.Names.MR:
                dataset = new MrDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
                break;
            case DatasetType.Names.PARAMETER_QUANTIFICATION:
                dataset = new ParameterQuantificationDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.PARAMETER_QUANTIFICATION_DATASET);
                break;
            case DatasetType.Names.PET:
                dataset = new PetDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.PET_DATASET);
                break;
            case DatasetType.Names.REGISTRATION:
                dataset = new RegistrationDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.REG_DATASET);
                break;
            case DatasetType.Names.SEGMENTATION:
                dataset = new SegmentationDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.SEG_DATASET);
                break;
            case DatasetType.Names.SR:
                dataset = new SrDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.SR_DATASET);
                break;
            case DatasetType.Names.SPECT:
                dataset = new SpectDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.SPECT_DATASET);
                break;
            case DatasetType.Names.STATISTICAL:
                dataset = new StatisticalDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.STATISTICAL_DATASET);
                break;
            case DatasetType.Names.TEMPLATE:
                dataset = new TemplateDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.TEMPLATE_DATASET);
                break;
            case DatasetType.Names.BIDS:
                dataset = new BidsDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
                break;
            case DatasetType.Names.XA:
                dataset = new XaDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.XA_DATASET);
                break;
            case DatasetType.Names.MEASUREMENT:
                dataset = new MeasurementDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.MEASUREMENT_DATASET);
                break;
            default:
                dataset = new GenericDataset();
                originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
                break;
        }
        dataset.setOriginMetadata(originMetadata);
        return dataset;
    }

    public static Dataset copyDatasetFromDataset(Dataset d) {
        DatasetType type = d.getType();

        return switch (type) {
            case CALIBRATION -> new CalibrationDataset(d);
            case CT -> new CtDataset(d);
            case EEG -> new EegDataset(d);
            case MEG -> new MegDataset(d);
            case MESH -> new MeshDataset(d);
            case PARAMETER_QUANTIFICATION -> new ParameterQuantificationDataset(d);
            case PET -> new PetDataset(d);
            case REGISTRATION -> new RegistrationDataset(d);
            case SEGMENTATION -> new SegmentationDataset(d);
            case SPECT -> new SpectDataset(d);
            case STATISTICAL -> new StatisticalDataset(d);
            case TEMPLATE -> new TemplateDataset(d);
            case BIDS -> new BidsDataset(d);
            case XA -> new XaDataset(d);
            default -> new GenericDataset(d);
        };
    }
}
