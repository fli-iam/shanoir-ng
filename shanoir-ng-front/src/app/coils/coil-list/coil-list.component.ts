import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { TableComponent } from "../../shared/components/table/table.component";
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";

@Component({
    selector: 'coil-list',
    templateUrl: 'coil-list.component.html',
    styleUrls: ['coil-list.component.css'],
})
export class CoilListComponent {
    public coils: Coil[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;
   // private createAcqEquip = false;

    constructor(private coilService: CoilService, private confirmDialogService: ConfirmDialogService,
        private viewContainerRef: ViewContainerRef, private keycloakService: KeycloakService) {
        this.getCoils();
        this.createColumnDefs();
    }

    // Grid data
    getCoils(): void {
        this.loading = true;
        this.coilService.getCoils().then(coils => {
            if (coils) {
                this.coils = coils;
            }
            this.loading = false;
        })
            .catch((error) => {
                // TODO: display error
                this.coils = [];
            });
    }

    // Grid columns definition
    private createColumnDefs() {

        this.columnDefs = [
            { headerName: "Name", field: "name" },
            
            { headerName: "Acquisition Equipment Model", field: "manufacturerModel.name" , type: "link", clickAction: {
                target: "/manufacturer-model", getParams: function (coil: Coil): Object {
                    return { id: coil.manufacturerModel.id , mode: "view" };
                }

            } },
           
            { headerName: "Center", field: "center.name" , type: "link", clickAction: {
                target: "/center", getParams: function (coil: Coil): Object {
                    return { id: coil.center.id, mode: "view" };
                }
            }},

            { headerName: "Coil Type", field: "coilType" },
            { headerName: "Number of channels", field: "numberOfChannels" },
            { headerName: "Serial number", field: "serialNumber" }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target: "/coil", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target: "/coil", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new coil.", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/coil", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/coil", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
        }
    }

    openDeleteExaminationConfirmDialog = (item: Coil) => {
        this.confirmDialogService
            .confirm('Delete coil', 'Are you sure you want to delete the following entity?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteCoil(item.id);
                }
            })
    }

    deleteCoil(coilId: number) {
        // Delete coil and refresh page
        this.coilService.delete(coilId).then((res) => this.getCoils());
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let coil of this.coils) {
            if (coil["isSelectedInTable"]) ids.push(coil.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}