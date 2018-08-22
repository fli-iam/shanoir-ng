import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { TableComponent } from "../../shared/components/table/table.component";
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { Router } from '@angular/router';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';

@Component({
    selector: 'coil-list',
    templateUrl: 'coil-list.component.html',
    styleUrls: ['coil-list.component.css'],
})
export class CoilListComponent {
    private coils: Coil[];
    private coilsPromise: Promise<void> = this.getCoils();
    private browserPaging: BrowserPaging<Coil>;
    private columnDefs: any[];
    private customActionDefs: any[];

    constructor(
            private coilService: CoilService, 
            private confirmDialogService: ConfirmDialogService,
            private viewContainerRef: ViewContainerRef, 
            private keycloakService: KeycloakService,
            private router: Router) {
        this.createColumnDefs();
    }

    getPage(pageable: FilterablePageable): Promise<Page<Coil>> {
        return new Promise((resolve) => {
            this.coilsPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    getCoils(): Promise<void> {
        return this.coilService.getCoils().then(coils => {
            if (coils) {
                this.coils = coils;
                this.browserPaging = new BrowserPaging(coils, this.columnDefs);
            }
        })
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
    }

    private onRowClick(coil: Coil) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/coil'], { queryParams: { id: coil.id, mode: "view" } });
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