import { Component, ViewContainerRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { TableComponent } from '../../shared/components/table/table.component';

@Component({
    selector: 'dataset-list',
    templateUrl: 'dataset-list.component.html',
    styleUrls: ['dataset-list.component.css']
})

export class DatasetListComponent {
    private columnDefs: any[];
    private customActionDefs: any[];
    private rowClickAction: Object;
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    @ViewChild('dsTable') table: TableComponent;

    constructor(
            private datasetService: DatasetService, 
            private confirmDialogService: ConfirmDialogService, 
            private viewContainerRef: ViewContainerRef,
            private keycloakService: KeycloakService,
            private msgService: MsgBoxService,
            private studyService: StudyService,
            private subjectService: SubjectService, 
            private router: Router) {

        this.fetchStudies();
        this.fetchSubjects();
        this.createColumnDefs();
    }

    getPage(pageable: Pageable): Promise<Page<Dataset>> {
        return this.datasetService.getPage(pageable).then(page => {
            return page;
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
            {headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"]},
            {headerName: "Type", field: "type", width: "50px", suppressSorting: true},
            {headerName: "Subject", field: "subjectId", cellRenderer: (params: any) => this.getSubjectName(params.data.subjectId)},
            {headerName: "Study", field: "studyId", cellRenderer: (params: any) => this.getStudyName(params.data.studyId)},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.creationDate)},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
        this.columnDefs.push({
            headerName: "", type: "button", awesome: "fa-eye", action: (dataset) => this.router.navigate(['/dataset/details/'+dataset.id])
        });
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
                {
                    headerName: "", 
                    type: "button", 
                    awesome: "fa-edit", 
                    action: (dataset) => this.router.navigate(['/dataset/edit/'+dataset.id])
                }, {
                    headerName: "", 
                    type: "button", 
                    awesome: "fa-trash", 
                    action: this.openDeleteConfirmDialog
                }
            );
        }
    }
    
    private onRowClick(dataset: Dataset) {
        this.router.navigate(['/dataset/details/'+dataset.id]);
    }

    openDeleteConfirmDialog = (item: Dataset) => {
        this.confirmDialogService
                .confirm('Delete dataset', 'Are you sure you want to delete dataset ' + item.id + '?',
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.datasetService.delete(item.id).then(() => {
                            this.table.refresh();
                            this.msgService.log('info', 'The dataset has been sucessfully deleted');
                        });
                    }
                });
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
        if (!this.subjects || this.subjects.length == 0 || !id) return id ? id+'' : '';
        for (let subject of this.subjects) { 
            if (subject.id == id) return subject.name;
        }
        throw new Error('Cannot find subject for id = ' + id);
    }

    private getStudyName(id: number): string {
        if (!this.studies || this.studies.length == 0 || !id) return id+'';
        for (let study of this.studies) {
            if (study.id == id) return study.name;
        }
        throw new Error('Cannot find study for id = ' + id);
    }

}