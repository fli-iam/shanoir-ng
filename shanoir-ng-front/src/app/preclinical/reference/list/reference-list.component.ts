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

import {Component,ViewChild} from '@angular/core';

import { Reference } from '../shared/reference.model';
import { ReferenceService } from '../shared/reference.service';
import { TableComponent } from '../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../shared/components/entity/entity-list.browser.component.abstract';

@Component({
  selector: 'reference-list',
  templateUrl:'reference-list.component.html',
  styleUrls: ['reference-list.component.css'], 
  providers: [ReferenceService]
})
    
export class ReferencesListComponent  extends BrowserPaginEntityListComponent<Reference>{
  @ViewChild('referenceTable') table: TableComponent;
    
    constructor(
        private referenceService: ReferenceService) {
            super('preclinical-reference');
     }
    
    getEntities(): Promise<Reference[]> {
        return this.referenceService.getAll();
    }
    
    getColumnDefs(): any[] {
        let colDef: any[] = [
            {headerName: "Category", field: "category"},
            {headerName: "Type", field: "reftype"},
            {headerName: "Value", field: "value"}     
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    

}