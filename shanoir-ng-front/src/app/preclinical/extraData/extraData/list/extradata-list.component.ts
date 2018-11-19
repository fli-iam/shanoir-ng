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