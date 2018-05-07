import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { TableComponent } from "../../shared/components/table/table.component";
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';

@Component({
    selector: 'dataset-list',
    templateUrl: 'dataset-list.component.html',
    styleUrls: ['dataset-list.component.css']
})

export class DatasetListComponent {
    public datasets: Dataset[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;

    constructor(
            private datasetService: DatasetService, 
            private confirmDialogService: ConfirmDialogService, 
            private viewContainerRef: ViewContainerRef,
            private keycloakService: KeycloakService) {
        this.getAll();
        this.createColumnDefs();
    }

    // Grid data
    getAll(): void {
        this.loading = true;
        this.datasetService.getAll().then(datasets => {
            if (datasets) {
                this.datasets = datasets;
            }
            this.loading = false;
        })
        .catch((error) => {
            this.datasets = [];
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
            {headerName: "Id", field: "id", type: "number", width: "30px"},
            {headerName: "Name", field: "name"},
            {headerName: "Type", field: "type", width: "50px"},
            {headerName: "Subject", field: "subjectId"},
            {headerName: "Study", field: "studyId"},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.creationDate);
            }},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", type: "button", awesome: "fa-edit", target: "/dataset", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", awesome: "fa-eye", target: "/dataset", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }

        this.customActionDefs = [
            {title: "new user", awesome: "fa-plus", target: "../user"},
        ];
        this.rowClickAction = {target : "/dataset", getParams: function(item: any): Object {
            return {id: item.id, mode: "view"};
        }};
    }

    openDeleteConfirmDialog = (item: Dataset) => {
         this.confirmDialogService
                .confirm('Delete dataset', 'Are you sure you want to delete dataset ' + item.id + '?',
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item.id);
                    }
                })
    }

    delete(id: number) {
        // Delete user and refresh page
        this.datasetService.delete(id).then((res) => this.getAll());
    }

}