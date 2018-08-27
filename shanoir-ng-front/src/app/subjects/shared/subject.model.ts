import { Examination } from "../../examinations/shared/examination.model";
import { SubjectStudy } from "./subject-study.model";
import { ImagedObjectCategory } from "./imaged-object-category.enum";
import { Sex } from "./subject.types";
import { IdNameObject } from "../../shared/models/id-name-object.model";

export class Subject {
    examinations: Examination[];
    id: number;
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    subjectStudyList: SubjectStudy[];

    constructor(subject?: IdNameObject) {
        if (subject) {
            this.id = subject.id;
            this.name = subject.name;
        }
    }

    public static makeSubject(id: number, name: string, identifier: string, subjectStudy: SubjectStudy): Subject {
        let subject = new Subject();
        subject.id = id;
        subject.name = name;
        subject.identifier = identifier;
        subject.subjectStudyList = [subjectStudy];
        return subject;
    }
}
