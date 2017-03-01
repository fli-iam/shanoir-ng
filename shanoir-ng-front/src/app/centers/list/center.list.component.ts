import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { TableComponent } from "../../shared/table/table.component";
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-list',
    templateUrl: 'center.list.component.html'
})

export class CenterListComponent {
    public centers: Center[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;

    constructor(private centerService: CenterService, private confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
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
    }


}