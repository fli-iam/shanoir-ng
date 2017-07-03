import { Pipe, PipeTransform } from "@angular/core";

import { ManufacturerModel } from './manufModel.model';

@Pipe({ name: "manufModelLabel" })
export class ManufacturerModelPipe implements PipeTransform {

    transform(manufModel: ManufacturerModel) {
        if (manufModel) {
            return manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                + " (" + manufModel.datasetModalityType + ") - " + manufModel.manufacturer.name;
        }
        return "";
    }

}