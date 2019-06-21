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

package org.shanoir.ng.subject.repository;

import org.shanoir.ng.subject.model.Subject;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Subject.
 *
 * @author msimon
 */
public interface SubjectRepository extends CrudRepository<Subject, Long>, SubjectRepositoryCustom {

	/**
	 * Find subject by name.
	 *
	 * @param name
	 *            
	 * @return a Subject.
	 */
	Subject findByName(String name);
	
	/**
	 * Find subject by identifier.
	 *
	 * @param identifier
	 *            
	 * @return a Subject.
	 */
	Subject findByIdentifier(String identifier);
	
	
	@Query("SELECT s FROM Subject s WHERE s.name LIKE CONCAT(:centerCode,'%','%','%','%')")
	public Subject findFromCenterCode(@Param("centerCode") String centerCode);
}
