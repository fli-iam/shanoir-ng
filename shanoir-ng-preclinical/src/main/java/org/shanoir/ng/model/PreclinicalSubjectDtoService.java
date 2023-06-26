package org.shanoir.ng.model;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PreclinicalSubjectDtoService {

    public AnimalSubject getAnimalSubjectFromDto(PreclinicalSubjectDto dto) {
        AnimalSubject animalSubject = new AnimalSubject();
        animalSubject.setId(dto.getAnimalSubject().getId());
        animalSubject.setBiotype(dto.getAnimalSubject().getBiotype());
        animalSubject.setProvider(dto.getAnimalSubject().getProvider());
        animalSubject.setSpecie(dto.getAnimalSubject().getSpecie());
        animalSubject.setStabulation(dto.getAnimalSubject().getStabulation());
        animalSubject.setStrain(dto.getAnimalSubject().getStrain());
        return animalSubject;
    }

    public PreclinicalSubjectDto getPreclinicalDtoFromAnimalSubject(AnimalSubject subject) {
        PreclinicalSubjectDto dto = new PreclinicalSubjectDto();
        dto.setId(subject.getId());
        dto.setAnimalSubject(new AnimalSubjectDto());
        dto.getAnimalSubject().setId(dto.getId());
        dto.setSubject(new SubjectDto(dto.getId()));
        dto.getAnimalSubject().setBiotype(subject.getBiotype());
        dto.getAnimalSubject().setProvider(subject.getProvider());
        dto.getAnimalSubject().setSpecie(subject.getSpecie());
        dto.getAnimalSubject().setStabulation(subject.getStabulation());
        dto.getAnimalSubject().setStrain(subject.getStrain());
        return dto;
    }

    public AnimalSubjectDto getAnimalSubjectDtoFromAnimalSubject(AnimalSubject subject) {
        AnimalSubjectDto dto = new AnimalSubjectDto();
        dto.setId(subject.getId());
        dto.setBiotype(subject.getBiotype());
        dto.setProvider(subject.getProvider());
        dto.setSpecie(subject.getSpecie());
        dto.setStabulation(subject.getStabulation());
        dto.setStrain(subject.getStrain());
        return dto;
    }

    public List<AnimalSubjectDto> getAnimalDtoListFromAnimalSubjectList(List<AnimalSubject> subjects) {
        List<AnimalSubjectDto> dtos = new ArrayList<>();

        for(AnimalSubject subject : subjects){
            dtos.add(this.getAnimalSubjectDtoFromAnimalSubject(subject));
        }

        return dtos;

    }

}
