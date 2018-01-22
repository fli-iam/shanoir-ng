import { IdNameObject } from "../../shared/models/id-name-object.model";
import { CoilType } from "./coil-type.enum";

export class Coil {
    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: IdNameObject;
    manufacturerModel: IdNameObject;
    coilType:CoilType;

}