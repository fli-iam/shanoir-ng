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

package org.shanoir.ng.center.repository;

import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.IdName;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for centers.
 *
 * @author msimon
 */
public interface CenterRepository extends CrudRepository<Center, Long> {


	Center findByName(String name);
	
	String findNameById(Long id);
	
	@Query("select new org.shanoir.ng.shared.core.model.IdName(c.id, c.name) from Center c")
	public List<IdName> findIdsAndNames();
	
	@Query("select new org.shanoir.ng.shared.core.model.IdName(c.id, c.name) from Center c, StudyCenter sc where sc.center = c and sc.study.id = :studyId")
	public List<IdName> findIdsAndNames(@Param("studyId") Long studyId);

}
