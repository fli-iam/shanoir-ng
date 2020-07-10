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
 * mkain: For the method findSubjectFromCenterCode I would have preferred to use
 * JPQL or Spring Data Jpa with method names. Both did not work for me: 1) JPQL
 * does not support "limit 1". With JPQL I would have to use Pageable to implement
 * the same, but as I want to give back one subject, I see no sense in asking the
 * caller to work with PageRequest(0,1), what looks strange too. 2) With Spring
 * Data Jpa I was not able to combine findFirstByOrderDesc and StartsWith, that
 * is why I am using nativeQuery here.
 *
 * @author msimon
 * @author mkain
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
	
	@Query(value = "SELECT * FROM subject WHERE name LIKE :centerCode ORDER BY name DESC LIMIT 1", nativeQuery = true)
	Subject findSubjectFromCenterCode(@Param("centerCode") String centerCode);
	
	/**
	 * Returns all instances of the type.
	 * 
	 * @return all entities
	 */
	Iterable<Subject> findBySubjectStudyListStudyIdIn(Iterable<Long> studyIds);

}
