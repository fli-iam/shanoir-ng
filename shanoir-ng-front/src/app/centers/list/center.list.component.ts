import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';
import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ModalComponent } from '../../shared/utils/modal.component';
import { TableComponent } from "../../shared/table/table.component";

@Component({
    selector: 'center-list',
    templateUrl: 'center.list.component.html',
    styleUrls: ['center.list.component.css']
})

export class CenterListComponent {
    @ViewChild('acqEqptModal') acqEqptModal: ModalComponent;
    public centers: Center[];
    public createAcqEquip = false;
    public columnDefs: any[];
    public customActionDefs: any[];
    public deletionInternalError: boolean = false;
    public isLinkedWithEqpts: boolean = false;
    public isLinkedWithStudies: boolean = false;
    public loading: boolean = false;
    public rowClickAction: Object;
    public visible = false;
    private visibleAnimate = false;
    
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
        this.columnDefs = [
            { headerName: "Name", field: "name" },
            { headerName: "Town", field: "city" },
            { headerName: "Country", field: "country" }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", type: "button", img: "assets/images/icons/edit.png", target: "/centerDetail", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: "assets/images/icons/view-1.png", target: "/centerDetail", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: "assets/images/icons/medical/cardiogram-1.png", tip: "Add acq. equip.",
                action: this.openCreateAcqEquip
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new center", img: "assets/images/icons/add-1.png", target: "/centerDetail", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/centerDetail", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
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
        this.centerService.delete(centerId).then((res) => this.getCenters()).catch((error) => {
            if (error.status == 422) {
                let errDetails = error.json().details.fieldErrors["delete"] || '';
                for (var errKey in errDetails) {
                    if (errDetails[errKey]["givenValue"] == "acquisitionEquipments") {
                        this.isLinkedWithEqpts = true;
                    }
                    if (errDetails[errKey]["givenValue"] == "studies") {
                        this.isLinkedWithStudies = true;
                    }
                }
            } else if (error.status == 500) {
                this.deletionInternalError = true;
            }
            setTimeout(this.removeErroLabel.bind(this), 5000);
        });
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

    openCreateAcqEquip = () => {
        this.acqEqptModal.show();
    }

    closePopin() {
        this.acqEqptModal.hide();
    }

    public show(): void {
        this.visible = true;
        setTimeout(() => this.visibleAnimate = true, 100);
    }

    public hide(): void {
        this.visibleAnimate = false;
        setTimeout(() => this.visible = false, 300);
    }

    public onContainerClicked(event: MouseEvent): void {
        if ((<HTMLElement>event.target).classList.contains('modal')) {
            this.hide();
        }
    }

    public removeErroLabel(): void {
        this.deletionInternalError = false;
        this.isLinkedWithEqpts = false;
        this.isLinkedWithStudies = false
    }
}