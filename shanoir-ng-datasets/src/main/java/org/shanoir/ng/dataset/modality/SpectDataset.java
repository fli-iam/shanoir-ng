package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * Spectroscopy dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class SpectDataset extends Dataset {

	/**
	 * UID 
	 */
	private static final long serialVersionUID = -4934855853599640771L;
	
	/** Spect Dataset Nature. */
	private Integer spectDatasetNature;

	/**
	 * @return the spectDatasetNature
	 */
	public SpectDatasetNature getSpectDatasetNature() {
		return SpectDatasetNature.getNature(spectDatasetNature);
	}

	/**
	 * @param spectDatasetNature
	 *            the spectDatasetNature to set
	 */
	public void setSpectDatasetNature(SpectDatasetNature spectDatasetNature) {
		if (spectDatasetNature == null) {
			this.spectDatasetNature = null;
		} else {
			this.spectDatasetNature = spectDatasetNature.getId();
		}
	}

	@Override
	public String getType() {
		return "Spect";
	}

}
