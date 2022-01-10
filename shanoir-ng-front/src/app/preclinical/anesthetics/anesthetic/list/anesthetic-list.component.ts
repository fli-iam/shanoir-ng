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
import { Component, ViewChild } from '@angular/core';

import {
    BrowserPaginEntityListComponent,
} from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { ShanoirError } from '../../../../shared/models/error.model';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { AnestheticType } from '../../../shared/enum/anestheticType';
import { ExaminationAnestheticService } from '../../examination_anesthetic/shared/examinationAnesthetic.service';
import { Anesthetic } from '../shared/anesthetic.model';
import { AnestheticService } from '../shared/anesthetic.service';


@Component({
  selector: 'anesthetic-list',
  templateUrl:'anesthetic-list.component.html',
  styleUrls: ['anesthetic-list.component.css'], 
  providers: [AnestheticService]
})
export class AnestheticsListComponent  extends BrowserPaginEntityListComponent<Anesthetic>{
    
    @ViewChild('anestheticsTable', { static: false }) table: TableComponent;
    
    constructor(
        private anestheticsService: AnestheticService, 
        private examinationAnestheticService: ExaminationAnestheticService
    ) 
    {
        super('preclinical-anesthetic');
    }
    
    getService(): EntityService<Anesthetic> {
        return this.anestheticsService;
    }
    
    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }    
    
    getEntities(): Promise<Anesthetic[]> {
        return this.anestheticsService.getAll();
    }
    
    getColumnDefs(): any[] {
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        let colDef: any[] = [
            {headerName: "Name", field: "name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.name);
            }},
            {headerName: "Type", field: "anestheticType", type: "Enum", cellRenderer: function (params: any) {
                return AnestheticType[params.data.anestheticType];
            }},
            {headerName: "Comment", field: "comment", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.comment);
            }}    
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    
    protected openDeleteConfirmDialog = (entity: Anesthetic) => {
        this.examinationAnestheticService.getAllExaminationForAnesthetic(entity.id).then(examinationAnesthetics => {
    		if (examinationAnesthetics){
    			let hasExams: boolean  = false;
    			hasExams = examinationAnesthetics.length > 0;
    			if (hasExams){
                    this.confirmDialogService
                        .confirm('Delete anesthetic', 'This anesthetic is linked to preclinical examinations, it can not be deleted');
    			}else{
    				this.openDeleteAnestheticConfirmDialog(entity);
    			}
    		}else{
    			this.openDeleteAnestheticConfirmDialog(entity);
    		}
    	}).catch((error) => {
            this.openDeleteAnestheticConfirmDialog(entity);
            throw error;
    	});    
    }   

    private openDeleteAnestheticConfirmDialog = (entity: Anesthetic) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-anesthetic n° ' + entity.id + ' ?'
            ).then(res => {
                if (res) {
                    this.getService().delete(entity.id).then(() => {
                        this.onDelete.next(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The preclinical-anesthetic sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next(new ShanoirError(reason));
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });                    
                }
            })
    }
    
    
}