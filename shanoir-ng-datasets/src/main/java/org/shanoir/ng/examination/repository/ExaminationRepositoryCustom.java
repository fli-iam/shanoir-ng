package org.shanoir.ng.examination.repository;

import org.shanoir.ng.examination.model.Examination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.List;

public interface ExaminationRepositoryCustom {

	Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable, Boolean preclinical);

	Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable);

	Page<Examination> findPageByStudyCenterOrStudyIdInAndSubjectName(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, String subjectName, Pageable pageable);

	Page<Examination> findPageByStudyCenterOrStudyIdInAndSearch(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable, Boolean preclinical, String searchStr, String searchField);

	List<Examination> findAllByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds);

}
