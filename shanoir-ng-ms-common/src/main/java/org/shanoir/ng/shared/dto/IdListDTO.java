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

package org.shanoir.ng.shared.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO with list of ids.
 * 
 * @author msimon
 *
 */
public class IdListDTO {

	private List<Long> idList;

	/**
	 * Default constructor.
	 */
	public IdListDTO() {
		this.idList = new ArrayList<>();
	}

	/**
	 * @return the idList
	 */
	public List<Long> getIdList() {
		return idList;
	}

	/**
	 * @param idList
	 *            the idList to set
	 */
	public void setIdList(List<Long> idList) {
		this.idList = idList;
	}

}
