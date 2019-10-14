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

import {Component, ViewChild} from '@angular/core'

import { PreclinicalSubject } from '../shared/preclinicalSubject.model';
import { ImagedObjectCategory } from '../../../subjects/shared/imaged-object-category.enum';
import { AnimalSubject } from '../shared/animalSubject.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { AnimalSubjectService } from '../shared/animalSubject.service';
import { TableComponent } from '../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../shared/components/entity/entity-list.browser.component.abstract';
import { ServiceLocator } from '../../../utils/locator.service';
import { ShanoirError } from '../../../shared/models/error.model';
import { resolve } from 'url';
import { MsgBoxService } from '../../../shared/msg-box/msg-box.service';


@Component({
  selector: 'animalSubject-list',
  templateUrl:'animalSubject-list.component.html',
  styleUrls: ['animalSubject-list.component.css'], 
  providers: [AnimalSubjectService]
})
export class AnimalSubjectsListComponent  extends BrowserPaginEntityListComponent<PreclinicalSubject>{

    @ViewChild('preclinicalSubjectsTable') table: TableComponent;

    public preclinicalSubjects: PreclinicalSubject[];
    public animalSubjects: AnimalSubject[];
    public subjects: Subject[];

    constructor(
        private animalSubjectService: AnimalSubjectService) {
            super('preclinical-subject');
    }


    getEntities(): Promise<PreclinicalSubject[]> {
        return new  Promise<PreclinicalSubject[]>(resolve => {
            this.preclinicalSubjects = [];
            this.animalSubjects = [];
            this.subjects = [];
            Promise.all([
                this.animalSubjectService.getSubjects(),
                this.animalSubjectService.getAnimalSubjects()
            ]).then(([subjects, animalSubjects]) => {
                this.subjects = subjects;
                this.animalSubjects = animalSubjects;
                if (this.animalSubjects){
                    for (let s of this.animalSubjects){
                        let preSubject: PreclinicalSubject = new PreclinicalSubject();
                        preSubject.animalSubject = s;
                        preSubject.id = s.id;
                        preSubject.subject = this.getSubjectWithId(s.subjectId);
                        this.preclinicalSubjects.push(preSubject);
                    }
                }
                resolve(this.preclinicalSubjects);
            });
        });
    }
    

    getColumnDefs(): any[] {
        let colDef: any[] = [
            {headerName: "Common name", field: "subject.name"},
            {headerName: "Imaged object category", field: "subject.imagedObjectCategory", cellRenderer: function (params: any) {
                    let imagedObjectCat: ImagedObjectCategory = <ImagedObjectCategory>params.data.subject.imagedObjectCategory;
                    if (ImagedObjectCategory[imagedObjectCat] === ImagedObjectCategory.PHANTOM) {
                    	return 'Phantom';
                    }else if (ImagedObjectCategory[imagedObjectCat] === ImagedObjectCategory.LIVING_ANIMAL) {
                     	return 'Living animal';
                    }else if (ImagedObjectCategory[imagedObjectCat] === ImagedObjectCategory.ANIMAL_CADAVER) {
                     	return 'Animal cadaver';
                    }else if (ImagedObjectCategory[imagedObjectCat] === ImagedObjectCategory.ANATOMICAL_PIECE) {
                     	return 'Anatomical piece';
                    }
                    return ImagedObjectCategory[imagedObjectCat];
                }
            },
            {headerName: "Species", field: "animalSubject.specie.value"},
            {headerName: "Strain", field: "animalSubject.strain.value"},
            {headerName: "Biological type", field: "animalSubject.biotype.value"},
            {headerName: "Provider", field: "animalSubject.provider.value"},
            {headerName: "Stabulation", field: "animalSubject.stabulation.value"} 
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
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
    
    protected openDeleteConfirmDialog = (entity: PreclinicalSubject) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-subject n° ' + entity.animalSubject.id+ ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    this.animalSubjectService.delete(entity.animalSubject.id).then((res) => {
                        this.animalSubjectService.deleteSubject(entity.subject.id).then((res2) => {
                            const index: number = this.preclinicalSubjects.indexOf(entity);
                            if (index !== -1) {
                                this.preclinicalSubjects.splice(index);
                            }
                            this.table.refresh();
                            this.msgBoxService.log('info', 'The preclinical-subject sucessfully deleted');
                        })
                    }
                    ).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next(new ShanoirError(reason));
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });                
                }
            }
        )
    }

    
    
}