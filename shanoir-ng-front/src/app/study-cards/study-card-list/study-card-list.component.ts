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

import { Component, ViewChild } from '@angular/core';

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { StudyCard } from '../shared/study-card.model';
import { StudyCardService } from '../shared/study-card.service';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { DatasetModalityType } from '../../shared/enums/dataset-modality-type';


@Component({
    selector: 'study-card-list',
    templateUrl: 'study-card-list.component.html',
    styleUrls: ['study-card-list.component.css'],
})
export class StudyCardListComponent extends BrowserPaginEntityListComponent<StudyCard> {
    
    @ViewChild('table') table: TableComponent;

    constructor(
            private studyCardService: StudyCardService,
            private acqEqptLabelPipe: AcquisitionEquipmentPipe) {
                
        super('study-card');
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    getEntities(): Promise<StudyCard[]> {
        return this.studyCardService.getAll();
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            { headerName: "Name", field: "name" },
            { headerName: "Study", field: "study.name"},
            { headerName: "Center", field: "acquisitionEquipment.center.name"},
            { headerName: "Equipment", field: "acquisitionEquipment", width: '200%',
                cellRenderer: params => this.format(params.data.acquisitionEquipment) }
        ];
        return colDef;       
    }

    format(acqEqpt: AcquisitionEquipment): string {
        if (acqEqpt && acqEqpt.manufacturerModel) {
            let manufModel: ManufacturerModel = acqEqpt.manufacturerModel;
            return manufModel.manufacturer.name + " - " + manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                + " (" + DatasetModalityType[manufModel.datasetModalityType] + ") " + acqEqpt.serialNumber;
        }
        return "";
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}