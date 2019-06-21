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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/validation/DatasetsModalityTypeCheckValidator.java
package org.shanoir.ng.datasetacquisition.validation;
=======
package org.shanoir.ng.datasetacquisition;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/DatasetsModalityTypeCheckValidator.java

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.shanoir.ng.dataset.modality.CalibrationDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;

/**
 * Validates if all datasets of an acquisition have same modality type than the
 * acquisition.
 * 
 * @author msimon
 *
 */
public class DatasetsModalityTypeCheckValidator
		implements ConstraintValidator<DatasetsModalityTypeCheck, DatasetAcquisition> {

	@Override
	public void initialize(final DatasetsModalityTypeCheck constraintAnnotation) {
	}

	@Override
	public boolean isValid(final DatasetAcquisition datasetAcquisition, final ConstraintValidatorContext context) {
		if (datasetAcquisition instanceof MrDatasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (!(dataset instanceof MrDataset)) {
					return false;
				}
			}
		} else if (datasetAcquisition instanceof PetDatasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (!(dataset instanceof PetDataset)) {
					return false;
				}
			}
		} else if (datasetAcquisition instanceof CtDatasetAcquisition) {
			for (Dataset dataset : datasetAcquisition.getDatasets()) {
				if (!(dataset instanceof CalibrationDataset)) {
					return false;
				}
			}
		}
		return true;
	}

}
