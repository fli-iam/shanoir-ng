package org.shanoir.ng.datasetacquisition;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.dataset.modality.CalibrationDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.datasetacquisition.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.pet.PetDatasetAcquisition;

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
