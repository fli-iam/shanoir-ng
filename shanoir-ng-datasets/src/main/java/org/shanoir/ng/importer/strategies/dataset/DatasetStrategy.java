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
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.Serie;

/**
 * This Interface is used by all strategies available for creating a new Dataset
 * based on a modality obtained obtained in the serie object sent by the MS IMPORT MicroService (cf ImporterService.class)
 * This interface is usually called from a DatasetAcquisitionStrategy
 *
 * <br/>Example of strategy :
 * <br/>- MrDatasetStrategy -> Modality MR - Magnetic Resonance
 * <br/>- CtDatasetStrategy -> Modality CT - Computed TDM
 * <br/>- MrsDatasetStrategy -> Modality MRS - Magnetic Resonance Spectroscopy
 * <br/>- XaDatasetStrategy -> Modality XA - X-Ray Angiography
 *
 * @author atouboul
 *
 */

public interface DatasetStrategy<T extends org.shanoir.ng.dataset.model.Dataset> {

	DatasetsWrapper<T> generateDatasetsForSerie(AcquisitionAttributes<String> dicomAttributes, Serie serie, Long subjectId) throws Exception;

	T generateSingleDataset(Attributes attributes, Serie serie, Dataset dataset, int datasetIndex, Long subjectId) throws Exception;

    String computeDatasetName(String name, int index);

}
