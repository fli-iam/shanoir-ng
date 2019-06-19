package org.shanoir.ng.center.repository;

import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.IdName;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

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
	public List<IdName> findIdsAndNames(Long studyId);

}
