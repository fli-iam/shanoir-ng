package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * Template dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class TemplateDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3399415257911069266L;

	/** Template Dataset Nature. */
	private Integer templateDatasetNature;

	/**
	 * @return the templateDatasetNature
	 */
	public TemplateDatasetNature getTemplateDatasetNature() {
		return TemplateDatasetNature.getNature(templateDatasetNature);
	}

	/**
	 * @param templateDatasetNature
	 *            the templateDatasetNature to set
	 */
	public void setTemplateDatasetNature(TemplateDatasetNature templateDatasetNature) {
		if (templateDatasetNature == null) {
			this.templateDatasetNature = null;
		} else {
			this.templateDatasetNature = templateDatasetNature.getId();
		}
	}

	@Override
	public String getType() {
		return "Template";
	}

}
