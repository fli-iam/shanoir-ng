package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * MR dataset metadata that could be updated by study card.
 * 
 * @author msimon
 *
 */
@Entity
public class MrDatasetMetadata extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2523777086183952849L;

	/** MR Dataset Nature. */
	private Integer mrDatasetNature;

	/**
	 * @return the mrDatasetNature
	 */
	public MrDatasetNature getMrDatasetNature() {
		return MrDatasetNature.getNature(mrDatasetNature);
	}

	/**
	 * @param mrDatasetNature
	 *            the mrDatasetNature to set
	 */
	public void setMrDatasetNature(MrDatasetNature mrDatasetNature) {
		if (mrDatasetNature == null) {
			this.mrDatasetNature = null;
		} else {
			this.mrDatasetNature = mrDatasetNature.getId();
		}
	}

}
