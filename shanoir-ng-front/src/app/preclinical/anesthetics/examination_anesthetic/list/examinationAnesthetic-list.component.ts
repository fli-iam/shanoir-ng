import {Component, Input, ViewChild, ViewContainerRef} from '@angular/core'

import { ExaminationAnesthetic } from '../shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../shared/examinationAnesthetic.service';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';

@Component({
  selector: 'examination-anesthetics-list',
  templateUrl:'examinationAnesthetic-list.component.html',
  styleUrls: ['examinationAnesthetic-list.component.css'], 
  providers: [ExaminationAnestheticService]
})
export class ExaminationAnestheticsListComponent  extends BrowserPaginEntityListComponent<ExaminationAnesthetic>{
  @ViewChild('examinationAnestheticTable') table: TableComponent; 
    
    
     constructor(
        private examAnestheticsService: ExaminationAnestheticService) {
            super('preclinical-examination-anesthetics');
     }
    
    getEntities(): Promise<ExaminationAnesthetic[]> {
        return this.examAnestheticsService.getAll();
    }
    
    getColumnDefs(): any[] {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        function castToString(id: number) {
            return String(id);
        };
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
            {headerName: "Anesthetic", field: "anesthetic.name"},
            {headerName: "Dose", field: "dose", type: "dose", cellRenderer: function (params: any) {
                return checkNullValue(params.data.dose);
            }},
            {headerName: "Dose Unit", field: "dose_unit.value", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.dose_unit.value);
            }},
            {headerName: "Injection interval", field: "injectionInterval"},
            {headerName: "Injection site", field: "injectionSite"},
            {headerName: "Injection type", field: "injectionType"},
            {headerName: "Start Date", field: "startDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.startDate);
            }},
            {headerName: "End Date", field: "endDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.endDate);
            }}      
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    
    
    
}