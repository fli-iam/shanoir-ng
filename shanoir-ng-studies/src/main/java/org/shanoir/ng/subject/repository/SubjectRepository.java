package org.shanoir.ng.subject.repository;

import java.util.Optional;

import org.shanoir.ng.shared.core.repository.CustomRepository;
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
