import {Component, ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { Therapy } from '../shared/therapy.model';
import { TherapyService } from '../shared/therapy.service';
import { TherapyType } from "../../../shared/enum/therapyType";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';

@Component({
  selector: 'therapy-list',
  templateUrl:'therapy-list.component.html',
  styleUrls: ['therapy-list.component.css'], 
  providers: [TherapyService]
})
export class TherapiesListComponent {
  public therapies: Therapy[];
  public loading: boolean = false;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
    
    constructor(
        public therapyService: TherapyService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            this.getTherapies();
            this.createColumnDefs();
     }   
    
    getTherapies(): void {
        this.loading = true;
        this.therapyService.getTherapies().then(therapies => {
            if(therapies){
                this.therapies = therapies;
            }else{
                this.therapies = [];  
            }
            this.loading = false;
        }).catch((error) => {
             this.therapies = [];  
        });              
    }
    
    
    delete(therapy: Therapy): void {      
      this.therapyService.delete(therapy.id).then((res) => this.getTherapies());
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
            {headerName: "Name", field: "name"},
            {headerName: "Type", field: "therapyType", type: "Enum", cellRenderer: function (params: any) {
                return TherapyType[params.data.therapyType];
            }},
            {headerName: "Comment", field: "comment"}
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteTherapyConfirmDialog},
            {headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/preclinical-therapy", getParams: function(item: any): Object {
                return {id: item.id, mode: "edit"};
            }});
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target : "/preclinical-therapy", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
        this.customActionDefs.push({title: "new therapy", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical-therapy", getParams: function(item: any): Object {
                return {mode: "create"};
        }});
        this.customActionDefs.push({title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {target : "/preclinical-therapy", getParams: function(item: any): Object {
                    return {id: item.id, mode: "view"};
            }};
        }
    }
    
    openDeleteTherapyConfirmDialog = (item: Therapy) => {
         this.confirmDialogService
                .confirm('Delete therapy', 'Are you sure you want to delete therapy ' + item.name + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let therapy of this.therapies) {
            if (therapy["isSelectedInTable"]) ids.push(therapy.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }
}