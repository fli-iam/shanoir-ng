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
import { capitalsAndUnderscoresToDisplayable } from '../../utils/app.utils';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';


@Component({
    selector: 'study-list',
    templateUrl: 'study-list.component.html',
    styleUrls: ['study-list.component.css']
})

export class StudyListComponent extends BrowserPaginEntityListComponent<Study> {

    @ViewChild('table', { static: false }) table: TableComponent;
    
    constructor(
        private studyService: StudyService) {
            
        super('study');
    }
    
    getService(): EntityService<Study> {
        return this.studyService;
    }

    getEntities(): Promise<Study[]> {
        return this.studyService.getAll();
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            { headerName: "Name", field: "name" },
            {
                headerName: "Status", field: "studyStatus", width: '70px', cellRenderer: function (params: any) {
                    return capitalsAndUnderscoresToDisplayable(params.data.studyStatus);
                }
            },
            {
                headerName: "Start date", field: "startDate", type: "date", cellRenderer: (params: any) => {
                    return this.dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End date", field: "endDate", type: "date", cellRenderer: (params: any) => {
                    return this.dateRenderer(params.data.endDate);
                }
            },
            {
                headerName: "Subjects", field: "nbSujects", type: "number", width: '30px'
            },
            {
                headerName: "Examinations", field: "nbExaminations", type: "number", width: '30px'
            }
        ];
        return colDef;
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

    canEdit(study: Study): boolean {
        return this.keycloakService.isUserAdmin() || (
            study.studyUserList && 
            study.studyUserList.filter(su => su.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE)).length > 0
        );
    }

    canDelete(study: Study): boolean {
        // Disallow delete study from list directly
        return false
    }
}