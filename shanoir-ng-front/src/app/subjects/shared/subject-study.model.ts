import { SubjectType } from './subject.types';
import { SubjectExamination } from "../../examinations/shared/subject-examination.model";
import { Subject } from "./subject.model";
import { Study } from "../../studies/shared/study.model";

export class SubjectStudy {
    id: number;
    examinations: SubjectExamination[];
    subject: Subject;
    study: Study;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
}