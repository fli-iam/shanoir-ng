import { Examination } from "../../examinations/shared/examination.model";
import { SubjectStudy } from "../shared/subject-study.model";
import { ImagedObjectCategory } from "../shared/imaged-object-category.enum";

export class Subject {
    examinations: Examination[];
    id: number;
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: "male" | "female";
    subjectStudyList: SubjectStudy[];
}