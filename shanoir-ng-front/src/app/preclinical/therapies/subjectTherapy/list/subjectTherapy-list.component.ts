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

import { SubjectTherapy } from '../shared/subjectTherapy.model';
import { SubjectTherapyService } from '../shared/subjectTherapy.service';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { TherapyType } from "../../../shared/enum/therapyType";
import { Frequency } from "../../../shared/enum/frequency";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { SubjectAbstractListInput } from '../../../shared/subjectEntity-list-input.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from '../../../../shared/components/table/column.definition.type';

@Component({
    selector: 'subject-therapy-list',
    templateUrl: './subjectTherapy-list.component.html',
    providers: [
        SubjectTherapyService
    ],
    standalone: false
})

@ModesAware
export class SubjectTherapiesListComponent extends SubjectAbstractListInput<SubjectTherapy> {

    constructor(
        private subjectTherapyService: SubjectTherapyService) {
            super('preclinical-subject-therapy');
    }

    getService(): EntityService<SubjectTherapy> {
        return this.subjectTherapyService;
    }

    public getEntityName() {
        return ('Therapy');
    }

    protected getEntity() {
        return new SubjectTherapy();
    }

    protected getEntityList() {
        return this.preclinicalSubject.therapies;
    }

    protected addEntity(subjectEntity: SubjectTherapy) {
        this.preclinicalSubject.therapies = this.preclinicalSubject.therapies.concat(subjectEntity);
    }

    protected getOptions(): any {
        // Specify that we can't view a therapy
        return {
            view: false
        };
    }

    getEntities(): Promise<SubjectTherapy[]> {
        let subjectTherapies: SubjectTherapy[] = [];
        if (this.preclinicalSubject && this.preclinicalSubject.animalSubject) {
            // Initialize from breadcrumbs cache if existing
            let ts: SubjectTherapy[];
            if (this.breadcrumbsService.currentStep.entity != null && (this.breadcrumbsService.currentStep.entity as PreclinicalSubject).therapies != null) {
                ts = (this.breadcrumbsService.currentStep.entity as PreclinicalSubject).therapies;
                this.preclinicalSubject.therapies = ts;
            } else if (this.preclinicalSubject.animalSubject.id) {
                return this.subjectTherapyService.getSubjectTherapies(this.preclinicalSubject).then(st => {
                    this.preclinicalSubject.therapies = st;
                    return st;
                });
            } else {
                this.preclinicalSubject.therapies = [];
            }
            return Promise.resolve(this.preclinicalSubject.therapies);
        }
        return Promise.resolve(subjectTherapies);
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            { headerName: "Therapy", field: "therapy.name" },
            {
                headerName: "Type", field: "therapy.therapyType", cellRenderer: function(params: any) {
                    return TherapyType[params.data.therapy.therapyType];
                }
            },
            { headerName: "Dose", field: "dose", type: "number" },
            { headerName: "Molecule", field: "molecule", type: "string" },
            { headerName: "Dose Unit", field: "dose_unit.value" },
            {
                headerName: "Type", field: "frequency", cellRenderer: function (params: any) {
                    return Frequency[params.data.frequency];
                }
            },
            { headerName: "Start Date", field: "startDate", type: "date" },
            { headerName: "End Date", field: "endDate", type: "date" }
        ];
        setTimeout(() => {
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                colDef.push({ headerName: "", type: "button", awesome: "fa-regular fa-edit", action: item => this.editSubjectEntity(item) });
            }
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                colDef.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeSubjectEntity(item) });
            }
        }, 100)
        return colDef;
    }
}
