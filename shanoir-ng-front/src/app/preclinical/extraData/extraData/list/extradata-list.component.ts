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

import {Component, Input, ViewChild, ViewContainerRef, OnChanges} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { ExtraData } from '../shared/extradata.model';
import {ExtraDataService } from '../shared/extradata.service';
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';
import { FilterablePageable, Page } from '../../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';


@Component({
  selector: 'extradata-list',
  templateUrl:'extradata-list.component.html',
  styleUrls: ['extradata-list.component.css'], 
  providers: [ExtraDataService]
})
export class ExtraDataListComponent  extends BrowserPaginEntityListComponent<ExtraData> {
    @Input() examination_id:number;
    @ViewChild('extradataTable') table: TableComponent;

    constructor(
        private extradataService: ExtraDataService) {
            super('preclinical-extradata');
     }
     
    getService(): EntityService<ExtraData> {
        return this.extradataService;
    }
    
    getEntities(): Promise<ExtraData[]> {
        if(this.examination_id){
            return this.extradataService.getExtraDatas(this.examination_id);
        }
    }
    
    getColumnDefs(): any[] {
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        let colDef: any[] = [
            {headerName: "Filename", field: "filename"},
            {headerName: "Data type", field: "extradatatype"},
            {headerName: "Has heart rate", field: "has_heart_rate", type: "boolean", cellRenderer: function (params: any) {
                return checkNullValue(params.data.has_heart_rate);
            }},
            {headerName: "Has respiratory rate", field: "has_respiratory_rate", type: "boolean", cellRenderer: function (params: any) {
                return checkNullValue(params.data.has_respiratory_rate);
            }},
            {headerName: "Has sao2", field: "has_sao2", type: "boolean", cellRenderer: function (params: any) {
                return checkNullValue(params.data.has_sao2);
            }},
            {headerName: "Has temperature", field: "has_temperature", type: "boolean", cellRenderer: function (params: any) {
                return checkNullValue(params.data.has_temperature);
            }}
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
      
    downloadExtraData = (extradata:ExtraData) => {
        window.open(this.extradataService.getDownloadUrl(extradata));
    }
    
    
    
}