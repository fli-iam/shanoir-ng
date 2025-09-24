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
import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, ValidatorFn, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { Coil } from 'src/app/coils/shared/coil.model';

import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { Option } from '../../../shared/select/select.component';
import { DicomService } from '../../shared/dicom.service';
import { ConditionScope, DicomTag, Operation, StudyCardCondition, TagType, VM } from '../../shared/study-card.model';
import { ShanoirMetadataField } from '../action/action.component';



@Component({
    selector: 'condition',
    templateUrl: 'condition.component.html',
    styleUrls: ['condition.component.css'],
    standalone: false
})
export class StudyCardConditionComponent implements OnInit, OnDestroy, OnChanges {
    
    form: UntypedFormGroup;
    @Input() ruleScope: 'Dataset' | 'DatasetAcquisition' | 'Examination';
    @Input() condition: StudyCardCondition;
    @Output() conditionChange: EventEmitter<StudyCardCondition> = new EventEmitter();
    @Input() mode: Mode = 'view';
    @Input() fieldOptions: Option<string>[];
    @Input() fields: ShanoirMetadataField[];
    tagOptions: Option<DicomTag>[];
    shanoirFieldOptions: Option<any>[];
    operations: Option<Operation>[] = [
        new Option('EQUALS', ' ', null, null, 'fa-solid fa-equals'),
        new Option('NOT_EQUALS', ' ', null, null, 'fa-solid fa-not-equal'),
        new Option('SMALLER_THAN', ' ', null, null, 'fa-solid fa-less-than'),
        new Option('BIGGER_THAN', ' ', null, null, 'fa-solid fa-greater-than'),
        new Option('CONTAINS', 'contains', null, null),
        new Option('DOES_NOT_CONTAIN', '! contains'),
        new Option('STARTS_WITH', 'starts with'),
        new Option('DOES_NOT_START_WITH', '! starts with'),
        new Option('ENDS_WITH', 'ends with'),
        new Option('DOES_NOT_END_WITH', '! ends with'),
        new Option('PRESENT', 'present'),
        new Option('ABSENT', 'absent'),
    ];
    @Output() delete: EventEmitter<void> = new EventEmitter();
    init: boolean = false;
    conditionTypeOptions: Option<ConditionScope>[];
    fieldLabel: string;
    cardinalityType: 'NONE' | 'ALL' | 'AT_LEAST' = 'ALL';
    @Input() showErrors: boolean;
    tagTouched: boolean = false;
    operationTouched: boolean = false;
    valueTouched: boolean = false;
    shanoirFieldTouched: boolean = false;
    private computeConditionOptionsSubscription: Subscription;
    private conditionChangeSubscription: Subscription;
    @Input() addSubForm: (subForm: FormGroup) => FormGroup;
    private parentForm: FormGroup;

    constructor(
            private dicomService: DicomService,
            private cdr: ChangeDetectorRef,
            private formBuilder: UntypedFormBuilder) {}

    buildForm(): UntypedFormGroup {
        let form: UntypedFormGroup = this.formBuilder.group({
            'values': new FormArray(this.condition.values?.map(val => {
                return this.buildValueControl(val);
            })),
        });
        return form;
    }

    private buildValueControl(value: string | Coil) {
        let validators: ValidatorFn[] = [Validators.required, Validators.minLength(1)]
        let type: TagType = this.condition?.dicomTag?.type;
        let vm: VM = this.condition?.dicomTag?.vm;
        if (['Double', 'Float'].includes(type)) {
            validators.push(Validators.pattern('[+-]?([0-9]*[.])?[0-9]+')); // reals : only numbers, with dot as decimal separator
        } else if (['Integer', 'Long'].includes(type)) {
            validators.push(Validators.pattern('[+-]?[0-9]+')); // only numbers w/o decimals 
        } else if (type == 'String') {
            validators.push(Validators.pattern('^[^\"]*$')); // exclude "
        } else if (type == 'Date') {
            validators.push(Validators.pattern((/^\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/))); // yyyyMMdd
        } else if (type == 'FloatArray') {
            validators.push(Validators.pattern(this.buildArrayPattern(vm, 'float')));
        } else if ( type == 'IntArray') {
            validators.push(Validators.pattern(this.buildArrayPattern(vm, 'int'))); // comma separated integers
        }
        return new FormControl(value, validators);
    }

    private buildArrayPattern(vm: VM, type: 'float' | 'int') {
        let regexp: string;
        let charBlock: string;
        if (type == 'float') {
            charBlock = '[+-]?([0-9]*[.])?[0-9]+';
        } else if (type == 'int') {
            charBlock = '[+-]?[0-9]+';
        } else throw new Error('bad type');

        if (vm.max.multiplier) {
            if (vm.max.number > 1) {
                regexp = '((' + charBlock + ')(,' + charBlock + '){' 
                        + (vm.max.number - 1) + ',' + (vm.max.number - 1)
                        + '})(,(' + charBlock + ')(,' + charBlock + '){' 
                        + (vm.max.number - 1) + ',' + (vm.max.number - 1) + '})*';
            } else {
                regexp = '(' + charBlock + ')(,' + charBlock + '){' + (vm.min - 1) + ',}';
            }
        } else {
            regexp = '(' + charBlock + ')(,' + charBlock + '){' + (vm.min - 1) + ',' + (vm.max.number - 1) + '}';
        }
        return regexp;
    }
            
    ngOnInit(): void {
        if (this.mode != 'view') {
            this.dicomService.getDicomTags().then(tags => {
                this.tagOptions = [];
                for (let tag of tags) {
                    let hexStr: string = tag.code.toString(16).padStart(8, '0').toUpperCase();
                    let cardinality: string = this.buildCadinalityLabel(tag.vm);
                    let label: string = hexStr.substr(0, 4) + ',' + hexStr.substr(4, 4) + ' - ' + tag.label + ' <' + tag.type + cardinality +'>';
                    this.tagOptions.push(new Option<DicomTag>(tag, label));
                }
            });
        }
        this.parentForm = this.addSubForm(this.form);
        setTimeout(() => this.init = true);
    }

    private buildCadinalityLabel(vm: VM): string {
        if (vm.max.multiplier) {
            if (vm.min == vm.max.number) {
                if (vm.min == 1) return '[n]' ;
                else return '[' + vm.min + ',' + vm.max.number + 'n]';
            }
            else return '[' + vm.min + ',' + vm.max.number + 'n]';
        } else {
            if (vm.min == vm.max.number) {
                if (vm.min == 1) return '' ;
                else return '[' + vm.min + ']';
            }
            else return '[' + vm.min + ',' + vm.max.number + ']';
        }
    }
            
    ngOnDestroy(): void {
        this.computeConditionOptionsSubscription?.unsubscribe();
        this.conditionChangeSubscription?.unsubscribe();
        setTimeout(() => {
            (this.form?.get('values') as FormArray)?.clear();
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.ruleScope && this.ruleScope) {
            if (this.ruleScope == 'Dataset') {
                this.conditionTypeOptions = [
                    new Option('StudyCardDICOMConditionOnDatasets', 'the DICOM field'),
                    new Option('DatasetMetadataCondOnDataset', 'the dataset field'),
                ];
            } else if (this.ruleScope == 'DatasetAcquisition') {
                this.conditionTypeOptions = [
                    new Option('StudyCardDICOMConditionOnDatasets', 'the DICOM field'),
                    new Option('AcqMetadataCondOnAcq', 'the acquisition field'),
                    new Option('AcqMetadataCondOnDatasets', 'the dataset field'),
                ];
            } else if (this.ruleScope == 'Examination') {
                this.conditionTypeOptions = [
                    new Option('StudyCardDICOMConditionOnDatasets', 'the DICOM field'),
                    new Option('ExamMetadataCondOnAcq', 'the acquisition field'),
                    new Option('ExamMetadataCondOnDatasets', 'the dataset field'),
                ];
            }
        }
        if (changes.condition && this.condition && this.fields) {
            if (this.condition.cardinality == -1) this.cardinalityType = 'ALL';
            if (this.condition.cardinality == 0) this.cardinalityType = 'NONE';
            if (this.condition.cardinality > 0) this.cardinalityType = 'AT_LEAST';
            if (this.conditionChangeSubscription) {
                this.conditionChangeSubscription.unsubscribe();
                this.conditionChangeSubscription = null;
            }
            let conditionField: ShanoirMetadataField = this.fields.find(assF => assF.field == this.condition.shanoirField);
            if (this.mode == 'view') {
                this.fieldLabel = conditionField?.label;
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
                        this.shanoirFieldOptions = opts?.map(opt => opt.clone());
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
                this.form = this.buildForm();
                this.filterOperations();
                this.previousField = this.condition.dicomTag; 
            }
        }
    }

    onConditionChange() {
        this.conditionChange.emit(this.condition);
    }

    onConditionValueChange() {
        this.onConditionChange();
        this.cdr.detectChanges();
    }

    onConditionOptionSelect(option: Option<any>) {
        if (option) option.disabled = true;
    }

    onConditionOptionUnselect(option: Option<any>) {
        const index: number = this.condition.values?.findIndex(val => val == option.value);
        option.disabled = false;
        setTimeout(() => { // without setTimeout angular mix up everything
            if(index > -1) this.condition.values?.splice(index, 1);
            if (this.condition.values?.length == 0) this.resetValues();
            this.onConditionChange();
        })
    }

    onTextValueRemove(index: number) {
        if (this.condition.values?.splice(index, 1)?.length > 0) {
            (this.form.get('values') as FormArray).removeAt(index);
            this.form.get('values').markAsTouched();
            this.form.get('values').markAsDirty();
        }
        this.onConditionChange();
    }

    onTextValueAdd() {
        if (this.condition.values?.push(null)) {
            (this.form.get('values') as FormArray).push(this.buildValueControl(null));
            this.form.get('values').markAsTouched();
            this.form.get('values').markAsDirty();
        }
    }

    private resetValues() {
        this.clearValues()
        if (this.condition.operation != 'PRESENT' && this.condition.operation != 'ABSENT') {
            setTimeout(() => { // otherwise bugs
                this.onTextValueAdd();
            })
        }
    }

    private clearValues() {
        this.condition.values = [];
        (this.form.get('values') as FormArray).clear();
    }

    onFieldChange() {
        this.computeConditionOptions();
        this.filterOperations();
        this.resetValues();
        this.valueTouched = false;
        this.conditionChange.emit(this.condition);
    }

    onDicomFieldChange(field: DicomTag) {
        if (field?.code != this.previousField?.code) {
            this.filterOperations();
            //if (field?.type != this.previousField?.type && field?.vm != this.previousField?.vm) {
                this.resetValues();
                this.valueTouched = false;
            //}
            this.onConditionChange();
            this.previousField = field;
        }
    }

    /**
     * Filter the available operations
     */
    private filterOperations() {
        if (this.condition.scope == 'StudyCardDICOMConditionOnDatasets') { // DICOM fields
            if (this.condition?.dicomTag) {
                let type: TagType = this.condition.dicomTag.type;
                if (['Double', 'Float', 'Integer', 'Long', 'Date'].includes(type)) {
                    this.operations.forEach(op => {
                        if (['EQUALS', 'SMALLER_THAN', 'BIGGER_THAN', 'NOT_EQUALS','PRESENT', 'ABSENT'].includes(op.value)) {
                            op.disabled = false;
                        } else {
                            op.disabled = true;
                        }
                       ;
                    });
                } else if (type == 'String') {
                    this.operations.forEach(op => {
                        if (['STARTS_WITH', 'EQUALS', 'ENDS_WITH', 'CONTAINS', 'DOES_NOT_CONTAIN', 'DOES_NOT_START_WITH', 'NOT_EQUALS', 'DOES_NOT_END_WITH', 'PRESENT', 'ABSENT'].includes(op.value)) {
                            op.disabled = false;
                        } else {
                            op.disabled = true;
                        }
                       ;
                    });
                } else if (['FloatArray', 'IntArray'].includes(type)) {
                    this.operations.forEach(op => {
                        if (['EQUALS', 'NOT_EQUALS', 'PRESENT', 'ABSENT'].includes(op.value)) {
                            op.disabled = false;
                        } else {
                            op.disabled = true;
                        }
                       ;
                    });
                } else {
                    this.operations.forEach(op => {
                        if (['PRESENT', 'ABSENT'].includes(op.value)) {
                            op.disabled = false;
                        } else {
                            op.disabled = true;
                        }
                    });
                }
            } else {
                this.operations.forEach(op => op.disabled = false);
            }
        } else { // Shanoir fields
            if (this.shanoirFieldOptions?.length > 0) { // with option list such as coils
                this.operations.forEach(op => {
                    if (['EQUALS', 'NOT_EQUALS'].includes(op.value)) {
                        op.disabled = false;
                    } else {
                        op.disabled = true;
                    }
                   ;
                });
            } else { // string fields
                this.operations.forEach(op => {
                    if (['STARTS_WITH', 'EQUALS', 'ENDS_WITH', 'CONTAINS', 'DOES_NOT_CONTAIN', 'DOES_NOT_START_WITH', 'NOT_EQUALS', 'DOES_NOT_END_WITH'].includes(op.value)) {
                        op.disabled = false;
                    } else {
                        op.disabled = true;
                    }
                   ;
                });
            }
        }
        // unselect disabled option
        if (this.condition.operation) {
            let selectOperation: Option<Operation> = this.operations.find(op => op.value == this.condition.operation);
            if (selectOperation?.disabled) {
                this.condition.operation = null;
            } 
        }
        if (this.shanoirFieldOptions?.length > 0) this.condition.operation = 'EQUALS';
    }

    private previousField: DicomTag;

    onConditionTypeChange(value: ConditionScope) {
        if (value != this.condition.scope) {
            this.condition.scope = value;
            this.condition.shanoirField = null;
            this.condition.dicomTag = null;
            this.filterOperations();
            this.resetValues();
            this.form = this.buildForm();
            this.onConditionChange();
            if (value.endsWith('OnDataset') || value.endsWith('OnDatasets')) {
                this.fieldOptions.forEach(opt => opt.disabled = opt.section != 'Dataset');
            } else if (value.endsWith('OnAcq') || value.endsWith('OnAcq')) {
                this.fieldOptions.forEach(opt => opt.disabled = opt.section != 'DatasetAcquisition');
            } else {
                this.fieldOptions.forEach(opt => opt.disabled = false);
            }
        }
    }

    onOperationChange() {
        if (this.condition.operation == 'PRESENT' || this.condition.operation == 'ABSENT') {
            this.clearValues();
        } else if (this.condition.values.length == 0) {
            this.resetValues();
        }
        this.onConditionChange(); 
    }

    private computeConditionOptions() {
        if (this.computeConditionOptionsSubscription) {
            this.computeConditionOptionsSubscription.unsubscribe();
            this.computeConditionOptionsSubscription = null;
        }
        if (this.condition.scope != 'StudyCardDICOMConditionOnDatasets') {
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

    onCardinalityTypeChange() {
        if (this.cardinalityType == 'ALL') {
            this.condition.cardinality = -1;
        } else if (this.cardinalityType == 'NONE') {
            this.condition.cardinality = 0;
        } else if (this.cardinalityType == 'AT_LEAST' && this.condition.cardinality <= 1) {
            this.condition.cardinality = 1;
        }
        this.onConditionChange(); 
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

    get cardinalityError(): boolean {
        return !(this.condition.cardinality && this.condition.cardinality > -1) && this.showErrors;
    }

    trackByFn(index) {
        return index;  
    }
}