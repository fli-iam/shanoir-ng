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

package org.shanoir.ng.examination.dto.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.*;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.mapper.StudyMapper;
import org.shanoir.ng.shared.mapper.SubjectMapper;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;

/**
 * Mapper for examinations.
 *²
 * @author yyao
 *
 */
@Mapper(componentModel = "spring", uses = { ExaminationMapper.class, SubjectMapper.class, StudyMapper.class })
public interface ExaminationMapper {

    ////// Entity to DTO

    @Named("id")
    default Long examinationtoLongId(Examination examination) {
        if (examination == null) {
            return null;
        }
        return examination.getId();
    }

    @Named("id")
    default List<Long> examinationsToLongIds(List<Examination> examinations) {
        if (examinations == null) {
            return null;
        }
        return examinations.stream().filter(Objects::nonNull).map(Examination::getId).collect(Collectors.toList());
    }

    @Named("idOnly")
    default ExaminationDTO examinationToId(Examination examination) {
        if (examination == null) {
            return null;
        }
        ExaminationDTO dto = new ExaminationDTO();
        dto.setId(examination.getId());
        return dto;
    }

    @Named("idOnly")
    default List<ExaminationDTO> examinationsToIds(List<Examination> examinations) {
        if (examinations == null) {
            return null;
        }
        return examinations.stream().filter(Objects::nonNull).map(exam -> {
            ExaminationDTO dto = new ExaminationDTO();
            dto.setId(exam.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    // Single entity

    /**
     /**
     * Some context of usage :
     */
    @Named("nullRelations")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "studyId", expression = "java(null)")
    @Mapping(target = "instrumentBasedAssessmentList", expression = "java(null)")
    ExaminationDTO examinationToExaminationNullRelationsDTO(Examination examination);

    /**
     * Some context of usage :
     * - Loading examination form
     */
    @Named("withCopiesAndSourceAndStudy")
    @Mapping(target = "copies", source = "copies", qualifiedByName = "id")
    @Mapping(target = "source", source = "source", qualifiedByName = "id")
    @Mapping(target = "studyId", source = "study", qualifiedByName = "id")
    @Mapping(target = "instrumentBasedAssessmentList", expression = "java(null)")
    ExaminationDTO examinationToExaminationDTOWithCopiesAndSourceAndStudyId(Examination examination);

    /**
     * Some context of usage :
     * - Loading examination form
     */
    @Named("idRelations")
    @Mapping(target = "copies", source = "copies", qualifiedByName = "id")
    @Mapping(target = "source", source = "source", qualifiedByName = "id")
    @Mapping(target = "studyId", source = "study", qualifiedByName = "id")
    ExaminationDTO examinationToExaminationIdRelationsDTOWithIdRelations(Examination examination);

    /**
     * Some context of usage
     * - Loading tree from lower entity (dataset/acquisition)
     */
    @Named("withStudy")
    @Mapping(target = "copies", expression = "java(null)")
    @Mapping(target = "source", expression = "java(null)")
    @Mapping(target = "instrumentBasedAssessmentList", expression = "java(null)")
    @Mapping(target = "studyId", source = "study", qualifiedByName = "id")
    ExaminationDTO examinationToExaminationDTOWithStudy(Examination examination);

    // Entity list

    /**
     * Some context of usage :
     */
    @IterableMapping(qualifiedByName = "nullRelations")
    List<ExaminationDTO> examinationListToExaminationListNullRelationsDTO(List<Examination> examinations);

    /**
     * Some context of usage :
     */
    @IterableMapping(qualifiedByName = "withCopiesAndSourceAndStudy")
    List<ExaminationDTO> examinationListToExaminationListIdRelationsDTO(List<Examination> examinations);

    ////// DTO to entity

    @Named("idOnly")
    default Examination idToExamination(Long id) {
        if (id == null) {
            return null;
        }
        Examination  examination = new Examination();
        examination.setId(id);
        return examination;
    }

    @Named("idOnly")
    default List<Examination> idsListToExaminationList(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().filter(Objects::nonNull).map(id -> {
            Examination examination = new Examination();
            examination.setId(id);
            return examination;
        }).collect(Collectors.toList());
    }

    // Single entity

    /**
     * Some context of usage :
     */
    @Named("idRelations")
    @Mapping(target = "copies", source = "copies", qualifiedByName = "idOnly")
    @Mapping(target = "source", source = "source", qualifiedByName = "idOnly")
    @Mapping(target = "study", source = "studyId", qualifiedByName = "idOnly")
    Examination examinationDTOToExaminationIdRelations(ExaminationDTO examinationDTO);

    ////// Pageable

    @IterableMapping(qualifiedByName = "idRelations")
    PageImpl<ExaminationDTO> examinationPageToExaminationIdRelationsDTOPage(Page<Examination> page);

    /**
     * Some context of usage :
     * - Populate examinations grid
     */
    @IterableMapping(qualifiedByName = "withStudy")
    PageImpl<ExaminationDTO> examinationListToExaminationListDTOPageWithStudy(Page<Examination> page);
}
