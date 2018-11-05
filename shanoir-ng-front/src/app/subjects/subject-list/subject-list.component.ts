import { Component, ViewChild } from '@angular/core';

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';

@Component({
    selector: 'subject-list',
    templateUrl: 'subject-list.component.html',
    styleUrls: ['subject-list.component.css']
})

export class SubjectListComponent extends BrowserPaginEntityListComponent<Subject> {
    
    @ViewChild('table') table: TableComponent;

    constructor(private subjectService: SubjectService) {       
        super('subject');
    }

    getEntities(): Promise<Subject[]> {
        return this.subjectService.getAll();
    }

    // Grid columns definition
    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        return [
            { headerName: "Common Name", field: "name", defaultSortCol: true, defaultAsc: true },
            { headerName: "Sex", field: "sex" },

            {
                headerName: "Birth Date", field: "birthDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.birthDate);
                }
            },
            { headerName: "Manual HD", field: "manualHemisphericDominance"},
            { headerName: "Language HD", field: "languageHemisphericDominance"},
            { headerName: "Imaged object category", field: "imagedObjectCategory"},
            { headerName: "Personal Comments", field: ""}
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}
