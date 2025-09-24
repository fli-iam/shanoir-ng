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
import {Component, ViewChild} from '@angular/core';

import {EntityService} from 'src/app/shared/components/entity/entity.abstract.service';

import {
    BrowserPaginEntityListComponent
} from '../../../shared/components/entity/entity-list.browser.component.abstract';
import {ColumnDefinition} from '../../../shared/components/table/column.definition.type';
import {TableComponent} from '../../../shared/components/table/table.component';
import {ShanoirError} from '../../../shared/models/error.model';
import {ImagedObjectCategory} from '../../../subjects/shared/imaged-object-category.enum';
import {SubjectService} from '../../../subjects/shared/subject.service';
import {AnimalSubjectService} from '../shared/animalSubject.service';
import {PreclinicalSubject} from '../shared/preclinicalSubject.model';


@Component({
    selector: 'animalSubject-list',
    templateUrl: 'animalSubject-list.component.html',
    styleUrls: ['animalSubject-list.component.css'],
    standalone: false
})
export class AnimalSubjectsListComponent  extends BrowserPaginEntityListComponent<PreclinicalSubject>{

    @ViewChild('preclinicalSubjectsTable', { static: false }) table: TableComponent;

    public preclinicalSubjects: PreclinicalSubject[];

    constructor(
        private animalSubjectService: AnimalSubjectService,
        private subjectService: SubjectService) {
            super('preclinical-subject');
    }

    getService(): EntityService<PreclinicalSubject> {
        return this.animalSubjectService;
    }

    getEntities(): Promise<PreclinicalSubject[]> {

        this.preclinicalSubjects = [];

        return this.subjectService.getPreclinicalSubjects().then(subjects => {

            if (!subjects) {
                return [];
            }

            const subMap = new Map();
            for (let sub of subjects) {
                subMap.set(sub.id, sub);
            }

            return this.animalSubjectService.getAnimalSubjects(subMap.keys()).then(animalSubject => {

                if (!animalSubject) {
                    return [];
                }

                for (let aSub of animalSubject){
                    let preSubject: PreclinicalSubject = new PreclinicalSubject();
                    preSubject.animalSubject = aSub;
                    preSubject.id = aSub.id;
                    preSubject.subject = subMap.get(preSubject.id);
                    this.preclinicalSubjects.push(preSubject);
                }
                return this.preclinicalSubjects;
            });
        });
    }


    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            {headerName: "Common name", field: "subject.name"},
            {headerName: "Imaged object category", field: "subject.imagedObjectCategory", cellRenderer: function (params: any) {
                    if(!params.data.subject){
                        return "";
                    }
                    let imagedObjectCat: ImagedObjectCategory = params.data.subject.imagedObjectCategory;
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

    getOptions() {
        return {
            new: false,
            view: true,
            edit: this.keycloakService.isUserAdminOrExpert(),
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    protected openDeleteConfirmDialog = (entity: PreclinicalSubject) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-subject n° ' + entity.id + ' ?'
            ).then(res => {
                if (res) {
                    this.subjectService.delete(entity.id).then(() => {
                        this.onDelete.next({entity: entity});
                        const index: number = this.preclinicalSubjects.indexOf(entity);
                        if (index !== -1) {
                            this.preclinicalSubjects.splice(index);
                        }
                        this.table.refresh();
                        this.consoleService.log('info', 'The preclinical-subject n°' + entity.id + ' was sucessfully deleted');
                    }
                    ).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next({entity: entity, error: new ShanoirError(reason)});
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });
                }
            }
        )
    }



}
