/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';

@Component({
    selector: 'dataset-list',
    templateUrl: 'dataset-list.component.html',
    styleUrls: ['dataset-list.component.css']
})

export class DatasetListComponent extends EntityListComponent<Dataset>{
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    @ViewChild('table') table: TableComponent;

    constructor(
            private datasetService: DatasetService,
            private studyService: StudyService,
            private subjectService: SubjectService) {
                
        super('dataset');
        this.fetchStudies();
        this.fetchSubjects();
    }
    
    getPage(pageable: Pageable): Promise<Page<Dataset>> {
        return this.datasetService.getPage(pageable);
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
            {headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"]},
            {headerName: "Type", field: "type", width: "50px", suppressSorting: true},
            {headerName: "Subject", field: "subject.name"},
            {headerName: "Study", field: "study.name"},
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

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: false,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    canEdit(ds: Dataset): boolean {
        return this.keycloakService.isUserAdmin() || (
            ds.study &&
            ds.study.studyUserList && 
            ds.study.studyUserList.filter(su => su.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE)).length > 0
        );
    }

    canDelete(ds: Dataset): boolean {
        return this.canEdit(ds);
    }
    
}