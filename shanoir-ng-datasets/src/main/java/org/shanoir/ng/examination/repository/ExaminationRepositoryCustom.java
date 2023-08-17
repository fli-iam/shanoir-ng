package org.shanoir.ng.examination.repository;

import java.util.List;

import org.shanoir.ng.examination.model.Examination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

public interface ExaminationRepositoryCustom {
	
	Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable, Boolean preclinical);

	Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable);
	
	Page<Examination> findPageByStudyCenterOrStudyIdInAndSubjectName(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, String subjectName, Pageable pageable);
	
	List<Examination> findAllByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds);
	
}
