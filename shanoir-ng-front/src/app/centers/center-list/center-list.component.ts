import { Component, ViewChild } from '@angular/core';

import { EntityListComponent } from '../../shared/components/entity/entity-list.component.abstract';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { ShanoirError } from '../../shared/models/error.model';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-list',
    templateUrl: 'center-list.component.html',
    styleUrls: ['center-list.component.css']
})

export class CenterListComponent extends EntityListComponent<Center> {

    @ViewChild('table') table: TableComponent;
    private centersPromise: Promise<void> = this.getCenters();
    private browserPaging: BrowserPaging<Center>;
    private centers: Center[];
    
    constructor(
            private centerService: CenterService) {
        super('center');
        this.manageDelete();
    }

    getPage(pageable: FilterablePageable): Promise<Page<Center>> {
        return new Promise((resolve) => {
            this.centersPromise.then(() => {
                this.browserPaging.setItems(this.centers);
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    private getCenters(): Promise<void> {
        return this.centerService.getCenters().then(centers => {
            if (centers) {
                this.centers = centers;
                this.browserPaging = new BrowserPaging(centers, this.columnDefs);
            }
        });
    }

    getColumnDefs() {
        let columnDefs: any[] = [
            { headerName: "Name", field: "name" },
            { headerName: "Town", field: "city" },
            { headerName: "Country", field: "country" }
        ];
        if (this.keycloakService.isUserAdminOrExpert()) {
            columnDefs.push({ headerName: "", type: "button", awesome: "fa-podcast", tip: "Add acq. equip.", action: item => this.openCreateAcqEquip() });
        }
        return columnDefs;
    }

    private openCreateAcqEquip = () => {
        //this.acqEqptModal.show();
    }

    // closePopin() {
    //     this.acqEqptModal.hide();
    // }


    private manageDelete() {
        this.subscribtions.push(
            this.onDelete.subscribe(response => {
                if (response instanceof Center) {
                    this.centers = this.centers.filter(item => item.id != response.id);
                } else if (response instanceof ShanoirError) {
                    if (response.code == 422) {
                        let msg: string  = this.buildDeleteErrMsg(response.details.fieldErrors["delete"] || '');
                        this.msgBoxService.log('warn', msg, 10000);
                    }     
                }
            })
        );
    }

    private buildDeleteErrMsg(errDetails: any): string {
        let isLinkedWithEqpts: boolean = false;
        let isLinkedWithStudies: boolean = false;
        for (var errKey in errDetails) {
            if (errDetails[errKey]["givenValue"] == "acquisitionEquipments") {
                isLinkedWithEqpts = true;
            }
            if (errDetails[errKey]["givenValue"] == "studies") {
                isLinkedWithStudies = true;
            }
        }
        let msg: string = 'This center cannot be deleted. It is associated with ';
        if (isLinkedWithEqpts) msg += 'acquisition equipment(s)' ;
        if (isLinkedWithEqpts && isLinkedWithStudies) msg += ' and ';
        if (isLinkedWithStudies) msg += 'study(ies)';
        return msg;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}