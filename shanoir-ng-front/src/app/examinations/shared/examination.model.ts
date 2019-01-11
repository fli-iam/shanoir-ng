import { Entity } from "../../shared/components/entity/entity.abstract";
import { IdNameObject } from "../../shared/models/id-name-object.model";
import { SubjectWithSubjectStudy } from "../../subjects/shared/subject.with.subject-study.model";
import { ServiceLocator } from "../../utils/locator.service";
import { ExaminationService } from "./examination.service";

export class Examination extends Entity {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdNameObject;
    subjectStudy: SubjectWithSubjectStudy;
    subject: IdNameObject;
    subjectId: number;
    study: IdNameObject;
    studyId: number;
    studyName: string;
    center: IdNameObject;
    centerId: number;
    centerName: string;
    comment: string;
    note: string;
    subjectWeight: number;
    preclinical: boolean;
    hasStudyCenterData: boolean = false;
    
    service: ExaminationService = ServiceLocator.injector.get(ExaminationService);
}