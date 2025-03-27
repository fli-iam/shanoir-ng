package org.shanoir.ng.shared.repository;

import java.util.List;

import org.shanoir.ng.shared.model.SubjectStudy;
import org.springframework.data.repository.CrudRepository;

public interface SubjectStudyRepository extends CrudRepository<SubjectStudy, Long> {

	public List<SubjectStudy> findByStudy_IdInAndSubjectIdIn(List<Long> studiesId, List<Long> subjectIds);

	public List<SubjectStudy> findByStudy_Id(Long studyId);

	public List<SubjectStudy> findByStudy_IdAndSubjectId(Long studyId, Long subjectId);

}
