/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Pipe, PipeTransform } from "@angular/core";

import { AcquisitionEquipment } from "./acquisition-equipment.model";
import { DatasetModalityType } from "../../enum/dataset-modality-type.enum";
import { ManufacturerModel } from './manufacturer-model.model';

@Pipe({ name: "acqEqptLabel" })
export class AcquisitionEquipmentPipe implements PipeTransform {

    transform(acqEqpt: AcquisitionEquipment): string {
        if (acqEqpt) {
            let manufModel: ManufacturerModel = acqEqpt.manufacturerModel;
            if (manufModel && acqEqpt.center) {
                return manufModel.manufacturer.name + " - " + manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                    + " (???" + DatasetModalityType.getLabel(manufModel.datasetModalityType) + ") " + acqEqpt.serialNumber + " - " + acqEqpt.center.name;
            } else if (acqEqpt.center && acqEqpt.center.name) {
                return acqEqpt.serialNumber + " - " + acqEqpt.center.name;
            } else if (acqEqpt.serialNumber) {
                return acqEqpt.serialNumber
            }
        } 
        return "?";
    }

}