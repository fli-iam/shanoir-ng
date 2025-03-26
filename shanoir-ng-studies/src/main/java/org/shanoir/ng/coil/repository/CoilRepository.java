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

package org.shanoir.ng.coil.repository;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.coil.model.Coil;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for coils.
 *
 * @author msimon
 */
public interface CoilRepository extends CrudRepository<Coil, Long> {
	
	@EntityGraph(attributePaths = { "manufacturerModel.manufacturer" })
	List<Coil> findAll();
	
	@EntityGraph(attributePaths = { "center", "manufacturerModel.manufacturer" })
	Optional<Coil> findById(Long id);
	
	/**
	 * Find coil by name.
	 *
	 * @param name name.
	 * @return a coil.
	 */
	Optional<Coil> findByName(String name);

	List<Coil> findByCenterId(Long centerId);

}
