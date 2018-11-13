import { IdNameObject } from "../../shared/models/id-name-object.model";
import { ExaminationService } from "./examination.service";
import { ServiceLocator } from "../../utils/locator.service";
import { Entity } from "../../shared/components/entity/entity.abstract";

export class Examination extends Entity {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdNameObject;
    subjectId: number;
    subjectName: string;
    subject: IdNameObject;
    studyId: number;
    studyName: string;
    centerId: number;
    centerName: string;
    comment: string;
    note: string;
    subjectWeight: number;
    preclinical: boolean;
    
    service: ExaminationService = ServiceLocator.injector.get(ExaminationService);
}