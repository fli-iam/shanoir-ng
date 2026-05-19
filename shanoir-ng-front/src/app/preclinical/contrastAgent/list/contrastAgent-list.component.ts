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

import {Component, Input, ViewChild} from '@angular/core'

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { ContrastAgent } from '../shared/contrastAgent.model';
import { ContrastAgentService } from '../shared/contrastAgent.service';
import { InjectionInterval } from "../../shared/enum/injectionInterval";
import { InjectionSite } from "../../shared/enum/injectionSite";
import { InjectionType } from "../../shared/enum/injectionType";
import { TableComponent } from '../../../shared/components/table/table.component';
import { ColumnDefinition } from '../../../shared/components/table/column.definition.type';
import { BrowserPaginEntityListComponent } from '../../../shared/components/entity/entity-list.browser.component.abstract';

@Component({
    selector: 'contrast-agent-list',
    templateUrl: 'contrastAgent-list.component.html',
    styleUrls: ['contrastAgent-list.component.css'],
    standalone: false
})
export class ContrastAgentsListComponent extends BrowserPaginEntityListComponent<ContrastAgent>{
  @Input() protocol_id:number;
  @ViewChild('contrastAgentTable') table: TableComponent;
  
   
    constructor(
        private contrastAgentsService: ContrastAgentService)
        {
            super('preclinical-contrast-agent');
        }

    getService(): EntityService<ContrastAgent> {
        return this.contrastAgentsService;
    }
    
    getEntities(): Promise<ContrastAgent[]> {
        return this.contrastAgentsService.getContrastAgents(this.protocol_id);
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            {headerName: "Name", field: "name.value"},
            {headerName: "Manufactured Name", field: "manufacturedName", type: "string"},
            {headerName: "Concentration", field: "concentration", type: "number"},
            {headerName: "Concentration Unit", field: "concentrationUnit.value"},
            {headerName: "Dose", field: "dose", type: "number"},
            {headerName: "Dose Unit", field: "doseUnit.value"},
            {headerName: "Injection interval", field: "injectionInterval", type: "string", cellRenderer: function (params: any) {
                return InjectionInterval[params.data.injectionInterval];
            }},
            {headerName: "Injection site", field: "injectionSite", type: "string", cellRenderer: function (params: any) {
                return InjectionSite[params.data.injectionSite];
            }},
            {headerName: "Injection type", field: "injectionType", type: "string", cellRenderer: function (params: any) {
                return InjectionType[params.data.injectionType];
            }}  
        ];   
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    
}