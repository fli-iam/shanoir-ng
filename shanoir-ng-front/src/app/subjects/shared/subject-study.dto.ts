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
import { QualityTag } from '../../study-cards/shared/quality-card.model';
import { SubjectStudyTagDTO, Tag } from '../../tags/tag.model';
import { SubjectStudy } from './subject-study.model';
import { SubjectType } from './subject.types';


export class SubjectStudyDTO {
    id: number;
    subject: IdName;
    subjectPreclinical: boolean;
    study: SimpleStudyDTO;
    studyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    subjectStudyTags: SubjectStudyTagDTO[];
    tags: Tag[];
    qualityTag: QualityTag;

    constructor(subjectStudy: SubjectStudy) {
        this.id = subjectStudy.id;
        if(subjectStudy.subject != null){
            this.subject = new IdName(subjectStudy.subject.id, subjectStudy.subject.name);
            this.subjectPreclinical = subjectStudy.subject.preclinical;
        }else{
            this.subject = null;
            this.subjectPreclinical = false;
        }
        this.study = subjectStudy.study ? new SimpleStudyDTO(subjectStudy.study) : null;
        this.studyIdentifier = subjectStudy.studyIdentifier;
        this.subjectType = subjectStudy.subject.subjectType;
        this.physicallyInvolved = subjectStudy.physicallyInvolved;
        this.subjectStudyTags = subjectStudy.tags ? subjectStudy.tags.map(tag => new SubjectStudyTagDTO(new Id(this.id), tag)) : null;
        this.qualityTag = subjectStudy.qualityTag;
    }
}
