import {Component, ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";


import { PreclinicalSubject } from '../shared/preclinicalSubject.model';
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { AnimalSubject } from '../shared/animalSubject.model';
import { Subject } from '../shared/subject.model';
import { AnimalSubjectService } from '../shared/animalSubject.service';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';

@Component({
  selector: 'animalSubject-list',
  templateUrl:'animalSubject-list.component.html',
  styleUrls: ['animalSubject-list.component.css'], 
  providers: [AnimalSubjectService]
})
export class AnimalSubjectsListComponent {
  public preclinicalSubjects: PreclinicalSubject[];
  public animalSubjects: AnimalSubject[];
  public subjects: Subject[];
  public loading: boolean = false;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
    
    constructor(
        public animalSubjectService: AnimalSubjectService,
        public router: Router,
        public confirmDialogService: ConfirmDialogService, 
        private keycloakService: KeycloakService,
        private viewContainerRef: ViewContainerRef) {
            this.getAnimalSubjects();
            this.createColumnDefs();
     }
    
    getAnimalSubjects(): void {
    	this.preclinicalSubjects = [];
    	
        this.loading = true;
        this.animalSubjectService.getAnimalSubjects().then(animalSubjects => {
            if(animalSubjects){
                this.animalSubjects = animalSubjects;
            }else{
                this.animalSubjects = [];
            }
            // load subjects
            this.animalSubjectService.getSubjects().then(subjects => {
            	if(subjects){
                	this.subjects = subjects;
                	// build preclinical subject
                	this.preclinicalSubjects = [];
    				if (this.animalSubjects){
    					for (let s of this.animalSubjects){
    						let preSubject: PreclinicalSubject = new PreclinicalSubject();
    						preSubject.animalSubject = s;
    						preSubject.id = s.id;
    						preSubject.subject = this.getSubjectWithId(s.subjectId);
    						this.preclinicalSubjects.push(preSubject);
    					}
    				}
            	}else{
                	this.subjects = [];
            	}
            	this.loading = false;
        	}).catch((error) => {
            	this.subjects = [];
        	}); 
        }).catch((error) => {
            this.animalSubjects = [];
        }); 
    }
    
    
    
    
    getSubjectWithId(subjectId: number): Subject {
    	if (this.subjects){
    		for (let s of this.subjects){
    			if (s.id == subjectId){
    				return s;
    			}
    		}
    	}
    }
    
    
    delete(preclinicalSubject: PreclinicalSubject): void {    
      this.animalSubjectService.delete(preclinicalSubject.animalSubject.id).then((res) => {
      	this.animalSubjectService.deleteSubject(preclinicalSubject.subject.id).then((res2) => {
      		this.getAnimalSubjects();
      	})
      }
      );
    }
        
    // Grid columns definition
    private createColumnDefs() {
        function castToString(id: number) {
            return String(id);
        };
        this.columnDefs = [
            {headerName: "Common name", field: "subject.identifier"},
            {headerName: "Imaged object category", field: "subject.imagedObjectCategory"},
            {headerName: "Specie", field: "animalSubject.specie.value"},
            {headerName: "Strain", field: "animalSubject.strain.value"},
            {headerName: "Biological type", field: "animalSubject.biotype.value"},
            {headerName: "Provider", field: "animalSubject.provider.value"},
            {headerName: "Stabulation", field: "animalSubject.stabulation.value"}
           
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
            	{headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteSubjectConfirmDialog},
            	{headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/preclinical/subject", getParams: function(item: any): Object {
                	return {id: item.id, mode: "edit"};
            	}}
            );
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target : "/preclinical/subject", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        
        
        this.customActionDefs = [];
            if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({title: "new subject", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical/subject", getParams: function(item: any): Object {
                    return {mode: "create"};
            }});
            this.customActionDefs.push({title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
            }
            if (!this.keycloakService.isUserGuest()) {
                this.rowClickAction = {target : "/preclinical/subject", getParams: function(item: any): Object {
                        return {id: item.id, mode: "view"};
                }};
            }
    }
    
    openDeleteSubjectConfirmDialog = (item: PreclinicalSubject) => {
         this.confirmDialogService
                .confirm('Delete subject', 'Are you sure you want to delete subject ' + item.animalSubject.id + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let subject of this.preclinicalSubjects) {
            if (subject["isSelectedInTable"]) ids.push(subject.animalSubject.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}