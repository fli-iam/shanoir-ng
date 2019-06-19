import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { Study } from '../../studies/shared/study.model';
import { Subject } from './subject.model';
import { SubjectType } from './subject.types';
import { Id } from '../../shared/models/id.model';

export class SubjectStudy {
    id: number;
    examinations: SubjectExamination[];
    subject: Subject;
    study: Study;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
}

export class SubjectStudyDTO {
    id: number;
    examinations: number[];
    subject: Id;
    study: Id;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;

    constructor(subjectStudy: SubjectStudy) {
        this.id = subjectStudy.id;
        this.examinations = subjectStudy.examinations ? subjectStudy.examinations.map(exam => exam.id) : null;
        this.subject = subjectStudy.subject ? new Id(subjectStudy.subject.id) : null;
        this.study = subjectStudy.study ? new Id(subjectStudy.study.id) : null;
        this.subjectStudyIdentifier = subjectStudy.subjectStudyIdentifier;
        this.subjectType = subjectStudy.subjectType;
        this.physicallyInvolved = subjectStudy.physicallyInvolved;
    }
}