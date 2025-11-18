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

package org.shanoir.ng.dataset.dto;

/**
 * DTO for dataset download data.
 *
 */
public class DatasetDownloadData {

	private Long id;

	private boolean canDownload;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isCanDownload() {
		return canDownload;
	}

	public boolean getCanDownload() {
		return canDownload;
	}

	public void setCanDownload(boolean canDownload) {
		this.canDownload = canDownload;
	}
}
