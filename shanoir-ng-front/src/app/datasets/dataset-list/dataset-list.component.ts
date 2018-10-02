import { Component, ViewChild } from '@angular/core';

import { EntityListComponent } from '../../shared/components/entity/entity-list.component.abstract';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';

@Component({
    selector: 'dataset-list',
    templateUrl: 'dataset-list.component.html',
    styleUrls: ['dataset-list.component.css']
})

export class DatasetListComponent extends EntityListComponent<Dataset>{
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    @ViewChild('dsTable') table: TableComponent;

    constructor(
            private datasetService: DatasetService,
            private studyService: StudyService,
            private subjectService: SubjectService) {
                
        super('dataset', {'new': false});
        this.fetchStudies();
        this.fetchSubjects();
    }

    getPage(pageable: Pageable): Promise<Page<Dataset>> {
        return this.datasetService.getPage(pageable).then(page => {
            return page;
        });
    }

    // Grid columns definition
    getColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        return [
            {headerName: "Id", field: "id", type: "number", width: "30px"},
            {headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"]},
            {headerName: "Type", field: "type", width: "50px", suppressSorting: true},
            {headerName: "Subject", field: "subjectId", cellRenderer: (params: any) => this.getSubjectName(params.data.subjectId)},
            {headerName: "Study", field: "studyId", cellRenderer: (params: any) => this.getStudyName(params.data.studyId)},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.creationDate)},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
    }

    private fetchSubjects() {
        this.subjectService.getAll().then(subjects => {
            this.subjects = subjects;
        });
    }

    private fetchStudies() {
        this.studyService.getAll().then(studies => {
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

    getCustomActionsDefs(): any[] {
        return [];
    }

}