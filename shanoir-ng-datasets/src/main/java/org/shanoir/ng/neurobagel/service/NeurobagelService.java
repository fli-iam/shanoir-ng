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

package org.shanoir.ng.neurobagel.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Studies exposed to the Neurobagel federation: flagged by an admin (neurobagel_export) and not drafts.
 *
 * The Neurobagel export job has no user behind it and bypasses the per-study rights, so every
 * /neurobagel/study/{studyId}/... endpoint must answer 404 unless {@link #isExported(Long)} is true.
 */
@Service
public class NeurobagelService {

    @Autowired
    private StudyRepository studyRepository;

    public List<NeurobagelStudyDTO> findExportedStudies() {
        return studyRepository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc().stream()
                .map(study -> new NeurobagelStudyDTO(study.getId(), study.getName()))
                .toList();
    }

    public boolean isExported(Long studyId) {
        return studyId != null && studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(studyId);
    }

    public Optional<NeurobagelStudyDTO> findExportedStudy(Long studyId) {
        if (!isExported(studyId)) {
            return Optional.empty();
        }
        return studyRepository.findById(studyId).map(study -> new NeurobagelStudyDTO(study.getId(), study.getName()));
    }

}
