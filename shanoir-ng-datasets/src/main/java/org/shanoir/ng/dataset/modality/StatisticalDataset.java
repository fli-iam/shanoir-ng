package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * Statistical dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class StatisticalDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 7175768970653122456L;

	@Override
	public String getType() {
		return "Statistical";
	}

}
