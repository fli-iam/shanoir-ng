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

import {Component, ViewChild} from '@angular/core'

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { ExaminationAnesthetic } from '../shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../shared/examinationAnesthetic.service';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { ColumnDefinition } from '../../../../shared/components/table/column.definition.type';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';

@Component({
    selector: 'examination-anesthetics-list',
    templateUrl: 'examinationAnesthetic-list.component.html',
    styleUrls: ['examinationAnesthetic-list.component.css'],
    standalone: false
})
export class ExaminationAnestheticsListComponent  extends BrowserPaginEntityListComponent<ExaminationAnesthetic>{
  @ViewChild('examinationAnestheticTable') table: TableComponent; 
    
    
    constructor(private examAnestheticsService: ExaminationAnestheticService) {
        super('preclinical-examination-anesthetics');
    }

    getService(): EntityService<ExaminationAnesthetic> {
        return this.examAnestheticsService;
    }
    
    getEntities(): Promise<ExaminationAnesthetic[]> {
        return this.examAnestheticsService.getAll();
    }
    
    getColumnDefs(): ColumnDefinition[] {
        const colDef: ColumnDefinition[] = [
            {headerName: "Anesthetic", field: "anesthetic.name"},
            {headerName: "Dose", field: "dose", type: "number"},
            {headerName: "Dose Unit", field: "doseUnit.value"},
            {headerName: "Injection interval", field: "injectionInterval"},
            {headerName: "Injection site", field: "injectionSite"},
            {headerName: "Injection type", field: "injectionType"},
            {headerName: "Start Date", field: "startDate", type: "date"},
            {headerName: "End Date", field: "endDate", type: "date"}      
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    
    
    
}