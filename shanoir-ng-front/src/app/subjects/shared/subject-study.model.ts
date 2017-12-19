import { SubjectType } from "../../subjects/shared/subject-type";
import { SubjectExamination } from "../../examinations/shared/subject-examination.model";

export class SubjectStudy {
    examinations: SubjectExamination[];
    subjectId: number;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
}