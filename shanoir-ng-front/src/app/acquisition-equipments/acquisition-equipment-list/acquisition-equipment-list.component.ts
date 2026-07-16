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

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';

@Component({
    selector: 'acquisition-equipment-list',
    templateUrl: 'acquisition-equipment-list.component.html',
    imports: [TableComponent]
})

export class AcquisitionEquipmentListComponent extends BrowserPaginEntityListComponent<AcquisitionEquipment> {

    @ViewChild('table', { static: false }) table: TableComponent;
    private acqEquips: AcquisitionEquipment[];

    private createAcqEquip = false;
    private selectedAcqEquip : AcquisitionEquipment = new AcquisitionEquipment();


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
    getColumnDefs(): ColumnDefinition[] {
        const columnDefs: ColumnDefinition[] = [
            {
                headerName: "Center equipment", field: "name", cellRenderer: function (params: any) {
                    const acqEquip: AcquisitionEquipment = params.data;
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
                headerName: "Modality", field: "manufacturerModel.datasetModalityType", cellRenderer: function (params: any) {
                    const mod = DatasetModalityType.all().find(dsMod => dsMod.toString() == params.data.manufacturerModel.datasetModalityType);
                    if (mod) return DatasetModalityType.getLabel(mod);
                }
            },
            {
                headerName: "Manufacturer model name", field: "manufacturerModel.name",
                route: (acqEquip: AcquisitionEquipment) => '/manufacturer-model/details/' + acqEquip.manufacturerModel.id
            },
            {
                headerName: "Serial number", field: "serialNumber", width: "200px"
            },
            {
                headerName: "Acquisition Center", field: "center.name",
                route: (acqEquip: AcquisitionEquipment) => '/center/details/' + acqEquip.center.id
            }
        ];
        if (this.keycloakService.isUserAdminOrExpert()) {
            columnDefs.push({
                headerName: "",
                type: "button",
                awesome: "fa-solid fa-magnet",
                tip: () => { return "Add coil"},
                action: (acqEquip) => this.openCreateCoil(acqEquip)
            });
        }
        return columnDefs;
    }

    openCreateCoil(acqEquip: AcquisitionEquipment) {
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/coil/create'], ).then(() => {
            this.breadcrumbsService.currentStep.addPrefilled('entity.center', acqEquip.center, true);
            this.breadcrumbsService.currentStep.addPrefilled('entity.manufacturerModel', acqEquip.manufacturerModel, true);
            currentStep.waitFor(this.breadcrumbsService.currentStep);
        });
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

}
