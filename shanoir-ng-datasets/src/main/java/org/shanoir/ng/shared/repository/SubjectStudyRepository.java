package org.shanoir.ng.shared.repository;

import java.util.List;

import org.shanoir.ng.shared.model.SubjectStudy;
import org.springframework.data.repository.CrudRepository;

public interface SubjectStudyRepository extends CrudRepository<SubjectStudy, Long>{

	public List<SubjectStudy> findByStudyIdInAndSubjectIdIn(List<Long> studiesId, List<Long> subjectIds);

}
