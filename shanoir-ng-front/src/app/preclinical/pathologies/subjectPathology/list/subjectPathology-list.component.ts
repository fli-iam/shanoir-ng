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

import {Component} from '@angular/core'

import { SubjectPathology } from '../shared/subjectPathology.model';
import { SubjectPathologyService } from '../shared/subjectPathology.service';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { SubjectAbstractListInput } from '../../../shared/subjectEntity-list-input.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from '../../../../shared/components/table/column.definition.type';


@Component({
    selector: 'subject-pathology-list',
    templateUrl: 'subjectPathology-list.component.html',
    providers: [
        SubjectPathologyService
    ],
    standalone: false
})

@ModesAware
export class SubjectPathologiesListComponent extends SubjectAbstractListInput<SubjectPathology>{

    constructor(
        private subjectPathologyService: SubjectPathologyService) {
            super('preclinical-subject-pathology');
    }

    getService(): EntityService<SubjectPathology> {
        return this.subjectPathologyService;
    }

    public getEntityName() {
        return ('Pathology');
    }

    protected getEntity() {
        return new SubjectPathology();
    }

    protected addEntity(subjectEntity: SubjectPathology) {
        this.preclinicalSubject.pathologies = this.preclinicalSubject.pathologies.concat(subjectEntity);
    }

    protected getOptions(): any {
        // Specify that we can't view a pathology'
        return {
            view: false
        };
    }

    protected getEntityList() {
        return this.preclinicalSubject.pathologies;
    }

    getEntities(): Promise<SubjectPathology[]> {
        let subjectPathologies: SubjectPathology[] = [];
        if (this.preclinicalSubject && this.preclinicalSubject.animalSubject) {
            if (this.breadcrumbsService.currentStep.isPrefilled("entity") && this.breadcrumbsService.currentStep.isPrefilled("PathologyToCreate") && !this.breadcrumbsService.currentStep.isPrefilled("PathologyToUpdate")) {
                this.breadcrumbsService.currentStep.getPrefilledValue("entity").then(res => {
                    this.preclinicalSubject = res;
                })
            } else if (this.breadcrumbsService.currentStep.isPrefilled("PathologyToUpdate")) {
                this.breadcrumbsService.currentStep.getPrefilledValue("PathologyToUpdate").then(res => {
                    this.preclinicalSubject = res as PreclinicalSubject;
                })
            } else if (this.preclinicalSubject.animalSubject.id) {
                return this.subjectPathologyService.getSubjectPathologies(this.preclinicalSubject).then(st => {
                    this.preclinicalSubject.pathologies = st;
                    return st;
                });
            } else {
                this.preclinicalSubject.pathologies = [];
            }
            return Promise.resolve(this.preclinicalSubject.pathologies);
        }
        return Promise.resolve(subjectPathologies);
    }

     getColumnDefs(): ColumnDefinition[] {
        let columnDefs: ColumnDefinition[] = [
            { headerName: "Pathology", field: "pathology.name" },
            { headerName: "PathologyModel", field: "pathologyModel.name" },
            { headerName: "Location", field: "location.value" },
            { headerName: "Start Date", field: "startDate", type: "date" },
            { headerName: "End Date", field: "endDate", type: "date" },
        ];
         setTimeout(() => {
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-edit", action: item => this.editSubjectEntity(item) });
            }
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeSubjectEntity(item) });
            }
         }, 100)
        return columnDefs;
    }
}
