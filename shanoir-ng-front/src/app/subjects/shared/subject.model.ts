import { Examination } from "../../examinations/shared/examination.model";
import { SubjectStudy } from "../shared/subject-study.model";
import { ImagedObjectCategory } from "../shared/imaged-object-category.enum";

export class Subject {
    examinations: Examination[];
    id: number;
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "left" | "right";
    manualHemisphericDominance: "left" | "right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: "male" | "female";
    subjectStudyList: SubjectStudy[];
}