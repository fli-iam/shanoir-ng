import {Component, ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { Therapy } from '../shared/therapy.model';
import { TherapyService } from '../shared/therapy.service';
import { TherapyType } from "../../../shared/enum/therapyType";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';
import { SubjectTherapyService } from '../../subjectTherapy/shared/subjectTherapy.service';
import { FilterablePageable, Page } from '../../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../../shared/components/table/table.component';


@Component({
  selector: 'therapy-list',
  templateUrl:'therapy-list.component.html',
  styleUrls: ['therapy-list.component.css'], 
  providers: [TherapyService]
})
export class TherapiesListComponent {
  public therapies: Therapy[];
  private therapiesPromise: Promise<void> = this.getTherapies();
  private browserPaging: BrowserPaging<Therapy>;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
  @ViewChild('therapiesTable') table: TableComponent;
    
    constructor(
        public therapyService: TherapyService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService,
        public subjectTherapyService: SubjectTherapyService, 
        private viewContainerRef: ViewContainerRef) {
            this.createColumnDefs();
     }   
    
    
    getPage(pageable: FilterablePageable): Promise<Page<Therapy>> {
        return new Promise((resolve) => {
            this.therapiesPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }
    getTherapies(): Promise<void> {
    	this.therapies = [];  
        this.browserPaging = new BrowserPaging(this.therapies, this.columnDefs);
        return this.therapyService.getTherapies().then(therapies => {
            this.therapies = therapies;
            this.browserPaging.setItems(therapies);
            this.table.refresh();
        })            
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
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.checkSubjectsForTherapy},
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
    
    checkSubjectsForTherapy= (item: Therapy) => {
 		 this.subjectTherapyService.getAllSubjectForTherapy(item.id).then(subjectTherapies => {
    		if (subjectTherapies){
    			let hasSubjects: boolean  = false;
    			hasSubjects = subjectTherapies.length > 0;
    			if (hasSubjects){
    				this.confirmDialogService
                		.confirm('Delete therapy', 'This therapy is linked to subjects, it can not be deleted', 
                    		this.viewContainerRef)
    			}else{
    				this.openDeleteTherapyConfirmDialog(item);
    			}
    		}else{
    			this.openDeleteTherapyConfirmDialog(item);
    		}
    	}).catch((error) => {
    		console.log(error);
    		this.openDeleteTherapyConfirmDialog(item);
    	});    
 	}
}