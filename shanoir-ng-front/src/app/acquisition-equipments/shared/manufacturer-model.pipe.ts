import { Pipe, PipeTransform } from "@angular/core";

import { DatasetModalityType } from "../../shared/enums/dataset-modality-type";
import { ManufacturerModel } from './manufacturer-model.model';

@Pipe({ name: "manufModelLabel" })
export class ManufacturerModelPipe implements PipeTransform {

    transform(manufModel: ManufacturerModel) {
        if (manufModel) {
            return manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                + " (" + DatasetModalityType[manufModel.datasetModalityType] + ") - " + manufModel.manufacturer.name;
        }
        return "";
    }

}