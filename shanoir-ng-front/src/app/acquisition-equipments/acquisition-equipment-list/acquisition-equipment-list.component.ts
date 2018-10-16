import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { DatasetModalityType } from '../../shared/enums/dataset-modality-type';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { TableComponent } from "../../shared/components/table/table.component";
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { Center } from '../../centers/shared/center.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { Router } from '@angular/router';

@Component({
    selector: 'acquisition-equipment-list',
    templateUrl: 'acquisition-equipment-list.component.html',
    styleUrls: ['acquisition-equipment-list.component.css']
})

export class AcquisitionEquipmentListComponent {

    private acqEquips: AcquisitionEquipment[];
    private acqEqPromise: Promise<void> = this.getAcquisitionEquipments();
    private browserPaging: BrowserPaging<AcquisitionEquipment>;

    private columnDefs: any[];
    private customActionDefs: any[];

    private createAcqEquip = false;
    private selectedAcqEquip : AcquisitionEquipment = new AcquisitionEquipment();

    @ViewChild('coilModal') coilModal: ModalComponent;


    constructor(
            private acqEquipService: AcquisitionEquipmentService, 
            private confirmDialogService: ConfirmDialogService,
            private viewContainerRef: ViewContainerRef, 
            private keycloakService: KeycloakService,
            private router: Router) {

        this.createColumnDefs();
    }

    getPage(pageable: FilterablePageable): Promise<Page<AcquisitionEquipment>> {
        return new Promise((resolve) => {
            this.acqEqPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    // Grid data
    getAcquisitionEquipments(): Promise<void> {
        return this.acqEquipService.getAcquisitionEquipments().then(acqEquips => {
            if (acqEquips) {
                this.acqEquips = acqEquips;
                this.browserPaging = new BrowserPaging(acqEquips, this.columnDefs);
            }
        })
    }

    // Grid columns definition
    private createColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };

        this.columnDefs = [
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
                headerName: "Manufacturer", field: "manufacturerModel.manufacturer.name", type: "link", 
                action: (acqEquip: AcquisitionEquipment) => this.router.navigate(['/manufacturer/details/' + acqEquip.manufacturerModel.manufacturer.id])
            },
            {
                headerName: "Manufacturer model name", field: "manufacturerModel.name", type: "link", 
                action: (acqEquip: AcquisitionEquipment) => this.router.navigate(['/manufacturer-model/details/' + acqEquip.manufacturerModel.id])
            },
            { headerName: "Serial number", field: "serialNumber", width: "200px" },
            {
                headerName: "Center", field: "center.name", type: "link", 
                action: (acqEquip: AcquisitionEquipment) => this.router.navigate(['/center/details/' + acqEquip.center.id])
            }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: item => this.router.navigate(['/acquisition-equipment/edit/'+ item.id])
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, action: item => this.router.navigate(['/acquisition-equipment/details/'+ item.id])
            });
        }

        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.CARDIOGRAM_ICON_PATH, 
                tip: "Add coil",
                action: this.openCreateCoil
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new acq. equip.", img: ImagesUrlUtil.ADD_ICON_PATH, action: item => this.router.navigate(['/acquisition-equipment/create'])
            });
        }
    }

    private onRowClick(acqEquip: AcquisitionEquipment) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/acquisition-equipment/details/'+ acqEquip.id]);
        }
    }

    openDeleteAcquisitionEquipmentConfirmDialog = (item: AcquisitionEquipment) => {
        this.confirmDialogService
            .confirm('Delete acqEquip', 'Are you sure you want to delete the following entity?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteAcquisitionEquipment(item.id);
                }
            })
    }

    deleteAcquisitionEquipment(acqEquipId: number) {
        // Delete acqEquip and refresh page
        this.acqEquipService.delete(acqEquipId).then((res) => this.getAcquisitionEquipments());
    }

    openCreateCoil = () => { 
        
        /*for (let acqEquip of this.acqEquips) {
            if (acqEquip["isSelectedInTable"])   this.selectedAcqEquip =   acqEquip.center;     
        }*/

        this.coilModal.show();
    }

    closePopin() {
        this.coilModal.hide();
    }

}