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
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { StudyService } from '../../studies/shared/study.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';


@Component({
    selector: 'examination-list',
    templateUrl: 'examination-list.component.html',
    styleUrls: ['examination-list.component.css'],
})
export class ExaminationListComponent extends EntityListComponent<Examination>{

    @ViewChild('table') table: TableComponent;
    private studiesICanAdmin: number[];

    constructor(
            private examinationService: ExaminationService,
            private studyService: StudyService) {
        
        super('examination');
        this.studyService.findStudiesIcanAdmin().then(ids => this.studiesICanAdmin = ids);
    }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.examinationService.getPage(pageable);
    }

    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        let colDef: any[] = [
            { headerName: "Examination id", field: "id" },
            {
                headerName: "Subject", field: "subject.name", cellRenderer: function (params: any) {
                    return (params.data.subject) ? params.data.subject.name : "";
                }
            },{
                headerName: "Examination date", field: "examinationDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.examinationDate);
                }, width: "100px"
            },{
                headerName: "Research study", field: "study.name",
                route: (examination: Examination) => '/study/details/' + examination.study.id
            },{
                headerName: "Center", field: "centerName",
                route: (examination: Examination) => '/center/details/' + examination.center.id
            }
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: false,
            view: true, 
            edit: false, 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    canDelete(exam: Examination): boolean {
        return this.keycloakService.isUserAdmin() || (
            exam.study &&
            this.studiesICanAdmin.includes(exam.study.id)
        );
    }
}