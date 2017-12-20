import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { StudyStatus } from '../shared/study-status.enum';
import { TableComponent } from "../../shared/components/table/table.component";

@Component({
    selector: 'study-list',
    templateUrl: 'study-list.component.html',
    styleUrls: ['study-list.component.css']
})

export class StudyListComponent {
    public columnDefs: any[];
    public customActionDefs: any[];
    public loading: boolean = false;
    public rowClickAction: Object;
    public studies: Study[];

    constructor(private confirmDialogService: ConfirmDialogService, private keycloakService: KeycloakService,
        private studyService: StudyService, private viewContainerRef: ViewContainerRef) {
        this.getStudies();
        this.createColumnDefs();
    }

    // Grid data
    getStudies(): void {
        this.loading = true;
        this.studyService.getStudies().then(studies => {
            if (studies) {
                this.studies = studies;
            }
            this.loading = false;
        })
            .catch((error) => {
                // TODO: display error
                this.studies = [];
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
            { headerName: "Name", field: "name" },
            {
                headerName: "Status", field: "studyStatus", cellRenderer: function (params: any) {
                    return StudyStatus[params.data.studyStatus];
                }
            },
            {
                headerName: "Start date", field: "startDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End date", field: "endDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.endDate);
                }
            },
            {
                headerName: "Subjects", field: "nbSujects", type: "number"
            },
            {
                headerName: "Examinations", field: "nbExaminations", type: "number"
            },
            {
                headerName: "", type: "button", img: "assets/images/icons/edit.png", target: "/study", getParams: function (item: any): Object {
                    return { id: item.id };
                }
            },
            { headerName: "", type: "button", img: "assets/images/icons/garbage.png", action: this.openDeleteStudyConfirmDialog }
        ];

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new study", img: "assets/images/icons/add.png", target: "/study", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }

        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/study", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
        }
    }

    openDeleteStudyConfirmDialog = (item: Study) => {
        this.confirmDialogService
            .confirm('Delete study', 'Are you sure you want to delete study ' + item.name + '?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteStudy(item.id);
                }
            })
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let study of this.studies) {
            if (study["isSelectedInTable"]) ids.push(study.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

    deleteStudy(studyId: number) {
        // Delete studyId and refresh page
        this.studyService.delete(studyId).then((res) => this.getStudies());
    }

}