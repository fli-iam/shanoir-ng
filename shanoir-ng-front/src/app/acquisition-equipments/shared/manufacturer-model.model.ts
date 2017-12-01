import { Manufacturer } from './manufacturer.model';
import { DatasetModalityType } from "../../shared/enums/dataset-modality-type";

export class ManufacturerModel {
    id: number;
    name: string;
    manufacturer: Manufacturer;
    magneticField: number;
    datasetModalityType: DatasetModalityType;
}