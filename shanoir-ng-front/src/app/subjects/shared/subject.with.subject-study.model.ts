import { SubjectStudy } from "./subject-study.model";

export class SubjectWithSubjectStudy {
    id: number;
    name: string;
    identifier: string;
    subjectStudy: SubjectStudy;
}