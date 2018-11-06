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