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

import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { Step } from '../../breadcrumbs/breadcrumbs.service';

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { TableComponent } from '../../shared/components/table/table.component';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

@Component({
    selector: 'acquisition-equipment-list',
    templateUrl: 'acquisition-equipment-list.component.html'
})

export class AcquisitionEquipmentListComponent extends BrowserPaginEntityListComponent<AcquisitionEquipment> {

    @ViewChild('table', { static: false }) table: TableComponent;
    private acqEquips: AcquisitionEquipment[];

    private createAcqEquip = false;
    private selectedAcqEquip : AcquisitionEquipment = new AcquisitionEquipment();

    @ViewChild('coilModal') coilModal: ModalComponent;


    constructor(
            private acqEquipService: AcquisitionEquipmentService,
            private viewContainerRef: ViewContainerRef) {
        super('acquisition-equipment');
    }

    getService(): EntityService<AcquisitionEquipment> {
        return this.acqEquipService;
    }
    
    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    // Grid data
    getEntities(): Promise<AcquisitionEquipment[]> {
        return this.acqEquipService.getAll();
    }

    // Grid columns definition
    getColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };

        let columnDefs: any = [
            {
                headerName: "Acquisition equipment", field: "name", cellRenderer: function (params: any) {
                    let acqEquip: AcquisitionEquipment = params.data;
                    if (!acqEquip) return;
                    return acqEquip.manufacturerModel.manufacturer.name + " - " + acqEquip.manufacturerModel.name + " "
                        + (acqEquip.manufacturerModel.magneticField ? (acqEquip.manufacturerModel.magneticField + "T") : "")
                        + " (" + DatasetModalityType.getLabel(acqEquip.manufacturerModel.datasetModalityType) + ")" 
                        + " " + acqEquip.serialNumber + " - " + acqEquip.center.name;
                }
            },
            {
                headerName: "Manufacturer", field: "manufacturerModel.manufacturer.name",
                route: (acqEquip: AcquisitionEquipment) => '/manufacturer/details/' + acqEquip.manufacturerModel.manufacturer.id
            },
            {
                headerName: "Manufacturer model name", field: "manufacturerModel.name",
                route: (acqEquip: AcquisitionEquipment) => '/manufacturer-model/details/' + acqEquip.manufacturerModel.id
            },
            { headerName: "Serial number", field: "serialNumber", width: "200px" },
            {
                headerName: "Center", field: "center.name",
                route: (acqEquip: AcquisitionEquipment) => '/center/details/' + acqEquip.center.id
            }
        ];
        if (this.keycloakService.isUserAdminOrExpert()) {
            columnDefs.push({
                headerName: "", 
                type: "button", 
                awesome: "fa-magnet", 
                tip: "Add coil",
                action: (acqEquip) => this.openCreateCoil(acqEquip)
            });
        }
        return columnDefs;
    }
    

    openCreateCoil(acqEquip: AcquisitionEquipment) { 
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/coil/create'], ).then(success => {
            this.breadcrumbsService.currentStep.addPrefilled('center', acqEquip.center);
            this.breadcrumbsService.currentStep.addPrefilled('manufacturerModel', acqEquip.manufacturerModel);
            currentStep.waitFor(this.breadcrumbsService.currentStep);
        });
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

}