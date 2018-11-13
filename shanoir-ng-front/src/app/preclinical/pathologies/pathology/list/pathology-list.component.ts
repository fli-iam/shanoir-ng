import {Component,ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { Pathology } from '../shared/pathology.model';
import { PathologyService } from '../shared/pathology.service';

import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';
import { FilterablePageable, Page } from '../../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';

@Component({
  selector: 'pathology-list',
  templateUrl:'pathology-list.component.html',
  styleUrls: ['pathology-list.component.css'], 
  providers: [PathologyService]
})
export class PathologiesListComponent extends BrowserPaginEntityListComponent<Pathology>{
    @ViewChild('pathologiesTableTable') table: TableComponent;
    
    constructor(
        private pathologyService: PathologyService) {
            super('preclinical-pathology');
     }
    
    getEntities(): Promise<Pathology[]> {
        return this.pathologyService.getAll();
    }
    
    getColumnDefs(): any[] {
        function castToString(id: number) {
            return String(id);
        };
        let colDef: any[] = [
            {headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }},
            {headerName: "Name", field: "name"}
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    
}