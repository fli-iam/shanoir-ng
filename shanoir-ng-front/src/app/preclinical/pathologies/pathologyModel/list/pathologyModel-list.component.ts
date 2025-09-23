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

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { PathologyModel } from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { ColumnDefinition } from '../../../../shared/components/table/column.definition.type';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { SubjectPathologyService } from '../../subjectPathology/shared/subjectPathology.service';
import { ShanoirError } from '../../../../shared/models/error.model';


@Component({
    selector: 'pathologyModel-list',
    templateUrl: 'pathologyModel-list.component.html',
    styleUrls: ['pathologyModel-list.component.css'],
    standalone: false
})
export class PathologyModelsListComponent extends BrowserPaginEntityListComponent<PathologyModel> {
  @ViewChild('modelsTable') table: TableComponent;
    
  constructor(
    private modelService: PathologyModelService, 
    private subjectPathologyService: SubjectPathologyService) 
    {
        super('preclinical-pathology-model');
    }

    getService(): EntityService<PathologyModel> {
        return this.modelService;
    }
    
    getEntities(): Promise<PathologyModel[]> {
        return this.modelService.getAll();
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            {headerName: "Name", field: "name"},
            {headerName: "Pathology", field: "pathology.name"},
            {headerName: "Comment", field: "comment"},
            {headerName: "Specifications file", field: "filename", type: "string"} 
            
        ];
        if (this.keycloakService.isUserAdminOrExpert()) {
            colDef.push({headerName: "", type: "button", awesome: "fa-solid fa-download",action: item => this.downloadModelSpecifications(item) });
        }
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }
    
        
    downloadModelSpecifications = (model:PathologyModel) => {
    	if (model.filename){
        	this.modelService.downloadFile(model);
        }else{
        	this.openInformationDialog(model);
        }
    }
    
    openInformationDialog = (model:PathologyModel) => {
        this.confirmDialogService
            .confirm('Download Specifications', 'No specifications have been found for '+model.name)
            .then(res => {
                
            })
    }
    
    
    protected openDeleteConfirmDialog = (entity: PathologyModel) => {
        this.subjectPathologyService.getAllSubjectForPathologyModel(entity.id).then(subjectPathologies => {
    		if (subjectPathologies){
    			let hasSubjects: boolean  = false;
    			hasSubjects = subjectPathologies.length > 0;
    			if (hasSubjects){
    				this.confirmDialogService
                		.confirm('Delete pathology model', 'This pathology model is linked to subjects, it can not be deleted');
    			}else{
    				this.openDeletePathologyModelConfirmDialog(entity);
    			}
    		}else{
    			this.openDeletePathologyModelConfirmDialog(entity);
    		}
    	}).catch((error) => {
            this.openDeletePathologyModelConfirmDialog(entity);
            throw error;
    	});     
    }   
    
 
    private openDeletePathologyModelConfirmDialog = (entity: PathologyModel) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-pathology-model n° ' + entity.id + ' ?'
            ).then(res => {
                if (res) {
                    this.getService().delete(entity.id).then(() => {
                        this.onDelete.next({entity: entity});
                        this.table.refresh();
                        this.consoleService.log('info', 'The preclinical-pathology-model n° ' + entity.id + ' was sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next({entity: entity, error: new ShanoirError(reason)});
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });                    
                }
            }) 
 	}
 
}