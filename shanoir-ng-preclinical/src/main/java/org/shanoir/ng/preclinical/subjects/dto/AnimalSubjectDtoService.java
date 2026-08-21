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

package org.shanoir.ng.preclinical.subjects.dto;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.springframework.stereotype.Service;

@Service
public class AnimalSubjectDtoService {

    public AnimalSubject getAnimalSubjectFromAnimalSubjectDto(AnimalSubjectDto dto) {
        AnimalSubject animalSubject = new AnimalSubject();
        animalSubject.setId(dto.getId());
        animalSubject.setBiotype(dto.getBiotype());
        animalSubject.setProvider(dto.getProvider());
        animalSubject.setSpecie(dto.getSpecie());
        animalSubject.setStabulation(dto.getStabulation());
        animalSubject.setStrain(dto.getStrain());
        animalSubject.setSubjectPathologies(getSubjectPathologiesFromDtos(dto));
        animalSubject.setSubjectTherapies(getSubjectTherapiesFromDtos(dto));
        return animalSubject;
    }

    public AnimalSubjectDto getAnimalSubjectDtoFromAnimalSubject(AnimalSubject subject) {
        AnimalSubjectDto dto = new AnimalSubjectDto();
        dto.setId(subject.getId());
        SubjectDto subDto = new SubjectDto();
        subDto.setId(dto.getId());
        subDto.setPreclinical(true);
        dto.setSubject(subDto);
        dto.setBiotype(subject.getBiotype());
        dto.setProvider(subject.getProvider());
        dto.setSpecie(subject.getSpecie());
        dto.setStabulation(subject.getStabulation());
        dto.setStrain(subject.getStrain());
        dto.setSubjectPathologies(getSubjectPathologyDtosFromEntities(subject.getSubjectPathologies()));
        dto.setSubjectTherapies(getSubjectTherapyDtosFromEntities(subject.getSubjectTherapies()));
        return dto;
    }

    private List<SubjectPathology> getSubjectPathologiesFromDtos(AnimalSubjectDto dto) {
        List<SubjectPathology> subjectPathologies = new ArrayList<>();
        if (dto.getSubjectPathologies() != null) {
            for (SubjectPathologyDto pathologyDto : dto.getSubjectPathologies()) {
                SubjectPathology subjectPathology = getSubjectPathologyFromDto(pathologyDto);
                subjectPathologies.add(subjectPathology);
            }
        }
        return subjectPathologies;
    }

    private SubjectPathology getSubjectPathologyFromDto(SubjectPathologyDto dto) {
        SubjectPathology subjectPathology = new SubjectPathology();
        subjectPathology.setId(dto.getId());
        subjectPathology.setPathology(dto.getPathologyModel() != null ? dto.getPathologyModel().getPathology() : null);
        subjectPathology.setPathologyModel(dto.getPathologyModel());
        subjectPathology.setLocation(dto.getLocation());
        subjectPathology.setStartDate(dto.getStartDate());
        subjectPathology.setEndDate(dto.getEndDate());
        return subjectPathology;
    }

    private List<SubjectTherapy> getSubjectTherapiesFromDtos(AnimalSubjectDto dto) {
        List<SubjectTherapy> subjectTherapies = new ArrayList<>();
        if (dto.getSubjectTherapies() != null) {
            for (SubjectTherapyDto therapyDto : dto.getSubjectTherapies()) {
                SubjectTherapy subjectTherapy = getSubjectTherapyFromDto(therapyDto);
                subjectTherapies.add(subjectTherapy);
            }
        }
        return subjectTherapies;
    }

    private SubjectTherapy getSubjectTherapyFromDto(SubjectTherapyDto dto) {
        SubjectTherapy subjectTherapy = new SubjectTherapy();
        subjectTherapy.setId(dto.getId());
        subjectTherapy.setTherapy(dto.getTherapy());
        subjectTherapy.setStartDate(dto.getStartDate());
        subjectTherapy.setEndDate(dto.getEndDate());
        subjectTherapy.setDose(dto.getDose());
        subjectTherapy.setDoseUnit(dto.getDoseUnit());
        subjectTherapy.setFrequency(dto.getFrequency());
        subjectTherapy.setMolecule(dto.getMolecule());
        return subjectTherapy;
    }

    private List<SubjectPathologyDto> getSubjectPathologyDtosFromEntities(List<SubjectPathology> entities) {
        List<SubjectPathologyDto> pathologyDtos = new ArrayList<>();
        if (entities != null) {
            for (SubjectPathology entity : entities) {
                SubjectPathologyDto pathologyDto = getSubjectPathologyDtoFromEntity(entity);
                pathologyDtos.add(pathologyDto);
            }
        }
        return pathologyDtos;
    }

    private SubjectPathologyDto getSubjectPathologyDtoFromEntity(SubjectPathology entity) {
        SubjectPathologyDto dto = new SubjectPathologyDto();
        dto.setId(entity.getId());
        dto.setPathology(entity.getPathology());
        dto.setPathologyModel(entity.getPathologyModel());
        if (entity.getLocation() != null) {
            dto.setLocation(entity.getLocation());
        }
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        return dto;
    }

    private List<SubjectTherapyDto> getSubjectTherapyDtosFromEntities(List<SubjectTherapy> entities) {
        List<SubjectTherapyDto> therapyDtos = new ArrayList<>();
        if (entities != null) {
            for (SubjectTherapy entity : entities) {
                SubjectTherapyDto therapyDto = getSubjectTherapyDtoFromEntity(entity);
                therapyDtos.add(therapyDto);
            }
        }
        return therapyDtos;
    }

    private SubjectTherapyDto getSubjectTherapyDtoFromEntity(SubjectTherapy entity) {
        SubjectTherapyDto dto = new SubjectTherapyDto();
        dto.setId(entity.getId());
        dto.setTherapy(entity.getTherapy());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setDose(entity.getDose());
        dto.setDoseUnit(entity.getDoseUnit());
        dto.setFrequency(entity.getFrequency());
        dto.setMolecule(entity.getMolecule());
        return dto;
    }

}
