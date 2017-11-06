import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { StudyStatus } from '../shared/enum/studyStatus';
import { TableComponent } from "../../shared/table/table.component";

@Component({
    selector: 'study-list',
    templateUrl: 'study.list.component.html',
    styleUrls: ['study.list.component.css']
})

export class StudyListComponent {
    public columnDefs: any[];
    public customActionDefs: any[];
    public loading: boolean = false;
    public rowClickAction: Object;
    public studies: Study[];

    constructor(private studyService: StudyService, private confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
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
                headerName: "Subjects", field: "subjectNames", type: "number", cellRenderer: function (params: any) {
                    return params.data.subjectNames ? params.data.subjectNames.length : 0;
                }
            },
            {
                headerName: "Examinations", field: "examinationIds", type: "number", cellRenderer: function (params: any) {
                    return params.data.examinationIds ? params.data.examinationIds.length : 0;
                }
            },
            {
                headerName: "", type: "button", img: "assets/images/icons/edit.png", target: "/studyDetail", getParams: function (item: any): Object {
                    return { id: item.id };
                }
            },
            { headerName: "", type: "button", img: "assets/images/icons/garbage-1.png", action: this.openDeleteStudyConfirmDialog }
        ];
        this.customActionDefs = [
            { title: "new study", img: "assets/images/icons/add-1.png", target: "../studyDetail" },
        ];
        this.rowClickAction = {
            target: "/studyDetail", getParams: function (item: any): Object {
                return { id: item.id };
            }
        };
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