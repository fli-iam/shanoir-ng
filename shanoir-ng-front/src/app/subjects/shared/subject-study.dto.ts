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
import { IdName } from '../../shared/models/id-name.model';
import { Id } from '../../shared/models/id.model';
import { SimpleStudyDTO } from '../../studies/shared/study.dto.model';
import { SubjectStudyTagDTO, Tag } from '../../tags/tag.model';
import { SubjectStudy } from './subject-study.model';
import { SubjectType } from './subject.types';


export class SubjectStudyDTO {
    id: number;
    examinations: number[];
    subject: IdName;
    study: SimpleStudyDTO;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    subjectStudyTags: SubjectStudyTagDTO[];
    tags: Tag[];

    constructor(subjectStudy: SubjectStudy) {
        this.id = subjectStudy.id;
        this.examinations = subjectStudy.examinations ? subjectStudy.examinations.map(exam => exam.id) : null;
        this.subject = subjectStudy.subject ? new IdName(subjectStudy.subject.id, subjectStudy.subject.name) : null;
        this.study = subjectStudy.study ? new SimpleStudyDTO(subjectStudy.study) : null;
        this.subjectStudyIdentifier = subjectStudy.subjectStudyIdentifier;
        this.subjectType = subjectStudy.subjectType;
        this.physicallyInvolved = subjectStudy.physicallyInvolved;
        this.subjectStudyTags = subjectStudy.tags.map(tag => new SubjectStudyTagDTO(new Id(this.id), tag));
    }
}