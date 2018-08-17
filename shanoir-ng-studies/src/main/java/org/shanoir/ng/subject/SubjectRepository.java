package org.shanoir.ng.subject;

import java.util.Optional;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Subject.
 *
 * @author msimon
 */
public interface SubjectRepository extends CrudRepository<Subject, Long>, ItemRepositoryCustom<Subject>, SubjectRepositoryCustom {

	/**
	 * Find subject by name.
	 *
	 * @param name
	 *            
	 * @return a Subject.
	 */
	Optional<Subject> findByName(String name);
	
	/**
	 * Find subject by identifier.
	 *
	 * @param identifier
	 *            
	 * @return a Subject.
	 */
	Optional<Subject> findByIdentifier(String identifier);
	
	@Query("SELECT MAX(s.name)  FROM Subject s WHERE s.name LIKE CONCAT(:centerCode,'%','%','%','%')")
	public String find(@Param("centerCode") String centerCode);
}
