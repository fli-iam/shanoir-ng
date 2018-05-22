package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;


import org.shanoir.ng.preclinical.therapies.Therapy;
import org.springframework.data.repository.CrudRepository;


public interface SubjectTherapyRepository extends CrudRepository<SubjectTherapy, Long>, SubjectTherapyRepositoryCustom{

	
	List<SubjectTherapy> findByTherapy(Therapy therapy);
}
