import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-list',
    templateUrl: 'center-list.component.html',
    styleUrls: ['center-list.component.css']
})

export class CenterListComponent {
    @ViewChild('acqEqptModal') acqEqptModal: ModalComponent;
    private centers: Center[];
    private centersPromise: Promise<void> = this.getCenters();
    private browserPaging: BrowserPaging<Center>;

    private createAcqEquip = false;
    private columnDefs: any[];
    private customActionDefs: any[];
    private deletionInternalError: boolean = false;
    private isLinkedWithEqpts: boolean = false;
    private isLinkedWithStudies: boolean = false;
    private visible = false;
    private visibleAnimate = false;
    
    constructor(private centerService: CenterService, 
            private confirmDialogService: ConfirmDialogService,
            private viewContainerRef: ViewContainerRef, 
            private keycloakService: KeycloakService,
            private router: Router) {
        this.createColumnDefs();
    }

    getCenters(): Promise<void> {
        return this.centerService.getCenters().then(centers => {
            if (centers) {
                this.centers = centers;
                this.browserPaging = new BrowserPaging(centers, this.columnDefs);
            }
        });
    }

    getPage(pageable: FilterablePageable): Promise<Page<Center>> {
        return new Promise((resolve) => {
            this.centersPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
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
            this.columnDefs.push({
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: center => this.router.navigate(['/center/edit/'+center.id])
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, action: center => this.router.navigate(['/center/details/'+center.id])
            });
        }
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.CARDIOGRAM_ICON_PATH, tip: "Add acq. equip.",
                action: this.openCreateAcqEquip
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new center", img: ImagesUrlUtil.ADD_ICON_PATH, action: center => this.router.navigate(['/center/create'])
            });
        }
    }

    private onRowClick(center: Center) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/center/details/'+center.id])
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