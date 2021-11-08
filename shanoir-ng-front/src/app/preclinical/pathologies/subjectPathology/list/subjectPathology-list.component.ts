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

import {Component, Input, ViewChild, forwardRef, EventEmitter} from '@angular/core'
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { SubjectPathology } from '../shared/subjectPathology.model';
import { SubjectPathologyService } from '../shared/subjectPathology.service';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { ShanoirError } from '../../../../shared/models/error.model';
import { ServiceLocator } from '../../../../utils/locator.service';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';
import { SubjectAbstractListInput } from '../../../shared/subjectEntity-list-input.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';


@Component({
    selector: 'subject-pathology-list',
    templateUrl: 'subjectPathology-list.component.html',
    providers: [
       { 
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => SubjectPathologiesListComponent),
          multi: true
        },
        SubjectPathologyService
    ]
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
            // Initialize from breadcrumbs cache if existing
            let ts: SubjectPathology[];
            if (this.breadcrumbsService.currentStep.entity != null && (this.breadcrumbsService.currentStep.entity as PreclinicalSubject).pathologies != null) {
                ts = (this.breadcrumbsService.currentStep.entity as PreclinicalSubject).pathologies;
                this.preclinicalSubject.pathologies = ts;
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

     getColumnDefs(): any[] {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        function checkNullValueReference(reference: any) {
            if (reference) {
                return reference.value;
            }
            return '';
        };

        let columnDefs: any[] = [
            { headerName: "Pathology", field: "pathology.name" },
            { headerName: "PathologyModel", field: "pathologyModel.name" },
            {
                headerName: "Location", field: "location.value", type: "reference", cellRenderer: function(params: any) {
                    return checkNullValueReference(params.data.location);
                }
            },
            {
                headerName: "Start Date", field: "startDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End Date", field: "endDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.endDate);
                }
            },
        ];
        setTimeout(() => {
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.editSubjectEntity(item) });
            }
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeSubjectEntity(item) });
            }
        }, 100)
        return columnDefs;
    }
}