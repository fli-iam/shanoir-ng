import { IdNameObject } from "../../shared/models/id-name-object.model";
import { CoilType } from "./coil-type.enum";
import { Center } from "../../centers/shared/center.model";
import { ManufacturerModel } from "../../acquisition-equipments/shared/manufacturer-model.model";

export class Coil {
    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;
    coilType:CoilType;
}