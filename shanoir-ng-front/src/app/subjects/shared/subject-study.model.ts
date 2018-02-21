import { SubjectType } from "../../subjects/shared/subject-type";
import { SubjectExamination } from "../../examinations/shared/subject-examination.model";
import { ExaminationDTO } from "../../examinations/shared/examinationDTO.model";

export class SubjectStudy {
    id: number;
    examinations: SubjectExamination[];
    examinationDTO: ExaminationDTO[];
    subjectId: number;
    studyId: number;
    subjectStudyIdentifier: string;
    subjectType: SubjectType;
    physicallyInvolved: boolean;
}