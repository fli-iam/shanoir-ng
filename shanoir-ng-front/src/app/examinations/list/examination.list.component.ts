import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { TableComponent } from "../../shared/table/table.component";
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";

@Component({
    selector: 'examination-list',
    templateUrl: 'examination.list.component.html',
    styleUrls: ['examination.list.component.css'],
})



export class ExaminationListComponent {
    public examinations: Examination[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;
    private createAcqEquip = false;

    constructor(private examinationService: ExaminationService, private confirmDialogService: ConfirmDialogService,
        private viewContainerRef: ViewContainerRef, private keycloakService: KeycloakService) {
        this.getExaminations();
        this.createColumnDefs();
    }

    // Grid data
    getExaminations(): void {
        this.loading = true;
        this.examinationService.getExaminations().then(examinations => {
            if (examinations) {
                this.examinations = examinations;
            }
            this.loading = false;
        })
            .catch((error) => {
                // TODO: display error
                this.examinations = [];
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
            { headerName: "Examination id", field: "id" },
            { headerName: "Subject", field: "subject.name" },
            {headerName: "Examination date", field: "examinationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.examinationDate);
            }},
            { headerName: "Research study", field: "study.name" },
            { headerName: "Examination executive", field: "" },
            { headerName: "Center", field: "center.name" }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", type: "button", img: "assets/images/icons/edit.png", target: "/examinationDetail", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: "assets/images/icons/view-1.png", target: "/examinationDetail", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new examination.", img: "assets/images/icons/add-1.png", target: "/examinationDetail", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/examinationDetail", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
        }
    }

    openDeleteExaminationConfirmDialog = (item: Examination) => {
        this.confirmDialogService
            .confirm('Delete examination', 'Are you sure you want to delete the following entity?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteExamination(item.id);
                }
            })
    }

    deleteExamination(examinationId: number) {
        // Delete examination and refresh page
        this.examinationService.delete(examinationId).then((res) => this.getExaminations());
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let examination of this.examinations) {
            if (examination["isSelectedInTable"]) ids.push(examination.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}