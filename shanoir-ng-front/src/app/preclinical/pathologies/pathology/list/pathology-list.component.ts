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