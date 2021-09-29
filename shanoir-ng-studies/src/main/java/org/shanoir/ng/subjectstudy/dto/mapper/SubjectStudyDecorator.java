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

package org.shanoir.ng.subjectstudy.dto.mapper;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.tag.model.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Decorator for link between a subject and a study.
 * 
 * @author msimon
 *
 */
public class SubjectStudyDecorator implements SubjectStudyMapper {

	@Autowired
	private SubjectStudyMapper delegate;
	
	@Autowired
	private TagMapper tagMapper;

	@Override
	public List<SubjectStudyDTO> subjectStudyListToSubjectStudyDTOList(List<SubjectStudy> subjectStudies) {
		final List<SubjectStudyDTO> subjectStudyDTOs = new ArrayList<>();
		if (subjectStudies != null) {
			for (SubjectStudy subjectStudy : subjectStudies) {
				subjectStudyDTOs.add(subjectStudyToSubjectStudyDTO(subjectStudy));
			}
		}
		return subjectStudyDTOs;
	}

	@Override
	public SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy) {
		final SubjectStudyDTO subjectStudyDTO = delegate.subjectStudyToSubjectStudyDTO(subjectStudy);
		if (!StringUtils.isEmpty(subjectStudy.getSubjectStudyIdentifier())) {
			subjectStudyDTO.setSubjectStudyIdentifier(subjectStudy.getSubjectStudyIdentifier());
		}
		subjectStudyDTO.setTags(tagMapper.tagListToTagDTOList(subjectStudy.getTags()));

		return subjectStudyDTO;
	}

}
