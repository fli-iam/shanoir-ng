import { Examination } from "../../examinations/shared/examination.model";

export class Subject {
    id: number;
    examinations: Examination[];
    name: string;
    imagedObjectCategory: string;
    subjectIdentifier: string;
}