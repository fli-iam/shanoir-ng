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
import { SimpleStudy } from '../../studies/shared/study.model';
import { QualityTag } from '../../study-cards/shared/quality-card.model';
import { Tag } from '../../tags/tag.model';
import { Subject } from './subject.model';
import { SubjectType } from './subject.types';


export class SubjectStudy {
    id: number;
    examinations: SubjectExamination[];
    subject: Subject;
    subjectId: number;
    study: SimpleStudy;
    studyId: number;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
    tags: Tag[];
    qualityTag: QualityTag;
}
