import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { DatasetModalityType } from '../../shared/enums/dataset-modality-type';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { TableComponent } from "../../shared/components/table/table.component";

@Component({
    selector: 'acquisition-equipment-list',
    templateUrl: 'acquisition-equipment-list.component.html',
    styleUrls: ['acquisition-equipment-list.component.css']
})

export class AcquisitionEquipmentListComponent {
    public acqEquips: AcquisitionEquipment[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;
    private createAcqEquip = false;

    constructor(private acqEquipService: AcquisitionEquipmentService, private confirmDialogService: ConfirmDialogService,
        private viewContainerRef: ViewContainerRef, private keycloakService: KeycloakService) {
        this.getAcquisitionEquipments();
        this.createColumnDefs();
    }

    // Grid data
    getAcquisitionEquipments(): void {
        this.loading = true;
        this.acqEquipService.getAcquisitionEquipments().then(acqEquips => {
            if (acqEquips) {
                this.acqEquips = acqEquips;
            }
            this.loading = false;
        })
            .catch((error) => {
                // TODO: display error
                this.acqEquips = [];
            });
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
                }, width: "200px"
            },
            {
                headerName: "Manufacturer model name", field: "manufacturerModel.name", type: "link", clickAction: {
                    target: "/manufacturer-model", getParams: function (acqEquip: AcquisitionEquipment): Object {
                        return { id: acqEquip.manufacturerModel.id, mode: "view" };
                    }
                }, width: "200px"
            },
            { headerName: "Serial number", field: "serialNumber", width: "200px" },
            {
                headerName: "Center", field: "center.name", type: "link", clickAction: {
                    target: "/center", getParams: function (acqEquip: AcquisitionEquipment): Object {
                        return { id: acqEquip.center.id, mode: "view" };
                    }
                }, width: "300px"
            }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", type: "button", img: "assets/images/icons/edit.png", target: "/acquisition-equipment", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: "assets/images/icons/view-1.png", target: "/acquisition-equipment", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new acq. equip.", img: "assets/images/icons/add-1.png", target: "/acquisition-equipment", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/acquisition-equipment", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
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

    deleteAll = () => {
        let ids: number[] = [];
        for (let acqEquip of this.acqEquips) {
            if (acqEquip["isSelectedInTable"]) ids.push(acqEquip.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}