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

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { EntityListComponent } from 'src/app/shared/components/entity/entity-list.component.abstract';
import { Pageable, Page } from 'src/app/shared/components/table/pageable.model';


@Component({
    selector: 'subject-list',
    templateUrl: 'subject-list.component.html',
    styleUrls: ['subject-list.component.css']
})

export class SubjectListComponent extends EntityListComponent<Subject> {

    getPage(pageable: Pageable): Promise<Page<Subject>> {
        return this.subjectService.getPage(pageable, this.table.filter.searchStr ? this.table.filter.searchStr : "");
    }

    @ViewChild('table', { static: false }) table: TableComponent;
    private studiesICanAdmin: number[];

    constructor(
            private subjectService: SubjectService,
            private studyService: StudyService) {
        super('subject');
        this.studyService.findStudyIdsIcanAdmin().then(ids => this.studiesICanAdmin = ids);
    }

    getService(): EntityService<Subject> {
        return this.subjectService;
    }

    getEntities(): Promise<Subject[]> {
        return this.subjectService.getClinicalSubjects()
    }

    // Grid columns definition
    getColumnDefs(): ColumnDefinition[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        return [
            { headerName: "Common Name", field: "name", defaultSortCol: true, defaultAsc: true },
            { headerName: "Sex", field: "sex", disableSearch: true },

            {
                headerName: "Birth Date", field: "birthDate", type: "date", disableSearch: true, cellRenderer: function (params: any) {
                    return dateRenderer(params.data.birthDate);
                }
            },
            { headerName: "Manual HD", field: "manualHemisphericDominance", disableSearch: true},
            { headerName: "Language HD", field: "languageHemisphericDominance", disableSearch: true},
            { headerName: "Imaged object category", field: "imagedObjectCategory", disableSearch: true}
        ];
    }

    completeColDefs() {
        super.completeColDefs();
        this.columnDefs[this.columnDefs.findIndex(col => col.headerName == "Id")].disableSearch = true;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: this.keycloakService.isUserAdminOrExpert(),
            view: true,
            edit: this.keycloakService.isUserAdminOrExpert(),
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    canDelete(subject: Subject): boolean {
        return this.keycloakService.isUserAdmin() || (
            subject.subjectStudyList &&
            subject.subjectStudyList.filter(ss => this.studiesICanAdmin?.includes(ss.study.id)).length > 0
        );
    }
}
