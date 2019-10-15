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

import { Examination } from '../../examinations/shared/examination.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Id } from '../../shared/models/id.model';
import { ServiceLocator } from '../../utils/locator.service';
import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudy, SubjectStudyDTO } from './subject-study.model';
import { SubjectService } from './subject.service';
import { Sex } from './subject.types';

export class Subject extends Entity {

    id: number;
    examinations: Examination[];
    name: string;
    identifier: string;
    birthDate: Date;
    preclinical: boolean;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    selected: boolean = false;
    subjectStudyList: SubjectStudy[] = [];

    public static makeSubject(id: number, name: string, identifier: string, subjectStudy: SubjectStudy): Subject {
        let subject = new Subject();
        subject.id = id;
        subject.name = name;
        subject.identifier = identifier;
        subject.subjectStudyList = [subjectStudy];
        return subject;
    }

    service = ServiceLocator.injector.get(SubjectService);
    
    // Override
    public stringify() {
        return JSON.stringify(new SubjectDTO(this), this.replacer);
    }
}

export interface SimpleSubject {
    id: number;
    name: string;
    identifier: string; 
    subjectStudyList: SubjectStudy[];
}

export class SubjectDTO {

    id: number;
    examinations: Id[];
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    selected: boolean = false;
    subjectStudyList: Id[] = [];
	
    constructor(subject: Subject) {
        this.id = subject.id;
        if (subject.examinations) this.examinations = Id.toIdList(subject.examinations);
        this.name = subject.name;
        this.identifier = subject.identifier;
        this.birthDate = subject.birthDate;
        this.languageHemisphericDominance = subject.languageHemisphericDominance;
        this.manualHemisphericDominance = subject.manualHemisphericDominance;
        this.imagedObjectCategory = subject.imagedObjectCategory;
        this.sex = subject.sex;
        this.selected = subject.selected;
        this.subjectStudyList = subject.subjectStudyList ? subject.subjectStudyList.map(ss => {
            let dto = new SubjectStudyDTO(ss);
            dto.subject = null;
            return dto;
        }) : null;
    }
}
