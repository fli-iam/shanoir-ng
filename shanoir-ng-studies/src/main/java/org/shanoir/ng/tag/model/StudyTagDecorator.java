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

package org.shanoir.ng.tag.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class StudyTagDecorator implements StudyTagMapper {

    @Autowired
    private StudyTagMapper delegate;

    @Override
    public StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag) { return delegate.studyTagToStudyTagDTO(studyTag); }

    @Override
    public StudyTag studyTagDTOToStudyTag(StudyTagDTO studyTagDTO) { return delegate.studyTagDTOToStudyTag(studyTagDTO); }

    @Override
    public List<StudyTag> studyTagDTOListToStudyTagList(List<StudyTagDTO> studyTagDTOs) {
        final List<StudyTag> studyTags = new ArrayList<>();
        if (studyTagDTOs != null) {
            for (StudyTagDTO studyTagDTO : studyTagDTOs) {
                studyTags.add(studyTagDTOToStudyTag(studyTagDTO));
            }
        }
        return studyTags;
    }

    @Override
    public List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTagList) {
        final List<StudyTagDTO> studyTagDTOs = new ArrayList<>();
        if (studyTagList != null) {
            for (StudyTag studyTag : studyTagList) {
                studyTagDTOs.add(studyTagToStudyTagDTO(studyTag));
            }
        }
        return studyTagDTOs;
    }
}
