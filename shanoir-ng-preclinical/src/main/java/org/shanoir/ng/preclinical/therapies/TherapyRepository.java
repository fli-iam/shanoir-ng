package org.shanoir.ng.preclinical.therapies;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface TherapyRepository extends CrudRepository<Therapy, Long>, TherapyRepositoryCustom{
	
	Optional<Therapy> findByName(String name);
	
	List<Therapy> findByTherapyType(TherapyType therapyType);

}
