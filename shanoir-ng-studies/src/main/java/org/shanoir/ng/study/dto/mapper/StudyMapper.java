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

package org.shanoir.ng.study.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.StudyLightDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.subjectstudy.dto.SubjectStudyDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyCardPolicy;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.timepoint.TimepointMapper;

/**
 * Mapper for studies.
 *
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { TimepointMapper.class })
@DecoratedWith(StudyDecorator.class)
public interface StudyMapper {

	@Named("studies.simple")
	@IterableMapping(qualifiedByName = "study.simple")
	List<StudyDTO> studiesToStudyDTOs(List<Study> studies);

	@Named("study.simple")
	@Mappings({ @Mapping(target = "studyCards", ignore = true), @Mapping(target = "studyCenterList", ignore = true),
		@Mapping(target = "subjectStudyList", ignore = true), @Mapping(target = "tags", ignore = true), @Mapping(target = "studyTags", ignore = true),
		@Mapping(target = "storageVolume", ignore = true), @Mapping(target = "dataUserAgreementPaths", ignore = true),
		@Mapping(target = "protocolFilePaths", ignore = true), @Mapping(target = "timepoints", ignore = true)})
	StudyDTO studyToStudyDTO(Study study);

	@Named("studies.detailed")
	@IterableMapping(qualifiedByName = "study.detailed")
	List<StudyDTO> studiesToStudyDTOsDetailed(List<Study> studies);

	@Named("study.detailed")
	StudyDTO studyToStudyDTODetailed(Study study);

	@Named("studies.light")
	@IterableMapping(qualifiedByName = "study.light")
	List<StudyLightDTO> studiesToStudyLightDTOs(List<Study> studies);

	@Named("study.light")
	@Mapping(target = "studyTags", ignore = true)
	StudyLightDTO studyToStudyLightDTO(Study study);

	@Named("study.light.no.paths")
	@Mappings({
		@Mapping(target = "protocolFilePaths", ignore = true),
		@Mapping(target = "dataUserAgreementPaths", ignore = true)
	})
	StudyLightDTO studyToStudyLightDTONoFilePaths(Study study);

	@Named("studies.idname")
	@IterableMapping(qualifiedByName = "study.idname")
	List<IdNameCenterStudyDTO> studiesToSimpleStudyDTOs(List<Study> studies);

	@Named("study.idname")
	@Mappings({
		@Mapping(target = "studyCenterList", ignore = true),
		@Mapping(target = "tags", ignore = true)
	})
	IdNameCenterStudyDTO studyToExtendedIdNameDTO(Study study);

	IdName studyToIdNameDTO(Study study);

	@Mappings({
		@Mapping(target = "subjectPreclinical", source = "subject.preclinical")
	})
	SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy);

	default Integer map(StudyCardPolicy policy) {
        if (policy == null) return null;
        return policy.getId();
    }

}
