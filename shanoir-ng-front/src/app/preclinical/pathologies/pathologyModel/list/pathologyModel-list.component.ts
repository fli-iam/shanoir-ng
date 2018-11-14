import {Component,ViewChild} from '@angular/core';

import { PathologyModel } from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { ServiceLocator } from '../../../../utils/locator.service';
import { SubjectPathologyService } from '../../subjectPathology/shared/subjectPathology.service';
import { ShanoirError } from '../../../../shared/models/error.model';


@Component({
  selector: 'pathologyModel-list',
  templateUrl:'pathologyModel-list.component.html',
  styleUrls: ['pathologyModel-list.component.css'], 
  providers: [PathologyModelService]
})
export class PathologyModelsListComponent   extends BrowserPaginEntityListComponent<PathologyModel> {
  @ViewChild('modelsTable') table: TableComponent;
    
  constructor(
    private modelService: PathologyModelService, 
    private subjectPathologyService: SubjectPathologyService) 
    {
        super('preclinical-pathology-model');
    }

    getEntities(): Promise<PathologyModel[]> {
        return this.modelService.getAll();
    }

    getColumnDefs(): any[] {
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        let colDef: any[] = [
            {headerName: "Name", field: "name"},
            {headerName: "Pathology", field: "pathology.name"},
            {headerName: "Comment", field: "comment"},
            {headerName: "Specifications file", field: "filename", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.filename);
            }} 
            
        ];
        if (!this.keycloakService.isUserGuest()) {
            colDef.push({headerName: "", type: "button", awesome: "fa-download",action: item => this.downloadModelSpecifications(item) });
        }
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    
        
    downloadModelSpecifications = (model:PathologyModel) => {
    	if (model.filename){
        	window.open(this.modelService.getDownloadUrl(model));
        }else{
        	this.openInformationDialog(model);
        }
    }
    
    openInformationDialog = (model:PathologyModel) => {
        this.confirmDialogService
            .confirm('Download Specifications', 'No specifications have been found for '+model.name,
            ServiceLocator.rootViewContainerRef)
            .subscribe(res => {
                
            })
    }
    
    
    protected openDeleteConfirmDialog = (entity: PathologyModel) => {
        this.subjectPathologyService.getAllSubjectForPathologyModel(entity.id).then(subjectPathologies => {
    		if (subjectPathologies){
    			let hasSubjects: boolean  = false;
    			hasSubjects = subjectPathologies.length > 0;
    			if (hasSubjects){
    				this.confirmDialogService
                		.confirm('Delete pathology model', 'This pathology model is linked to subjects, it can not be deleted', 
                        ServiceLocator.rootViewContainerRef)
    			}else{
    				this.openDeletePathologyModelConfirmDialog(entity);
    			}
    		}else{
    			this.openDeletePathologyModelConfirmDialog(entity);
    		}
    	}).catch((error) => {
    		console.log(error);
    		this.openDeletePathologyModelConfirmDialog(entity);
    	});     
    }   
    
 
    private openDeletePathologyModelConfirmDialog = (entity: PathologyModel) => {
        if (this.keycloakService.isUserGuest()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-pathology-model n° ' + entity.id + ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    entity.delete().then(() => {
                        this.onDelete.next(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The preclinical-pathology-model sucessfully deleted');
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