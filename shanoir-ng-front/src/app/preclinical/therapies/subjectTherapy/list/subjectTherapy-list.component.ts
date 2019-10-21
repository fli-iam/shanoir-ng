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

import {Input, Output, ViewChild, Component, forwardRef, EventEmitter} from '@angular/core'

import { SubjectTherapy } from '../shared/subjectTherapy.model';
import { SubjectTherapyService } from '../shared/subjectTherapy.service';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { TherapyType } from "../../../shared/enum/therapyType";
import { Frequency } from "../../../shared/enum/frequency";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { TableComponent } from '../../../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../../../shared/components/entity/entity-list.browser.component.abstract';
import { ShanoirError } from '../../../../shared/models/error.model';
import { ServiceLocator } from '../../../../utils/locator.service';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';
import { ControlValueAccessor } from '@angular/forms';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { Mode } from '../../../../shared/components/entity/entity.component.abstract';

@Component({
    selector: 'subject-therapy-list',
    templateUrl: 'subjectTherapy-list.component.html',
    styleUrls: ['subjectTherapy-list.component.css'],
        providers: [
        { 
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => SubjectTherapiesListComponent),
          multi: true
        },
        SubjectTherapyService
    ]
})


@ModesAware
export class SubjectTherapiesListComponent  extends BrowserPaginEntityListComponent<SubjectTherapy> implements ControlValueAccessor {

    @Input() canModify: Boolean = false;
    @Input() preclinicalSubject: PreclinicalSubject;
    @Input() mode: Mode;
    @Output() onEvent = new EventEmitter();
    protected model: any;
    protected disabled: boolean = false;
    protected propagateChange = (_: any) => {};
    protected propagateTouched = () => {};
    public toggleFormST: boolean = false;
    public createSTMode: boolean = false;
    public therapySelected: SubjectTherapy;
    private therapyList: SubjectTherapy[] = [];

   @ViewChild('subjectTherapiesTable') table: TableComponent;

    constructor(
        private subjectTherapyService: SubjectTherapyService) {
            super('preclinical-subject-therapy');
    }

    private addToCache(key: string, toBeCached: any) {
        if (!this.breadcrumbsService.currentStep.isPrefilled(key))  {
            this.breadcrumbsService.currentStep.addPrefilled(key, []);
        }
        this.breadcrumbsService.currentStep.getPrefilledValue(key).push(toBeCached);
    }

    private getCache(key: string) {
        if (!this.breadcrumbsService.currentStep.isPrefilled(key))  {
           this.breadcrumbsService.currentStep.addPrefilled(key, []);
        }
        return this.breadcrumbsService.currentStep.getPrefilledValue(key);
    }

    getEntities(): Promise<SubjectTherapy[]> {
        let subjectTherapies: SubjectTherapy[] = [];
        if (this.preclinicalSubject && this.preclinicalSubject.animalSubject && this.preclinicalSubject.animalSubject.id) {
            // Initialize from breadcrumbs cache if existing
            let ts: SubjectTherapy[];
            if (this.breadcrumbsService.currentStep.entity != null && (this.breadcrumbsService.currentStep.entity as PreclinicalSubject).therapies != null) {
                ts = (this.breadcrumbsService.currentStep.entity as PreclinicalSubject).therapies;
                this.preclinicalSubject.therapies = ts;
            } else {
                return this.subjectTherapyService.getSubjectTherapies(this.preclinicalSubject).then(st => {
                    this.preclinicalSubject.therapies = st;
                    return st;
                });
            }
            return Promise.resolve(this.preclinicalSubject.therapies);
        }
        return Promise.resolve(subjectTherapies);
    }

    getColumnDefs(): any[] {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        function castToString(id: number) {
            return String(id);
        };
        function checkNullValue(value: any) {
            if (value) {
                return value;
            }
            return '';
        };
        function checkNullValueReference(reference: any) {
            if (reference) {
                return reference.value;
            }
            return '';
        };
        let colDef: any[] = [  
            { headerName: "Therapy", field: "therapy.name" },
            {
                headerName: "Type", field: "therapy.therapyType", type: "Enum", cellRenderer: function(params: any) {
                    return TherapyType[params.data.therapy.therapyType];
                }
            },
            {
                headerName: "Dose", field: "dose", type: "dose", cellRenderer: function(params: any) {
                    return checkNullValue(params.data.dose);
                }
            },
            {
                headerName: "Dose Unit", field: "dose_unit.value", type: "reference", cellRenderer: function(params: any) {
                    return checkNullValueReference(params.data.dose_unit);
                }
            },
            {
                headerName: "Type", field: "frequency", type: "Enum", cellRenderer: function (params: any) {
                    return Frequency[params.data.frequency];
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
            }     
        ];
        setTimeout(() => {
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                colDef.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.editSubjectTherapy(item) });
            }
            if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
                colDef.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeSubjectTherapy(item) });
            }
        }, 100)
        return colDef;       
    }

    private editSubjectTherapy = (item: SubjectTherapy) => {
        this.therapySelected = item;
        this.toggleFormST = true;
        this.createSTMode = false;
    }

    private removeSubjectTherapy = (item: SubjectTherapy) => {
        const index: number = this.preclinicalSubject.therapies.indexOf(item);
        if (index !== -1) {
            this.preclinicalSubject.therapies.splice(index, 1);
        }
        if (item.id != null) {
            this.addToCache("therapiesToDelete", item);
        } else {
            if (this.getCache("therapiesToCreate").indexOf(item) != -1) {
                this.getCache("therapiesToCreate").splice(this.getCache("therapiesToCreate").indexOf(item), 1);
            }
            if (this.getCache("therapiesToCreate").indexOf(item) != -1) {
                 this.getCache("therapiesToUpdate").splice(this.getCache("therapiesToUpdate").indexOf(item), 1);
            }
        }
        this.onEvent.emit("delete");
        this.onDelete.next(item);
        this.table.refresh();
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    viewSubjectTherapy = (therapy: SubjectTherapy) => {
        this.toggleFormST = true;
        this.createSTMode = false;
        this.therapySelected = therapy;
    }

    toggleSubjectTherapyForm() {
        if (this.toggleFormST == false) {
            this.toggleFormST = true;
        } else if (this.toggleFormST == true) {
            this.toggleFormST = false;
        } else {
            this.toggleFormST = true;
        }
        this.createSTMode = true;
        this.therapySelected = new SubjectTherapy();
    }

    refreshDisplayTherapy(subjectTherapy: SubjectTherapy, create: boolean){
        this.toggleFormST = false;
        this.createSTMode = false;
        if (subjectTherapy && subjectTherapy != null) {
            if (!subjectTherapy.id && create) {
                this.addToCache("therapiesToCreate", subjectTherapy);
                this.onAdd.next(subjectTherapy);
                this.writeValue(subjectTherapy);
            } else  if (subjectTherapy.id && !create) {
                this.addToCache("therapiesToUpdate", subjectTherapy);
            }
        }
        this.onEvent.emit("create");
        this.table.refresh();
    }

    goToAddTherapy(){
        this.therapySelected = new SubjectTherapy();
        this.createSTMode = true;
        if (this.toggleFormST==false) {
            this.toggleFormST = true;
        } else if (this.toggleFormST==true) {
            this.toggleFormST = false;
        } else {
            this.toggleFormST = true;
        }
    }
    
    protected onRowClick(entity: SubjectTherapy) {
        // do nothing to avoid wrong route
    }

    writeValue(value: any): void {
        this.therapyList.push(value);
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }
}