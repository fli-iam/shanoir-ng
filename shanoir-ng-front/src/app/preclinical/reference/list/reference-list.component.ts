import {Component,ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";

import { Reference } from '../shared/reference.model';
import { ReferenceService } from '../shared/reference.service';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';

@Component({
  selector: 'reference-list',
  templateUrl:'reference-list.component.html',
  styleUrls: ['reference-list.component.css'], 
  providers: [ReferenceService]
})
    
export class ReferencesListComponent {
  public references: Reference[];
  public loading: boolean = false;
  public columnDefs: any[];
  public customActionDefs: any[];
  public rowClickAction: Object;
    
    constructor(
        private referenceService: ReferenceService,
        private keycloakService: KeycloakService,
        private router: Router,
        private confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            this.getReferences(); 
            this.createColumnDefs();
     }
    
       
    getReferences(): void {
        this.loading = true;
        this.references = [];
        this.referenceService.getReferences().then(references => {
            if(references){
                this.references = references;    
            }
            this.loading = false;
        }).catch((error) => {
            console.log('error '+error)
        });
    }

    
    delete(reference: Reference): void {      
      this.referenceService.delete(reference).then((res) => this.getReferences());
    }
    
        
    // Grid columns definition
    private createColumnDefs() {
        function castToString(id: number) {
            return String(id);
        };
        this.columnDefs = [
            /*{headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }},*/
            {headerName: "Category", field: "category"},
            {headerName: "Type", field: "reftype"},
            {headerName: "Value", field: "value"}     
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteReferenceConfirmDialog},
            {headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/preclinical-reference", getParams: function(item: any): Object {
                return {id: item.id, mode: "edit"};
            }});
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img:  ImagesUrlUtil.VIEW_ICON_PATH, target : "/preclinical-reference", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
        this.customActionDefs.push({title: "new reference", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical-reference", getParams: function(item: any): Object {
                return {mode: "create"};
        }});
        this.customActionDefs.push({title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {target : "/preclinical-reference", getParams: function(item: any): Object {
                    return {id: item.id, mode: "view"};
            }};
        }
        
    }
    
    openDeleteReferenceConfirmDialog = (item: Reference) => {
         this.confirmDialogService
                .confirm('Delete reference', 'Are you sure you want to delete reference ' + item.reftype +' ' + item.value + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let reference of this.references) {
            if (reference["isSelectedInTable"]) ids.push(reference.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }
 

}