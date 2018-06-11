import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { TableComponent } from "../../shared/components/table/table.component";
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Subject } from '../../subjects/shared/subject.model';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { SubjectService } from '../../subjects/shared/subject.service';

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
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    
    constructor(
            private datasetService: DatasetService, 
            private confirmDialogService: ConfirmDialogService, 
            private viewContainerRef: ViewContainerRef,
            private keycloakService: KeycloakService,
            private msgService: MsgBoxService,
            private studyService: StudyService,
            private subjectService: SubjectService) {
        this.fetchStudies();
        this.fetchSubjects();
        this.getAll();
        this.createColumnDefs();
        this.fetchSubjects();
        this.fetchStudies();
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
            {headerName: "Subject", field: "subjectId", cellRenderer: (params: any) => this.getSubjectName(params.data.subjectId)},
            {headerName: "Study", field: "studyId", cellRenderer: (params: any) => this.getStudyName(params.data.studyId)},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.creationDate)},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", awesome: "fa-eye", target: "/dataset", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", 
                    type: "button", 
                    awesome: "fa-edit", 
                    target: "/dataset", 
                    getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                }, {
                    headerName: "", 
                    type: "button", 
                    awesome: "fa-trash", 
                    action: this.openDeleteConfirmDialog
                }
            );
        }
        this.rowClickAction = {target : "/dataset", getParams: function(item: any): Object {
            return {id: item.id, mode: "view"};
        }};
    }

    openDeleteConfirmDialog = (item: Dataset) => {
        console.log(item);
        this.confirmDialogService
                .confirm('Delete dataset', 'Are you sure you want to delete dataset ' + item.id + '?',
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.datasetService.delete(item.id).then(() => {
                            let index: number = this.datasets.indexOf(item);
                            if (index > -1) this.datasets.splice(index, 1);
                            this.msgService.log('info', 'The dataset has been sucessfully deleted');
                        });
                    }
                })
    }

    delete(id: number) {
        // Delete user and refresh page
        this.datasetService.delete(id).then((res) => this.getAll());
    }

    private fetchSubjects() {
        this.subjectService.getSubjects().then(subjects => {
            this.subjects = subjects;
        });
    }

    private fetchStudies() {
        this.studyService.getStudies().then(studies => {
            this.studies = studies;
        });
    }

    private getSubjectName(id: number): string {
        if (!this.subjects || this.subjects.length == 0 || !id) return null;
        for (let subject of this.subjects) {
            if (subject.id == id) return subject.name;
        }
        throw new Error('Cannot find subject for id = ' + id);
    }

    private getStudyName(id: number): string {
        if (!this.studies || this.studies.length == 0 || !id) return null;
        for (let study of this.studies) {
            if (study.id == id) return study.name;
        }
        throw new Error('Cannot find study for id = ' + id);
    }

}