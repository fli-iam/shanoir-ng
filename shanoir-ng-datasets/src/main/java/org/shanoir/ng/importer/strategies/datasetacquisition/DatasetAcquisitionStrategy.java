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

import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

/**
 * 
 * This Interface is used by all strategies available for creating a new DatasetAcquisition
 *  based on a modality obtained obtained in the serie object sent by the MS IMPORT MicroService (cf ImporterService.class)
 *  
 * example of strategy :
 * 	- MrDatasetAcquisitionStrategy -> Modality MR - Magnetic Resonance
 *  - CtDatasetAcquisitionStrategy -> Modality CT
 *  - MrsDatasetAcquisitionStrategy -> Modality MRS - Magnetic Resonance Spectroscopy
 * 
 * @author atouboul
 *
 */
public interface DatasetAcquisitionStrategy {
	
	// Create a new dataset acquisition 
	DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob);

}