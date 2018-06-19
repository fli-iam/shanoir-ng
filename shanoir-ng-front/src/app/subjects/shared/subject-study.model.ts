import { SubjectType } from '../../subjects/shared/subject.types';
import { SubjectExamination } from "../../examinations/shared/subject-examination.model";
import { Subject } from "./subject.model";
import { Study } from "../../studies/shared/study.model";

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
}