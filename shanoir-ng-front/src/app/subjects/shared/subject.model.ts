import { Examination } from "../../examinations/shared/examination.model";
import { SubjectStudy } from "../shared/subject-study.model";
import { HemisphericDominance } from "../shared/hemispheric-dominance.enum";
import { Sex } from "../shared/sex.enum";
import { ImagedObjectCategory } from "../shared/imaged-object-category.enum";

export class Subject {
    examinations: Examination[];
    id: number;
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: HemisphericDominance;
    manualHemisphericDominance: HemisphericDominance;
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    subjectStudyList: SubjectStudy[];
}