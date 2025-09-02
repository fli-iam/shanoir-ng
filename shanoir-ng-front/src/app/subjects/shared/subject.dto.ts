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
import { Injectable } from '@angular/core';

import { Examination } from '../../examinations/shared/examination.model';
import { Id } from '../../shared/models/id.model';
import { StudyDTOService } from '../../studies/shared/study.dto';
import { Tag } from '../../tags/tag.model';
import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudyDTO } from './subject-study.dto';
import { Subject } from './subject.model';
import {Sex, SubjectType} from './subject.types';
import {formatDate} from "@angular/common";
import {QualityTag} from "../../study-cards/shared/quality-card.model";
import {SimpleStudy, Study} from "../../studies/shared/study.model";


@Injectable()
export class SubjectDTOService {

    constructor(
    ) {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: SubjectDTO, result?: Subject): Promise<Subject> {
        if (!result) result = new Subject();
        SubjectDTOService.mapSyncFields(dto, result);
        return Promise.resolve(result);
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: SubjectDTO[], result?: Subject[]): Promise<Subject[]>{
        if (!result) result = [];
        if (dtos) {
            for (let dto of dtos) {
                let entity = new Subject();
                SubjectDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
        }
        return Promise.resolve(result);
    }

    static mapSyncFields(dto: SubjectDTO, entity: Subject): Subject {
        entity.id = dto.id;
        entity.examinations = dto.examinations ? dto.examinations.map(examId => {
            let exam: Examination = new Examination();
            exam.id = examId.id;
            return exam;
        }) : null;
        entity.name = dto.name;
        entity.identifier = dto.identifier;
        entity.birthDate = dto.birthDate ? new Date(dto.birthDate) : null;
        entity.preclinical = dto.preclinical;
        entity.languageHemisphericDominance = dto.languageHemisphericDominance;
        entity.manualHemisphericDominance = dto.manualHemisphericDominance;
        entity.imagedObjectCategory = dto.imagedObjectCategory;
        entity.sex = dto.sex;
        entity.studyIdentifier = dto.studyIdentifier;
        entity.isAlreadyAnonymized = dto.isAlreadyAnonymized;
        entity.subjectType = dto.subjectType;
        entity.physicallyInvolved = dto.physicallyInvolved;
        entity.tags = dto.tags;
        entity.qualityTag = dto.qualityTag;
        entity.study = new Study()
        entity.study.id = dto.studyId;
        return entity;
    }

    static tagDTOToTag(tagDTO: any): Tag {
        let tag: Tag = new Tag();
        tag.id = tagDTO.id;
        tag.name = tagDTO.name;
        tag.color = tagDTO.color;
        return tag;
    }
}

export class SubjectDTO {

    id: number;
    examinations: Id[];
    name: string;
    identifier: string;
    birthDate: string;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    selected: boolean = false;
    preclinical: boolean;
    studyIdentifier: string;
    isAlreadyAnonymized: boolean = false;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    tags: Tag[];
    qualityTag: QualityTag;
    study: SimpleStudy;
    studyId: number;

    constructor(subject: Subject) {
        this.id = subject.id;
        if (subject.examinations) this.examinations = Id.toIdList(subject.examinations);
        this.name = subject.name;
        this.identifier = subject.identifier;
        if (subject.birthDate && !isNaN(subject.birthDate.getTime())) this.birthDate = formatDate(subject.birthDate, 'yyyy-MM-dd', 'en');
        this.languageHemisphericDominance = subject.languageHemisphericDominance;
        this.manualHemisphericDominance = subject.manualHemisphericDominance;
        this.imagedObjectCategory = subject.imagedObjectCategory;
        this.sex = subject.sex;
        this.selected = subject.selected;
        this.preclinical = subject.preclinical;
        this.studyIdentifier = subject.studyIdentifier;
        this.isAlreadyAnonymized = subject.isAlreadyAnonymized;
        this.subjectType = subject.subjectType;
        this.physicallyInvolved = subject.physicallyInvolved;
        this.tags = subject.tags;
        this.qualityTag = subject.qualityTag;
        this.study = new SimpleStudy(subject.study);
    }
}
