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
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from '../shared/dataset-processing.service';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';

@Component({
    selector: 'dataset-processing-list',
    templateUrl: 'dataset-processing-list.component.html',
    styleUrls: ['dataset-processing-list.component.css']
})
export class DatasetProcessingListComponent extends BrowserPaginEntityListComponent<DatasetProcessing> {

    @ViewChild('table', { static: false }) table: TableComponent;
    
    constructor(private datasetProcessingService: DatasetProcessingService) {
        super('datasetProcessing');
    }
    
    getService(): EntityService<DatasetProcessing> {
        return this.datasetProcessingService;
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    getEntities(): Promise<DatasetProcessing[]> {
        return this.datasetProcessingService.getAll(); 
    }

    getColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };

        let columnDefs: any[] = [
            { headerName: 'Id', field: 'id', type: 'number', width: '30px', defaultSortCol: true},
            { headerName: "Processing", field: "datasetProcessingType" },
            { headerName: "Comment", field: "comment" },
            { headerName: "Date", field: "processingDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.processingDate) }
        ];
        return columnDefs;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}