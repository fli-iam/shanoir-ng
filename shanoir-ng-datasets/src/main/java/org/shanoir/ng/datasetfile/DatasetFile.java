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

package org.shanoir.ng.datasetfile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Dataset file.
 * 
 * @author msimon
 *
 */
@Entity
public class DatasetFile extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3272282965762717831L;

	/** The dataset expression. */
	@ManyToOne
	@JoinColumn(name = "dataset_expression_id")
	private DatasetExpression datasetExpression;

	private boolean pacs;

	@Column(columnDefinition = "TEXT")
	private String path;

	/**
	 * @return the datasetExpression
	 */
	public DatasetExpression getDatasetExpression() {
		return datasetExpression;
	}

	/**
	 * @param datasetExpression
	 *            the datasetExpression to set
	 */
	public void setDatasetExpression(DatasetExpression datasetExpression) {
		this.datasetExpression = datasetExpression;
	}

	/**
	 * Return the wado request with the mime-type set as image/jpeg.
	 *
	 * @return the jpeg path
	 */
	@Transient
	public String getJpegPath() {
		return getPath().replaceFirst("application/dicom", "image/jpeg");
	}

	/**
	 * Return the wado request with the mime-type set as image/jpeg. Only used
	 * for multiframe.
	 *
	 * @return the jpeg path
	 */
	@Transient
	public String getJpegPath(final int frameNumber) {
		if (getDatasetExpression().isMultiFrame()) {
			return getJpegPath() + "&frameNumber=" + frameNumber;
		} else {
			return getJpegPath();
		}
	}

	/**
	 * @return the pacs
	 */
	public boolean isPacs() {
		return pacs;
	}

	/**
	 * @param pacs
	 *            the pacs to set
	 */
	public void setPacs(boolean pacs) {
		this.pacs = pacs;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
