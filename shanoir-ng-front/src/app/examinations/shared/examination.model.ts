import { Entity } from "../../shared/components/entity/entity.abstract";
import { IdName } from "../../shared/models/id-name.model";
import { SubjectWithSubjectStudy } from "../../subjects/shared/subject.with.subject-study.model";
import { ServiceLocator } from "../../utils/locator.service";
import { ExaminationService } from "./examination.service";

export class Examination extends Entity {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdName;
    subject: SubjectWithSubjectStudy;
    study: IdName;
    center: IdName;
    comment: string;
    note: string;
    subjectWeight: number;

    service: ExaminationService = ServiceLocator.injector.get(ExaminationService);
}