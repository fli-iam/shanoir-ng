import { SubjectType } from "../../subjects/shared/subject-type";
import { SubjectExamination } from "../../examinations/shared/subject-examination.model";

export class SubjectStudy {
    id: number;
    examinations: SubjectExamination[];
    subjectId: number;
    studyId: number;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
}