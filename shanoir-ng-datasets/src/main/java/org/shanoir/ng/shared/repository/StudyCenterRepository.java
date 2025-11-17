package org.shanoir.ng.shared.repository;

import java.util.Optional;

import org.shanoir.ng.shared.model.StudyCenter;
import org.springframework.data.repository.CrudRepository;

public interface StudyCenterRepository extends CrudRepository<StudyCenter, Long> {

    Optional<StudyCenter> findByStudyIdCenterId(Long studyId, Long centerId);

}
