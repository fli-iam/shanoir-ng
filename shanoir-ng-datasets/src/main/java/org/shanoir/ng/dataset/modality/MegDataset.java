package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * MEG dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class MegDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 8986396467410158683L;

	@Override
	public String getType() {
		return "Meg";
	}

}
