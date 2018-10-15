import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { TableComponent } from '../../shared/components/table/table.component';
import { DatasetModalityType } from '../../shared/enums/dataset-modality-type';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { Step } from 'src/app/breadcrumbs/breadcrumbs.service';
import { Coil } from 'src/app/coils/shared/coil.model';

@Component({
    selector: 'acquisition-equipment-list',
    templateUrl: 'acquisition-equipment-list.component.html'
})

export class AcquisitionEquipmentListComponent extends BrowserPaginEntityListComponent<AcquisitionEquipment> {

    @ViewChild('table') table: TableComponent;
    private acqEquips: AcquisitionEquipment[];

    private createAcqEquip = false;
    private selectedAcqEquip : AcquisitionEquipment = new AcquisitionEquipment();

    @ViewChild('coilModal') coilModal: ModalComponent;


    constructor(
            private acqEquipService: AcquisitionEquipmentService,
            private viewContainerRef: ViewContainerRef) {
        super('acquisition-equipment');
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
                        + " (" + DatasetModalityType[acqEquip.manufacturerModel.datasetModalityType] + ")"
                        + " " + acqEquip.serialNumber + " - " + acqEquip.center.name;
                }
            },
            {
                headerName: "Manufacturer", field: "manufacturerModel.manufacturer.name", type: "link", clickAction: {
                    target: "/manufacturer", getParams: function (acqEquip: AcquisitionEquipment): Object {
                        return { id: acqEquip.manufacturerModel.manufacturer.id, mode: "view" };
                    }
                }
            },
            {
                headerName: "Manufacturer model name", field: "manufacturerModel.name", type: "link", clickAction: {
                    target: "/manufacturer-model", getParams: function (acqEquip: AcquisitionEquipment): Object {
                        return { id: acqEquip.manufacturerModel.id, mode: "view" };
                    }
                }
            },
            { headerName: "Serial number", field: "serialNumber", width: "200px" },
            {
                headerName: "Center", field: "center.name", type: "link", clickAction: {
                    target: "/center", getParams: function (acqEquip: AcquisitionEquipment): Object {
                        return { id: acqEquip.center.id, mode: "view" };
                    }
                }
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
        let currentStep: Step = this.breadcrumbsService.lastStep;
        this.router.navigate(['/coil/create']).then(success => {
            currentStep.waitFor(this.breadcrumbsService.lastStep).subscribe(entity => {
                let createdCoil = entity as Coil;
                console.log('TODO : do something with this acqEquipment n°' + acqEquip.id + ' and the newly created coil n°' + createdCoil.id);
            });
        });
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

}