import {Component, Input, ViewChild} from '@angular/core'
import { ContrastAgent } from '../shared/contrastAgent.model';
import { ContrastAgentService } from '../shared/contrastAgent.service';
import { InjectionInterval } from "../../shared/enum/injectionInterval";
import { InjectionSite } from "../../shared/enum/injectionSite";
import { InjectionType } from "../../shared/enum/injectionType";
import { TableComponent } from '../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../shared/components/entity/entity-list.browser.component.abstract';

@Component({
  selector: 'contrast-agent-list',
  templateUrl:'contrastAgent-list.component.html',
  styleUrls: ['contrastAgent-list.component.css'], 
  providers: [ContrastAgentService]
})
export class ContrastAgentsListComponent extends BrowserPaginEntityListComponent<ContrastAgent>{
  @Input() protocol_id:number;
  @ViewChild('contrastAgentTable') table: TableComponent;
  
   
    constructor(
        private contrastAgentsService: ContrastAgentService)
        {
            super('preclinical-contrast-agent');
        }

    getEntities(): Promise<ContrastAgent[]> {
        return this.contrastAgentsService.getContrastAgents(this.protocol_id);
    }

    getColumnDefs(): any[] {
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        function checkNullValueReference(reference: any) {
            if(reference){
                return reference.value;
            }
            return '';
        };
        let colDef: any[] = [
            {headerName: "Name", field: "name", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.name);
            }},
            {headerName: "Manufactured Name", field: "manufactured_name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.manufactured_name);
            }},
            {
                headerName: "Concentration", field: "concentration", type: "number", cellRenderer: function(params: any) {
                    return checkNullValue(params.data.concentration);
                }
            },
            {headerName: "Concentration Unit", field: "concentration_unit", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.concentration_unit);
            }},
            {headerName: "Dose", field: "dose", type: "dose", cellRenderer: function (params: any) {
                return checkNullValue(params.data.dose);
            }},
            {headerName: "Dose Unit", field: "dose_unit", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.dose_unit);
            }},
            {headerName: "Injection interval", field: "injection_interval", type: "string", cellRenderer: function (params: any) {
                return InjectionInterval[params.data.injection_interval];
            }},
            {headerName: "Injection site", field: "injection_site", type: "string", cellRenderer: function (params: any) {
                return InjectionSite[params.data.injection_site];
            }},
            {headerName: "Injection type", field: "injection_type", type: "string", cellRenderer: function (params: any) {
                return InjectionType[params.data.injection_type];
            }}  
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    
}