package org.shanoir.ng.examination.schedule;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ExaminationLastCheckedRepository extends CrudRepository<ExaminationLastChecked, Long> {

    Optional<ExaminationLastChecked> findTopByOrderByIdDesc();

}
