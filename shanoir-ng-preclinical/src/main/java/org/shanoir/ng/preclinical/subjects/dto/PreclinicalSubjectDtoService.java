package org.shanoir.ng.preclinical.subjects.dto;

import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PreclinicalSubjectDtoService {

    public AnimalSubject getAnimalSubjectFromPreclinicalDto(PreclinicalSubjectDto dto) {
        return this.getAnimalSubjectFromAnimalSubjectDto(dto.getAnimalSubject());
    }

    public AnimalSubject getAnimalSubjectFromAnimalSubjectDto(AnimalSubjectDto dto) {
        AnimalSubject animalSubject = new AnimalSubject();
        animalSubject.setSubjectId(dto.getId());
        animalSubject.setBiotype(dto.getBiotype());
        animalSubject.setProvider(dto.getProvider());
        animalSubject.setSpecie(dto.getSpecie());
        animalSubject.setStabulation(dto.getStabulation());
        animalSubject.setStrain(dto.getStrain());
        return animalSubject;
    }



    public PreclinicalSubjectDto getPreclinicalDtoFromAnimalSubject(AnimalSubject subject) {
        PreclinicalSubjectDto dto = new PreclinicalSubjectDto();
        dto.setId(subject.getSubjectId());
        dto.setAnimalSubject(new AnimalSubjectDto());
        dto.getAnimalSubject().setId(dto.getId());
        SubjectDto subDto = new SubjectDto();
        subDto.setId(dto.getId());
        subDto.setPreclinical(true);
        dto.setSubject(subDto);
        dto.getAnimalSubject().setBiotype(subject.getBiotype());
        dto.getAnimalSubject().setProvider(subject.getProvider());
        dto.getAnimalSubject().setSpecie(subject.getSpecie());
        dto.getAnimalSubject().setStabulation(subject.getStabulation());
        dto.getAnimalSubject().setStrain(subject.getStrain());
        return dto;
    }

    public AnimalSubjectDto getAnimalSubjectDtoFromAnimalSubject(AnimalSubject subject) {
        AnimalSubjectDto dto = new AnimalSubjectDto();
        dto.setId(subject.getSubjectId());
        dto.setBiotype(subject.getBiotype());
        dto.setProvider(subject.getProvider());
        dto.setSpecie(subject.getSpecie());
        dto.setStabulation(subject.getStabulation());
        dto.setStrain(subject.getStrain());
        return dto;
    }

    public List<AnimalSubjectDto> getAnimalSubjectDtoListFromAnimalSubjectList(List<AnimalSubject> subjects) {
        List<AnimalSubjectDto> dtos = new ArrayList<>();

        for(AnimalSubject subject : subjects) {
            dtos.add(this.getAnimalSubjectDtoFromAnimalSubject(subject));
        }

        return dtos;

    }

}
