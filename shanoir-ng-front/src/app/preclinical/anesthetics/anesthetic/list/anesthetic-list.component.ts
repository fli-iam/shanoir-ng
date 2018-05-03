import {Component, ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { Anesthetic } from '../shared/anesthetic.model';
import { AnestheticService } from '../shared/anesthetic.service';
import { AnestheticType } from "../../../shared/enum/anestheticType";
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';

@Component({
  selector: 'anesthetic-list',
  templateUrl:'anesthetic-list.component.html',
  styleUrls: ['anesthetic-list.component.css'], 
  providers: [AnestheticService]
})
export class AnestheticsListComponent {
  public anesthetics: Anesthetic[];
  public loading: boolean = false;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
    
    constructor(
        public anestheticsService: AnestheticService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            this.getAnesthetics(); 
            this.createColumnDefs();
     }
    
    getAnesthetics(): void {
        this.loading = true;
        this.anestheticsService.getAnesthetics().then(anesthetics => {
            if(anesthetics){
                this.anesthetics = anesthetics;
            }else{
                this.anesthetics = [];
            }
            this.loading = false;
        }).catch((error) => {
            this.anesthetics = [];
        }); 
    }
    
    
    delete(anesthetic:Anesthetic): void {      
      this.anestheticsService.delete(anesthetic.id).then((res) => this.getAnesthetics());
    }
    
    /*editAnesthetic = (anesthetic:Anesthetic) => {
        this.router.navigate(['/preclinical/anesthetics/edit/', anesthetic.id]);
    }*/
    
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
        function checkNullValueReference(reference: any) {
            if(reference){
                return reference.value;
            }
            return '';
        };
        this.columnDefs = [
            /*{headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }},*/
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
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteAnestheticConfirmDialog},
            {headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/preclinical/anesthetic", getParams: function(item: any): Object {
                return {id: item.id, mode: "edit"};
            }});
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target : "/preclinical/anesthetic", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
        this.customActionDefs.push({title: "new anesthetic", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical/anesthetic", getParams: function(item: any): Object {
                return {mode: "create"};
        }});
        this.customActionDefs.push({title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {target : "/preclinical/anesthetic", getParams: function(item: any): Object {
                    return {id: item.id, mode: "view"};
            }};
        }
    }
    
    openDeleteAnestheticConfirmDialog = (item: Anesthetic) => {
         this.confirmDialogService
                .confirm('Delete anesthetic', 'Are you sure you want to delete anesthetic ' + item.id + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let anesthetic of this.anesthetics) {
            if (anesthetic["isSelectedInTable"]) ids.push(anesthetic.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}