import { IdNameObject } from "../../shared/models/id-name-object.model";

export class StudyCard {
    id: number;
    name: string;
    compatible: boolean;
    center: IdNameObject;
    niftiConverter: IdNameObject;
}