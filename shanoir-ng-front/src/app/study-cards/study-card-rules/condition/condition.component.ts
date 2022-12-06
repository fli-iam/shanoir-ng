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
import { Component, Input, OnInit, Output, EventEmitter, OnDestroy, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';

import { StudyCardCondition, DicomTag, Operation } from '../../shared/study-card.model';
import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { DicomService } from '../../shared/dicom.service';
import { Option } from '../../../shared/select/select.component';
import { ShanoirMetadataField } from '../action/action.component';
import { Subscription } from 'rxjs';



@Component({
    selector: 'condition',
    templateUrl: 'condition.component.html',
    styleUrls: ['condition.component.css']
})
export class StudyCardConditionComponent implements OnInit, OnDestroy, OnChanges {
    
    @Input() condition: StudyCardCondition;
    @Output() conditionChange: EventEmitter<StudyCardCondition> = new EventEmitter();
    @Input() mode: Mode = 'view';
    @Input() fieldOptions: Option<string>[];
    @Input() fields: ShanoirMetadataField[];
    tagOptions: Option<DicomTag>[];
    shanoirFieldOptions: Option<any>[];
    operations: Operation[] = ['STARTS_WITH', 'EQUALS', 'ENDS_WITH', 'CONTAINS', 'SMALLER_THAN', 'BIGGER_THAN'];
    @Output() delete: EventEmitter<void> = new EventEmitter();

    @Input() showErrors: boolean;
    tagTouched: boolean = false;
    operationTouched: boolean = false;
    valueTouched: boolean = false;
    shanoirFieldTouched: boolean = false;

    private computeConditionOptionsSubscription: Subscription;
    private conditionChangeSubscription: Subscription;

    constructor(
            private dicomService: DicomService,
            private cdr: ChangeDetectorRef) {}
            
    ngOnInit(): void {
        if (this.mode != 'view') {
            this.dicomService.getDicomTags().then(tags => {
                this.tagOptions = [];
                for (let tag of tags) {
                    let hexStr: string = tag.code.toString(16).padStart(8, '0').toUpperCase();
                    let label: string = hexStr.substr(0, 4) + ',' + hexStr.substr(4, 4) + ' - ' + tag.label;
                    this.tagOptions.push(new Option<DicomTag>(tag, label));
                }
            });
        }
    }
            
    ngOnDestroy(): void {
        this.computeConditionOptionsSubscription?.unsubscribe();
        this.conditionChangeSubscription?.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.condition && this.condition && this.fields) {
            if (this.conditionChangeSubscription) {
                this.conditionChangeSubscription.unsubscribe();
                this.conditionChangeSubscription = null;
            }
            let conditionField: ShanoirMetadataField = this.fields.find(assF => assF.field == this.condition.shanoirField );
            if (this.mode == 'view') {
                //this.fieldLabel = conditionField.label;
                if (conditionField && conditionField.options) {
                    this.conditionChangeSubscription = conditionField.options.subscribe(opts => {
                        if (opts && opts.length > 0) {
                            this.condition.values?.forEach(value => {
                                let valueOption: Option<any> = opts.find(opt => {
                                    return opt.value == value 
                                        || (opt.value.id && value['id'] && opt.value.id == value['id'])
                                });
                                if (valueOption) {
                                    // this.valueIsString = false;
                                    // this.valueLabel = valueOption.label;
                                } 
                            });
                        }
                    });
                } else {
                    //this.valueIsString = true;
                } 
            } else {
                if (conditionField && conditionField.options) {
                    this.conditionChangeSubscription = conditionField.options.subscribe(opts => {
                        this.shanoirFieldOptions = opts.map(opt => opt.clone());
                        if (opts && opts.length > 0) {
                            this.condition.values?.forEach(value => {
                                let valueOption: Option<any> = opts.find(opt => {
                                    return opt.value == value
                                        || (opt.value.id && value['id'] && opt.value.id == value['id'])
                                });
                                if (valueOption) {
                                    value = valueOption.value;
                                }
                            });
                        }
                    });
                    
                } else {
                    this.shanoirFieldOptions = null;
                } 
            }
        }
    }

    onConditionChange() {
        this.conditionChange.emit(this.condition);
    }

    onConditionValueChange(value: any, index: number) {
        this.cdr.detectChanges();
    }

    onConditionOptionSelect(option: Option<any>) {
        if (option) option.disabled = true;
        this.onConditionChange();
    }

    onConditionOptionUnselect(option: Option<any>) {
        const index: number = this.condition.values?.findIndex(val => val == option.value);
        option.disabled = false;
        setTimeout(() => { // without setTimeout angular mix up everything
            if(index > -1) this.condition.values?.splice(index, 1);
            if (this.condition.values?.length == 0) this.condition.values = [null];
            this.onConditionChange();
        })
    }

    onTextValueRemove(index: number) {
        this.condition.values?.splice(index, 1);
        this.onConditionChange();
    }

    onFieldChange(field: string) {
        this.computeConditionOptions();
        if (this.shanoirFieldOptions?.length > 0) this.condition.operation = 'EQUALS';
        else this.condition.operation = null;
        this.condition.values = [null];
        this.valueTouched = false;
        this.conditionChange.emit(this.condition);
    }

    onConditionTypeChange(value: 'dicom' | 'shanoir') {
        this.condition.type = value;
        this.condition.shanoirField = null;
        this.condition.dicomTag = null;
        this.condition.operation = null;
        this.condition.values = [null];
    }

    private computeConditionOptions() {
        if (this.computeConditionOptionsSubscription) {
            this.computeConditionOptionsSubscription.unsubscribe();
            this.computeConditionOptionsSubscription = null;
        }
        if (this.condition.type == 'shanoir') {
            let conditionField: ShanoirMetadataField = this.fields.find(metadataField => metadataField.field == this.condition.shanoirField);
            if (conditionField && conditionField.options) {
                this.computeConditionOptionsSubscription = conditionField.options.subscribe(opts => {
                    this.shanoirFieldOptions = opts?.map(opt => opt.clone());
                });
            } else {
                this.shanoirFieldOptions = null;
            }    
        }
    }

    get tagError(): boolean {
        return !this.condition.dicomTag && (this.tagTouched || this.showErrors)
    }

    get operationError(): boolean {
        return !this.condition.operation && (this.operationTouched || this.showErrors)
    }

    get valueError(): boolean {
        return !(this.condition.values?.length > 0) && (this.valueTouched || this.showErrors)
    }

    get shanoirFieldError(): boolean {
        return !this.condition.shanoirField && (this.shanoirFieldTouched || this.showErrors)
    }

    trackByFn(index, item) {
        return index;  
    }
}