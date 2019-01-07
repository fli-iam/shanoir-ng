import { Pipe, PipeTransform } from "@angular/core";

import { AcquisitionEquipment } from "./acquisition-equipment.model";
import { DatasetModalityType } from "../../shared/enums/dataset-modality-type";
import { ManufacturerModel } from './manufacturer-model.model';

@Pipe({ name: "acqEqptLabel" })
export class AcquisitionEquipmentPipe implements PipeTransform {

    transform(acqEqpt: AcquisitionEquipment) {
        if (acqEqpt && acqEqpt.manufacturerModel) {
            let manufModel: ManufacturerModel = acqEqpt.manufacturerModel;
            return manufModel.manufacturer.name + " - " + manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                + " (" + DatasetModalityType[manufModel.datasetModalityType] + ") " + acqEqpt.serialNumber + " - " + acqEqpt.center.name;
        }
        return "";
    }

}