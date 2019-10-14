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

import {Component, Input, ViewChild} from '@angular/core'

import { SubjectPathology } from '../shared/subjectPathology.model';
import { SubjectPathologyService } from '../shared/subjectPathology.service';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { ShanoirError } from '../../../../shared/models/error.model';
import { ServiceLocator } from '../../../../utils/locator.service';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';



@Component({
    selector: 'subject-pathology-list',
    templateUrl: 'subjectPathology-list.component.html',
    styleUrls: ['subjectPathology-list.component.css'],
    providers: [SubjectPathologyService]
})
@ModesAware
export class SubjectPathologiesListComponent extends BrowserPaginEntityListComponent<SubjectPathology>{
    @Input() canModify: Boolean = false;
    @Input() preclinicalSubject: PreclinicalSubject;
    public toggleFormSP: boolean = false;
    public createSPMode: boolean = false;
    public pathoSelected: SubjectPathology;
    
    @ViewChild('subjectPathologiesTable') table: TableComponent;
    

    constructor(
        private subjectPathologyService: SubjectPathologyService) {
            super('preclinical-subject-pathology');
    }


	getEntities(): Promise<SubjectPathology[]> {
        let subjectPathologies: SubjectPathology[] = [];
        if (this.preclinicalSubject && this.preclinicalSubject.animalSubject && this.preclinicalSubject.animalSubject.id) {
            this.subjectPathologyService.getSubjectPathologies(this.preclinicalSubject).then(st => {
                subjectPathologies = st;
            })
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
        let colDef: any[] = [
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
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
    
    

    refreshList(newSubPatho: SubjectPathology) {
        this.getEntities(),
        this.refreshDisplay(true);
    }


    refreshDisplay(cancel: boolean) {
        this.toggleFormSP = false;
        this.createSPMode = false;
    }

    protected openDeleteConfirmDialog = (entity: SubjectPathology) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-subject-pathology n° ' + entity.id + ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    this.subjectPathologyService.deleteSubjectPathology(this.preclinicalSubject, entity).then(() => {
                        this.onDelete.next(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The preclinical-subject-pathology sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next(new ShanoirError(reason));
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });                    
                }
            })
    }
    


    viewSubjectPathology = (therapy: SubjectPathology) => {
        this.toggleFormSP = true;
        this.createSPMode = false;
        this.pathoSelected = therapy;
    }

    toggleSubjectPathologyForm() {
        if (this.toggleFormSP == false) {
            this.toggleFormSP = true;
        } else if (this.toggleFormSP == true) {
            this.toggleFormSP = false;
        } else {
            this.toggleFormSP = true;
        }
        this.createSPMode = true;
        this.pathoSelected = new SubjectPathology();
    }

}