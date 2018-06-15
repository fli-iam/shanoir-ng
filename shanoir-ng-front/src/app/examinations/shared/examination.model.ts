import { IdNameObject } from "../../shared/models/id-name-object.model";

export class Examination {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdNameObject;
    subjectId: number;
    studyId: number;
    centerId: number;
    comment: string;
    note: string;
    subjectWeight: number;
    preclinical: boolean;
}