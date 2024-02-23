package org.shanoir.ng.examination.schedule;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LatestCheckedExaminationRepository extends JpaRepository<LatestCheckedExamination, Long> {
	
	Optional<LatestCheckedExamination> findTopByOrderByIdDesc();

}
