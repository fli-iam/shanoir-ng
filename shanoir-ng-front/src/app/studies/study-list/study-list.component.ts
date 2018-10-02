import { Component, ViewChild } from '@angular/core';

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { StudyStatus } from '../shared/study-status.enum';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';

@Component({
    selector: 'study-list',
    templateUrl: 'study-list.component.html',
    styleUrls: ['study-list.component.css']
})

export class StudyListComponent extends BrowserPaginEntityListComponent<Study> {

    @ViewChild('table') table: TableComponent;
    
    constructor(
            private studyService: StudyService) {
        
        super('study');
    }

    getEntities(): Promise<Study[]> {
        return this.studyService.getAll();
    }

    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        let colDef: any[] = [
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
}