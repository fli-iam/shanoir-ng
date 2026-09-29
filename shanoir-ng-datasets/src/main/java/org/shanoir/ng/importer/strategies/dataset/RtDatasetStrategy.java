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

package org.shanoir.ng.importer.strategies.dataset;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.modality.RtDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetexpression.DatasetExpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RtDatasetStrategy implements DatasetStrategy<RtDataset> {

    @Autowired
    private DatasetExpressionContext datasetExpressionContext;

    @Override
    public DatasetsWrapper<RtDataset> generateDatasetsForSerie(AcquisitionAttributes<String> dicomAttributes, Serie serie,
            Long subjectId) throws Exception {
        DatasetsWrapper<RtDataset> datasetWrapper = new DatasetsWrapper<>();
        int datasetIndex;
        if (serie.getDatasets().size() > 1) {
            datasetIndex = 1;
        } else {
            datasetIndex = -1;
        }

        for (Dataset anyDataset : serie.getDatasets()) {
            RtDataset dataset = generateSingleDataset(
                    dicomAttributes.getDatasetAttributes(anyDataset.getFirstImageSOPInstanceUID()),
                    serie, anyDataset, datasetIndex, subjectId);
            datasetWrapper.getDatasets().add(dataset);
            datasetIndex++;
        }

        return datasetWrapper;
    }

    @Override
    public RtDataset generateSingleDataset(Attributes attributes, Serie serie, Dataset dataset, int datasetIndex,
            Long subjectId) throws Exception {
        RtDataset rtDataset = new RtDataset();
        rtDataset.setSOPInstanceUID(dataset.getFirstImageSOPInstanceUID());
        rtDataset.setCreationDate(serie.getSeriesDate());
        final String seriesDescription = serie.getSeriesDescription();

        DatasetMetadata datasetMetadata = new DatasetMetadata();
        rtDataset.setOriginMetadata(datasetMetadata);
        if (seriesDescription != null && !"".equals(seriesDescription)) {
            rtDataset.getOriginMetadata().setName(computeDatasetName(seriesDescription, datasetIndex));
            rtDataset.getOriginMetadata().setComment(seriesDescription);
        }

        rtDataset.getOriginMetadata().setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
        rtDataset.setSubjectId(subjectId);
        rtDataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.RT_DATASET);

        CardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects;
        if (rtDataset.getSubjectId() != null) {
            refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
        } else {
            refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.MULTIPLE_SUBJECTS_DATASET;
        }
        rtDataset.getOriginMetadata().setCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);

        for (ExpressionFormat expressionFormat : dataset.getExpressionFormats()) {
            datasetExpressionContext.setDatasetExpressionStrategy(expressionFormat.getType());
            DatasetExpression datasetExpression = datasetExpressionContext.generateDatasetExpression(serie, expressionFormat);
            datasetExpression.setDataset(rtDataset);
            rtDataset.getDatasetExpressions().add(datasetExpression);
        }

        DatasetMetadata originalDM = rtDataset.getOriginMetadata();
        rtDataset.setUpdatedMetadata(originalDM);

        return rtDataset;
    }

    @Override
    public String computeDatasetName(String name, int index) {
        if (index == -1) {
            return name;
        } else {
            return name + " " + index;
        }
    }

}
