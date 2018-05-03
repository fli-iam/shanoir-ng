import { Examination } from "../../../examinations/shared/examination.model";
import { SubjectStudy } from "../../../subjects/shared/subject-study.model";
import { ImagedObjectCategory } from "../shared/imaged-object-category.enum";
import { Sex } from "../shared/sex.enum";

export class Subject {
    examinations: Examination[];
    id: number;
    name: string;
    preclinical: boolean;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "left" | "right";
    manualHemisphericDominance: "left" | "right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    subjectStudyList: SubjectStudy[];
}