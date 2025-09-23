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
import {
    Component,
    ElementRef,
    EventEmitter,
    forwardRef,
    HostListener,
    Input,
    OnChanges,
    Output,
    QueryList,
    SimpleChanges,
    ViewChildren,
} from '@angular/core';
import { AbstractControl, ControlValueAccessor, FormGroup, NG_VALUE_ACCESSOR, ValidationErrors } from '@angular/forms';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

import { Coil } from '../../coils/shared/coil.model';
import { AcquisitionContrast } from '../../enum/acquisition-contrast.enum';
import { ContrastAgent } from '../../enum/contrast-agent.enum';
import { ExploredEntity } from '../../enum/explored-entity.enum';
import { MrSequenceApplication } from '../../enum/mr-sequence-application.enum';
import { MrSequencePhysics } from '../../enum/mr-sequence-physics.enum';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Option } from '../../shared/select/select.component';
import { MetadataFieldScope, StudyCardRule } from '../shared/study-card.model';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { MrDatasetNature } from '../../datasets/dataset/mr/dataset.mr.model';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { BidsDataType } from '../../enum/bids-data-type.enum';
import { SuperPromise } from '../../utils/super-promise';
import { QualityCardRule } from '../shared/quality-card.model';

import { StudyCardRuleComponent } from './study-card-rule.component';
import { ShanoirMetadataField } from './action/action.component';
import { QualityCardRuleComponent } from './quality-card-rule.component';

@Component({
    selector: 'study-card-rules',
    templateUrl: 'study-card-rules.component.html',
    styleUrls: ['study-card-rules.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            multi: true,
            useExisting: forwardRef(() => StudyCardRulesComponent),
        }
    ],
    standalone: false
})
export class StudyCardRulesComponent implements OnChanges, ControlValueAccessor {
    
    @Input() mode: Mode | 'select';
    @Input() cardType: 'studycard' | 'qualitycard';
    rules: (StudyCardRule | QualityCardRule)[];
    @ViewChildren('studyCardRule,qualityCardRule') ruleElements: QueryList<StudyCardRuleComponent | QualityCardRuleComponent>;
    private onTouchedCallback = () => {};
    onChangeCallback = (_: any) => {};
    @Input() manufModelId: number;
    @Input() allCoils: Coil[];
    @Input() studyId: number;
    assignmentFields: ShanoirMetadataField[];
    conditionFields: ShanoirMetadataField[];
    private coilOptionsSubject: Subject<Option<Coil>[]> = new BehaviorSubject<Option<Coil>[]>(null);
    private coilOptions: Observable<Option<Coil>[]> = this.coilOptionsSubject.asObservable();
    private allCoilsPromise: SuperPromise<Coil[]> = new SuperPromise();
    @Input() showErrors: boolean = false;
    @Output() importRules: EventEmitter<void> = new EventEmitter();
    @Output() selectedRulesChange: EventEmitter<(StudyCardRule | QualityCardRule)[]> = new EventEmitter();
    selectedRules: Map<number, StudyCardRule | QualityCardRule> = new Map();
    rulesToAnimate: Set<number> = new Set();
    @Input() addSubForm: (subForm: FormGroup) => FormGroup;

    
    constructor(
            private element: ElementRef,
            private confirmDialogService: ConfirmDialogService,
            private breadcrumbService: BreadcrumbsService) {

        if (this.breadcrumbService.currentStep.data.rulesToAnimate) 
            this.rulesToAnimate = this.breadcrumbService.currentStep.data.rulesToAnimate;
        else
            this.breadcrumbService.currentStep.data.rulesToAnimate = this.rulesToAnimate;
    }

    initFields() {
        this.assignmentFields = [
            new ShanoirMetadataField('Dataset modality type', 'MODALITY_TYPE', 'Dataset', DatasetModalityType.options),
            new ShanoirMetadataField('Protocol name', 'PROTOCOL_NAME', 'DatasetAcquisition'),
            new ShanoirMetadataField('Protocol comment', 'PROTOCOL_COMMENT', 'DatasetAcquisition'),
            new ShanoirMetadataField('Transmitting coil', 'TRANSMITTING_COIL', 'DatasetAcquisition', this.coilOptions),
            new ShanoirMetadataField('Receiving coil', 'RECEIVING_COIL', 'DatasetAcquisition', this.coilOptions),
            new ShanoirMetadataField('Explored entity', 'EXPLORED_ENTITY', 'Dataset', ExploredEntity.options),
            new ShanoirMetadataField('Acquisition contrast', 'ACQUISITION_CONTRAST', 'DatasetAcquisition', AcquisitionContrast.options),
            new ShanoirMetadataField('MR sequence application', 'MR_SEQUENCE_APPLICATION', 'DatasetAcquisition', MrSequenceApplication.options),
            new ShanoirMetadataField('MR sequence physics', 'MR_SEQUENCE_PHYSICS', 'DatasetAcquisition', MrSequencePhysics.options),
            new ShanoirMetadataField(this.cardType == 'studycard' ? 'New name for the dataset' : 'Dataset name' , 'NAME', 'Dataset'),
            new ShanoirMetadataField('Dataset comment', 'COMMENT', 'Dataset'),
            new ShanoirMetadataField('MR sequence name', 'MR_SEQUENCE_NAME', 'DatasetAcquisition'),
            new ShanoirMetadataField('Contrast agent used', 'CONTRAST_AGENT_USED', 'DatasetAcquisition', ContrastAgent.options),
            new ShanoirMetadataField('Mr Dataset Nature', 'MR_DATASET_NATURE', 'Dataset', MrDatasetNature.options),
			new ShanoirMetadataField('BIDS data type', 'BIDS_DATA_TYPE', 'DatasetAcquisition', BidsDataType.options)
        ];
        // here we reference assignment fields but conditions could be different
        this.conditionFields = this.assignmentFields;
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.manufModelId && this.manufModelId) {
            this.allCoilsPromise.then(allCoils => {
                let optionArr: Option<Coil>[] = [];
                allCoils
                    .filter(coil => coil.manufacturerModel.id == this.manufModelId)
                    .forEach(coil => optionArr.push(new Option<Coil>(coil, coil.name)));
                this.coilOptionsSubject.next(optionArr);
            });
        } 
        if(changes.studyId && this.studyId) {
            this.allCoilsPromise.then(allCoils => {
                let optionArr: Option<Coil>[] = [];
                allCoils
                    .filter(coil => coil.center?.studyCenterList?.find(sc => sc.study.id == this.studyId))
                    .forEach(coil => optionArr.push(new Option<Coil>(coil, coil.name)));
                this.coilOptionsSubject.next(optionArr);
            });

        }
        if (changes.allCoils && this.allCoils) {
            this.allCoilsPromise.resolve(this.allCoils);
        } 
        if (changes.cardType && this.cardType && (!this.assignmentFields || !this.conditionFields)) {
            this.initFields();
        }
    }

    addNewRule(scope: MetadataFieldScope) {
        let rule: StudyCardRule = new StudyCardRule(scope);
        rule.conditions = [];
        rule.assignments = []; 
        this.rules.push(rule);
        this.animateRule(this.rules.length - 1);
        this.onChangeCallback(this.rules);
    }

    addNewExamRule() {
        let rule: QualityCardRule = new QualityCardRule();
        rule.conditions = [];
        this.rules.push(rule);
        this.animateRule(this.rules.length - 1);
        this.onChangeCallback(this.rules);
    }

    writeValue(obj: any): void {
        this.rules = obj;
    }

    public animate

    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        
    }

    @HostListener('focusout', ['$event']) 
    private onFocusOut(event: FocusEvent) {
        if (!this.element.nativeElement.contains(event.relatedTarget)) {
            this.onTouchedCallback();
        } 
    }

    moveUp(index: number) {
        if (index <= 0) return; 
        this.switchWithFollowing(index - 1);
    }

    moveDown(index: number) {
        if (index >= this.rules.length - 1) return;
        this.switchWithFollowing(index);
    }

    switchWithFollowing(index: number) {
        this.rulesToAnimate.delete(index);
        this.rulesToAnimate.delete(index + 1);
        const delay: number = 0.2;
        let a = this.ruleElements.toArray()[index].elementRef.nativeElement;
        let b = this.ruleElements.toArray()[index + 1].elementRef.nativeElement;
        let aHeight = a.getBoundingClientRect().height;
        let bHeight = b.getBoundingClientRect().height;
        a.style.transition = 'all ' + delay + 's ease';
        b.style.transition = 'all ' + delay + 's ease';
        a.style.transform = 'translateY(' + bHeight + 'px)';
        b.style.transform = 'translateY(-' + aHeight + 'px)';
        setTimeout(() => {
            this.array_move(this.rules, index, index + 1);
            this.onChangeCallback(this.rules);
            a.style.transition = null;
            b.style.transition = null;
            a.style.transform = null;
            b.style.transform = null;
        }, delay*1000)
    }

    private array_move(arr, old_index, new_index) {
        if (new_index >= arr.length) {
            var k = new_index - arr.length + 1;
            while (k--) {
                arr.push(undefined);
            }
        }
        arr.splice(new_index, 0, arr.splice(old_index, 1)[0]);
        return arr; // for testing
    };

    copy(index: number) {
        let original: StudyCardRule | QualityCardRule = this.rules.slice(index, index + 1)[0];
        let copy;
        if (original instanceof StudyCardRule) {
            copy = StudyCardRule.copy(original);
        } else if (original instanceof QualityCardRule) {
            copy = QualityCardRule.copy(original);
        } 
        this.rules.push(copy);
        this.animateRule(this.rules.length - 1);
        this.onChangeCallback(this.rules);
    }

    delete(index: number) {
        this.confirmDialogService.confirm('Delete rule', 'Are you sure you want to delete this rule?').then(value => {
            if (value) this.rules.splice(index, 1);
            this.onChangeCallback(this.rules);
        })
    }

    public static validator = (control: AbstractControl): ValidationErrors | null => {
        const rules: (StudyCardRule | QualityCardRule)[] = control.value; 
        let errors: any = {};
        if (rules) {
            rules.forEach(rule => {
                if (rule.conditions?.find(cond => cond.scope == null)) {
                    errors.noType = true; 
                }
                if (rule.conditions?.find(cond => cond.scope == 'StudyCardDICOMConditionOnDatasets' && !cond.dicomTag)) {
                    errors.missingField = 'condition dicomTag';
                }
                if (rule.conditions?.find(cond => cond.scope != 'StudyCardDICOMConditionOnDatasets' && !cond.shanoirField)) {
                    errors.missingField = 'condition shanoirField';
                }
                if (rule.conditions?.find(cond => !cond.operation)) {
                    errors.missingField = 'condition operation';
                }      
                if (rule.conditions?.find(cond => cond.operation != 'PRESENT' && cond.operation != 'ABSENT' && cond.values?.length <= 0)) {
                    errors.missingField = 'condition values';
                }                     
                if (rule instanceof StudyCardRule) {
                    if (rule.assignments?.find(ass => !ass.field)) {
                        errors.missingField = 'assignment field';
                    }
                    if (rule.assignments?.find(ass => !ass.value)) {
                        errors.missingField = 'assignment value';
                    }
                    if (!rule.assignments || rule.assignments.length == 0) {
                        errors.noAssignment = true;
                    }
                } else if (rule instanceof QualityCardRule) {
                    if (!rule.tag) {
                        errors.missingField = 'quality tag';
                    }
                }
            });
        }

        return errors;
    }

    clickRule(i: number) {
        if (this.mode == 'select') {
            if (this.selectedRules.has(i)) this.selectedRules.delete(i);
            else (this.selectedRules.set(i, this.rules[i]));
            let rulesArr: (StudyCardRule | QualityCardRule)[] = [];
            this.selectedRules.forEach(rule => rulesArr.push(rule));
            this.selectedRulesChange.emit(rulesArr);
        }
    }

    animateRule(index: number) {
        this.rulesToAnimate.add(index);
    }

    canAnimateEnter(i: number): boolean {
        return this.rulesToAnimate.has(i);
    }
}
