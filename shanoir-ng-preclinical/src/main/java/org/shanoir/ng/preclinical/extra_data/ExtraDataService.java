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

package org.shanoir.ng.preclinical.extra_data;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * ExtraData service.
 *
 * @author sloury
 *
 */
public interface ExtraDataService<T> extends UniqueCheckableService {

	/**
	 * Delete an examination extra data.
	 * 
	 * @param id
	 *            examination extra data id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the examination extra datas.
	 * 
	 * @param id
	 *            examination id.
	 * @return a list of examination extra datas.
	 */
	List<T> findAllByExaminationId(Long id);

	/**
	 * Find examination extra data by its id.
	 *
	 * @param id
	 *            examination extra data id.
	 * @return an examination extra data or null.
	 */
	T findById(Long id);

	/**
	 * Save an examination extra data.
	 *
	 * @param examination
	 *            extra data examination extra data to create.
	 * @return created examination extra data.
	 * @throws ShanoirException
	 */
	T save(T extradata) throws ShanoirException;

	/**
	 * Update an examination extra data
	 *
	 * @param examination
	 *            extra data examination extra data to update.
	 * @return updated examination extra data.
	 * @throws ShanoirException
	 */
	T update(T extradata) throws ShanoirException;

}
