import {Component,ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router';

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { ConfirmDialogComponent } from "../../../../shared/components/confirm-dialog/confirm-dialog.component";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { PathologyModel } from '../shared/pathologyModel.model';
import { PathologyModelService } from '../shared/pathologyModel.service';
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';


@Component({
  selector: 'pathologyModel-list',
  templateUrl:'pathologyModel-list.component.html',
  styleUrls: ['pathologyModel-list.component.css'], 
  providers: [PathologyModelService]
})
export class PathologyModelsListComponent {
  public models: PathologyModel[];
  public loading: boolean = false;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
    
    constructor(
        public modelService: PathologyModelService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, 
        private viewContainerRef: ViewContainerRef) {
            this.getPathologyModels();
            this.createColumnDefs();
     }
    
    
    getPathologyModels(): void {
        this.loading = true;
        this.modelService.getPathologyModels().then(models => {
            if(models){
                this.models = models;
            }else{
                this.models = [];
            }
            this.loading = false;
        }).catch((error) => {
            this.models = [];
        }); 
    }
    
    
    delete(model: PathologyModel): void {      
      this.modelService.delete(model.id).then((res) => this.getPathologyModels());
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
            this.viewContainerRef)
            .subscribe(res => {
                
            })
    }
    
    
    // Grid columns definition
    private createColumnDefs() {
        function castToString(id: number) {
            return String(id);
        };
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        this.columnDefs = [
            /*{headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }},*/
            {headerName: "Name", field: "name"},
            {headerName: "Pathology", field: "pathology.name"},
            {headerName: "Comment", field: "comment"},
            {headerName: "Specifications file", field: "filename", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.filename);
            }}
        ];
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.DOWNLOAD_ICON_PATH, action: this.downloadModelSpecifications,component:this});
        }
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeletePathologyModelConfirmDialog},
            {headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/preclinical/pathologies/model", getParams: function(item: any): Object {
                return {id: item.id, mode: "edit"};
            }});
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target : "/preclinical/pathologies/model", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
        this.customActionDefs.push({title: "new pathology model", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical/pathologies/model", getParams: function(item: any): Object {
                return {mode: "create"};
        }});
        this.customActionDefs.push({title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {target : "/preclinical/pathologies/model", getParams: function(item: any): Object {
                    return {id: item.id, mode: "view"};
            }};
        }
    }
    
    openDeletePathologyModelConfirmDialog = (item: PathologyModel) => {
         this.confirmDialogService
                .confirm('Delete pathology model', 'Are you sure you want to delete pathology model ' + item.name + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
 
    deleteAll = () => {
        let ids: number[] = [];
        for (let model of this.models) {
            if (model["isSelectedInTable"]) ids.push(model.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }
}