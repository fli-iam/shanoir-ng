package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * Segmentation dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class SegmentationDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2192498115566764115L;

	@Override
	public String getType() {
		return "Segmentation";
	}

}
