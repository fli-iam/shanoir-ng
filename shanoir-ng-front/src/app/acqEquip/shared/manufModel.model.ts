import { Manufacturer } from './manuf.model';
import { DatasetModalityType } from "../../shared/enum/datasetModalityType";

export class ManufacturerModel {
    id: number;
    name: string;
    manufacturer: Manufacturer;
    magneticField: number;
    datasetModalityType: DatasetModalityType;
}