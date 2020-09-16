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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { Option } from '../../../shared/select/select.component';
import { StudyCardAssignment } from '../../shared/study-card.model';



@Component({
    selector: 'action',
    templateUrl: 'action.component.html',
    styleUrls: ['action.component.css']
})
export class StudyCardActionComponent implements OnChanges, OnDestroy {  
    @Input() assignment: StudyCardAssignment;
    @Output() actionChange: EventEmitter<StudyCardAssignment> = new EventEmitter();
    @Input() mode: Mode = 'view';
    @Input() fieldOptions: Option<string>[];
    @Input() fields: AssignmentField[];
    assigmentOptions: Option<any>[];
    @Output() delete: EventEmitter<void> = new EventEmitter();
    fieldLabel: string;
    valueLabel: string;
    valueIsString: boolean;
    badValueRef: boolean;

    @Input() showErrors: boolean;
    fieldTouched: boolean = false;
    valueTouched: boolean = false;

    private computeAssignmentOptionsSubscription: Subscription;
    private assignmentChangeSubscription: Subscription;

    onChangeField(field: string) {
        this.computeAssignmentOptions();
        this.assignment.value = null;
        this.valueTouched = false;
        this.actionChange.emit(this.assignment);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.fields && this.fields) {
            this.computeAssignmentOptions();
        }

        if (changes.assignment && this.assignment && this.fields) {
            if (this.assignmentChangeSubscription) {
                this.assignmentChangeSubscription.unsubscribe();
                this.assignmentChangeSubscription = null;
            }
            let assignmentField: AssignmentField = this.fields.find(assF => assF.field == this.assignment.field);
            if (this.mode == 'view') {
                this.fieldLabel = assignmentField.label;
                if (assignmentField && assignmentField.options) {
                    this.assignmentChangeSubscription = assignmentField.options.subscribe(opts => {
                        if (opts && opts.length > 0) {
                            let valueOption: Option<any> = opts.find(opt => {
                                return opt.value == this.assignment.value 
                                    || (opt.value.id && this.assignment.value['id'] && opt.value.id == this.assignment.value['id'])
                            });
                            if (valueOption) {
                                this.valueIsString = false;
                                this.valueLabel = valueOption.label;
                                this.badValueRef = false;
                            } else {
                                this.badValueRef = true;
                            }
                        }
                    });
                } else {
                    this.valueIsString = true;
                } 
            } else {
                if (assignmentField && assignmentField.options) {
                    this.assignmentChangeSubscription = assignmentField.options.subscribe(opts => {
                        this.assigmentOptions = opts;
                        if (opts && opts.length > 0) {
                            let valueOption: Option<any> = opts.find(opt => {
                                return opt.value == this.assignment.value 
                                    || (opt.value.id && this.assignment.value['id'] && opt.value.id == this.assignment.value['id'])
                            });
                            if (valueOption) {
                                this.assignment.value = valueOption.value;
                                this.badValueRef = false;
                            } else {
                                this.badValueRef = true;
                            }
                        }
                    });
                    
                } else {
                    this.assigmentOptions = null;
                } 
            }
        }
    }

    private computeAssignmentOptions() {
        if (this.computeAssignmentOptionsSubscription) {
            this.computeAssignmentOptionsSubscription.unsubscribe();
            this.computeAssignmentOptionsSubscription = null;
        }
        let assignmentField: AssignmentField = this.fields.find(assF => assF.field == this.assignment.field);
            if (assignmentField && assignmentField.options) {
                this.computeAssignmentOptionsSubscription = assignmentField.options.subscribe(opts => {
                    this.assigmentOptions = opts;
                });
            } else {
                this.assigmentOptions = null;
            }    
    }

    onSelectFieldOption(option: Option<any>) {
        if (option) option.disabled = true;
    }

    onDeSelectFieldOption(option: Option<any>) {
        if (option) option.disabled = false;
    }

    onChangeValue() {
        this.badValueRef = false;
        this.actionChange.emit(this.assignment);
    }

    get fieldError(): boolean {
        return !this.assignment.field && (this.fieldTouched || this.showErrors)
    }

    get valueError(): boolean {
        return !this.assignment.value && (this.valueTouched || this.showErrors)
    }

    ngOnDestroy(): void {
        if (this.assignmentChangeSubscription) this.assignmentChangeSubscription.unsubscribe();
        if (this.computeAssignmentOptionsSubscription) this.computeAssignmentOptionsSubscription.unsubscribe();
    }
}

export class AssignmentField {

    public options?: Observable<any[]>;

    constructor(
            public label: string, 
            public field: string,
            options?: Observable<any[]> | any[]) {

        if (options instanceof Observable) {
            this.options = options;
        } else {
            this.options = Observable.of(options);
        }
    }
}


