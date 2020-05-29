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

import {Input, Output, ViewChild, Component, forwardRef, EventEmitter} from '@angular/core';

import { PreclinicalSubject } from '../animalSubject/shared/preclinicalSubject.model';
import { Frequency } from "../shared/enum/frequency";
import { ModesAware } from "../shared/mode/mode.decorator";
import { TableComponent } from '../../shared/components/table/table.component';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { ShanoirError } from '../../shared/models/error.model';
import { ServiceLocator } from '../../utils/locator.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { ControlValueAccessor } from '@angular/forms';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Entity } from '../../shared/components/entity/entity.abstract';

// @Component({
//     providers:  [{
//       provide: NG_VALUE_ACCESSOR,
//       useExisting: forwardRef(() => SubjectAbstractListInput),
//       multi: true
//     }]
// })

@ModesAware
export abstract class SubjectAbstractListInput<T extends Entity>  extends BrowserPaginEntityListComponent<T> implements ControlValueAccessor {

    @Input() canModify: Boolean = false;
    @Input() preclinicalSubject: PreclinicalSubject;
    @Input() mode: Mode;
    @Output() onEvent = new EventEmitter();
    protected propagateChange = (_: any) => {};
    protected propagateTouched = () => {};
    public toggleForm: boolean = false;
    public createMode: boolean = false;
    public selectedEntity: T;
    private entityList: T[] = [];

   @ViewChild('subjectEntityTable') table: TableComponent;

    public abstract getEntityName();

    protected abstract getEntity();
 
    protected abstract getEntityList();

    protected addToCache(key: string, toBeCached: any) {
        if (!this.breadcrumbsService.currentStep.isPrefilled(key))  {
            this.breadcrumbsService.currentStep.addPrefilled(key, []);
        }
        this.breadcrumbsService.currentStep.getPrefilledValue(key).push(toBeCached);
    }

    protected getCache(key: string) {
        if (!this.breadcrumbsService.currentStep.isPrefilled(key))  {
           this.breadcrumbsService.currentStep.addPrefilled(key, []);
        }
        return this.breadcrumbsService.currentStep.getPrefilledValue(key);
    }

    protected editSubjectEntity = (item: T) => {
        this.selectedEntity = item;
        this.toggleForm = true;
        this.createMode = false;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    viewSubjectEntity = (item: T) => {
        this.toggleForm = true;
        this.createMode = false;
        this.selectedEntity = item;
    }

    toggleSubjectItemForm() {
        if (this.toggleForm == false) {
            this.toggleForm = true;
        } else if (this.toggleForm == true) {
            this.toggleForm = false;
        } else {
            this.toggleForm = true;
        }
        this.createMode = true;
        this.selectedEntity = this.getEntity();
    }

    refreshDisplayEntity(subjectEntity: T, create: boolean){
        this.toggleForm = false;
        this.createMode = false;
        if (subjectEntity && subjectEntity != null) {
            if (!subjectEntity.id && create) {
                this.addToCache(this.getEntityName() + "ToCreate", subjectEntity);
                this.onAdd.next(subjectEntity);
                this.writeValue(subjectEntity);
            } else  if (subjectEntity.id && !create) {
                this.addToCache(this.getEntityName() + "ToUpdate", subjectEntity);
            }
        }
        this.onEvent.emit("create");
        this.table.refresh();
    }
    
    protected removeSubjectEntity = (item: T) => {
        const index: number = this.getEntityList().indexOf(item);
        if (index !== -1) {
            this.getEntityList().splice(index, 1);
        }
        if (item.id != null) {
            this.addToCache(this.getEntityName() + "ToDelete", item);
        } else {
            if (this.getCache(this.getEntityName() + "ToCreate").indexOf(item) != -1) {
                this.getCache(this.getEntityName() + "ToCreate").splice(this.getCache(this.getEntityName() + "ToCreate").indexOf(item), 1);
            }
            if (this.getCache(this.getEntityName() + "ToCreate").indexOf(item) != -1) {
                 this.getCache(this.getEntityName() + "ToUpdate").splice(this.getCache(this.getEntityName() + "ToUpdate").indexOf(item), 1);
            }
        }
        this.onEvent.emit("delete");
        this.onDelete.next(item);
        this.table.refresh();
    }

    goToAddEntity(){
        this.selectedEntity = this.getEntity();
        this.createMode = true;
        if (this.toggleForm==false) {
            this.toggleForm = true;
        } else if (this.toggleForm==true) {
            this.toggleForm = false;
        } else {
            this.toggleForm = true;
        }
    }

    public onRowClick(entity: T) {
        // do nothing to avoid wrong route
    }

    writeValue(value: any): void {
        this.entityList.push(value);
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        //Nothing to do
    }
}