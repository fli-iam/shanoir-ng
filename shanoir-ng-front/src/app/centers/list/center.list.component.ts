import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { TableComponent } from "../../shared/table/table.component";
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";

@Component({
    selector: 'center-list',
    templateUrl: 'center.list.component.html',
    styleUrls: ['center.list.component.css']
})

export class CenterListComponent {
    public centers: Center[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;
    private createAcqEquip = false;

    constructor(private centerService: CenterService, private confirmDialogService: ConfirmDialogService, 
        private viewContainerRef: ViewContainerRef, private keycloakService: KeycloakService) {
        this.getCenters();
        this.createColumnDefs();
    }   

    // Grid data
    getCenters(): void {
        this.loading = true;
        this.centerService.getCenters().then(centers => {
            if (centers) {
                this.centers = centers;
            }
            this.loading = false;
        })
        .catch((error) => {
            // TODO: display error
            this.centers = [];
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
            {headerName: "Name", field: "name" },
            {headerName: "Town", field: "city" },
            {headerName: "Country", field: "country" }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: "/assets/images/icons/garbage-1.png", action: this.openDeleteCenterConfirmDialog},
            {headerName: "", type: "button", img: "/assets/images/icons/edit.png", target : "/detailCenter", getParams: function(item: any): Object {
                return {id: item.id, mode: "edit"};
            }});
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: "/assets/images/icons/view-1.png", target : "/detailCenter", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: "/assets/images/icons/medical/cardiogram-1.png", tip: "Add acq. equip.",
            action: this.openCreateAcqEquip});
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({title: "new center", img: "/assets/images/icons/add-1.png", target: "/detailCenter", getParams: function(item: any): Object {
                    return {mode: "create"};
            }});
            this.customActionDefs.push({title: "delete selected", img: "/assets/images/icons/garbage-1.png", action: this.deleteAll });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {target : "/detailCenter", getParams: function(item: any): Object {
                    return {id: item.id, mode: "view"};
            }};
        }
    }

    openDeleteCenterConfirmDialog = (item: Center) => {
         this.confirmDialogService
            .confirm('Delete center', 'Are you sure you want to delete center ' + item.name + ' , ' + item.city + ' , ' + item.country + '?',
                this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteCenter(item.id);
                }
            })
    }

    deleteCenter(centerId: number) {
        // Delete center and refresh page
        this.centerService.delete(centerId).then((res) => this.getCenters());
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let center of this.centers) {
            if (center["isSelectedInTable"]) ids.push(center.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

    openCreateAcqEquip = ()=> {
        this.createAcqEquip = true;
    }

    closePopup(subject: any) {
        this.createAcqEquip = false;
    }

}