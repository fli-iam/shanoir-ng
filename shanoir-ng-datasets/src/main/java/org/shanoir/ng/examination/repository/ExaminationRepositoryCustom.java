/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
