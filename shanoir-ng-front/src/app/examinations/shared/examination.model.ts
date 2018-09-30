import { IdNameObject } from "../../shared/models/id-name-object.model";
import { ExaminationService } from "./examination.service";
import { ServiceLocator } from "../../utils/locator.service";
import { Entity } from "../../shared/components/entity/entity.interface";

export class Examination {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdNameObject;
    subjectId: number;
    subjectName: string;
    studyId: number;
    studyName: string;
    centerId: number;
    centerName: string;
    comment: string;
    note: string;
    subjectWeight: number;

    private service: ExaminationService = ServiceLocator.injector.get(ExaminationService);

    create(): Promise<Entity> {
        return this.service.create(this);
    }

    update(): Promise<void> {
        return this.service.update(this.id, this);
    }

    delete(): Promise<void> {
        return this.service.delete(this.id);
    }
}