import { IdNameObject } from "../../../shared/models/id-name-object.model";

export class Examination {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdNameObject;
    subject: IdNameObject;
    studyId: number;
    studyName: string;
    centerId: number;
    centerName: string;
    comment: String;
    note: String;
    subjectWeight: number;
    preclinical: boolean;
}