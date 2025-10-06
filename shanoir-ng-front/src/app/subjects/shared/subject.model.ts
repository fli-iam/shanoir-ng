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
import {Tag} from "../../tags/tag.model";
import {QualityTag} from "../../study-cards/shared/quality-card.model";
import {SimpleStudy, Study} from "../../studies/shared/study.model";

import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudy } from './subject-study.model';
import {Sex, SubjectType} from './subject.types';


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
    studyIdentifier: string;
    isAlreadyAnonymized: boolean = false;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    tags: Tag[];
    qualityTag: QualityTag;
    study: Study;

    public static makeSubject(id: number, name: string, identifier: string, study: SimpleStudy): Subject {
        const subject = new Subject();
        subject.id = id;
        subject.name = name;
        subject.identifier = identifier;
        subject.study = new Study()
        subject.study.id = study.id;
        return subject;
    }
}

export class SimpleSubject {
    id: number;
    name: string;
    identifier: string;
    subjectStudyList: SubjectStudy[];
    study: SimpleStudy;

    constructor(subject: Subject) {
        this.id = subject.id ? subject.id : null;
        this.name = subject.name;
        this.identifier = subject.studyIdentifier;
        this.subjectStudyList = null;
        this.study = subject.study;
    }
}
