package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.Dataset;

/**
 * CT dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class CtDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1035190618348031062L;

	@Override
	public String getType() {
		return "Ct";
	}

}
