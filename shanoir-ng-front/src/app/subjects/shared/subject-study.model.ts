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

import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { Study } from '../../studies/shared/study.model';
import { Subject } from './subject.model';
import { SubjectType } from './subject.types';
import { Id } from '../../shared/models/id.model';
import { Tag } from '../../tags/tag.model';

export class SubjectStudy {
    id: number;
    examinations: SubjectExamination[];
    subject: Subject;
    subjectId: number;
    study: Study;
    studyId: number;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    tags: Tag[];
    availableTags: Tag[];
}

export class SubjectStudyDTO {
    id: number;
    examinations: number[];
    subject: Id;
    study: Id;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    tags: Tag[];
    availableTags: Tag[];

    constructor(subjectStudy: SubjectStudy) {
        this.id = subjectStudy.id;
        this.examinations = subjectStudy.examinations ? subjectStudy.examinations.map(exam => exam.id) : null;
        this.subject = subjectStudy.subject ? new Id(subjectStudy.subject.id) : null;
        this.study = subjectStudy.study ? new Id(subjectStudy.study.id) : null;
        this.subjectStudyIdentifier = subjectStudy.subjectStudyIdentifier;
        this.subjectType = subjectStudy.subjectType;
        this.physicallyInvolved = subjectStudy.physicallyInvolved;
        this.tags = subjectStudy.tags;
        this.availableTags = subjectStudy.availableTags;
    }
}