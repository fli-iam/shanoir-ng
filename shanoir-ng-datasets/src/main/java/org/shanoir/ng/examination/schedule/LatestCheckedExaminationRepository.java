package org.shanoir.ng.examination.schedule;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface LatestCheckedExaminationRepository extends CrudRepository<LatestCheckedExamination, Long> {
	
	Optional<LatestCheckedExamination> findTopByOrderByIdDesc();

}
