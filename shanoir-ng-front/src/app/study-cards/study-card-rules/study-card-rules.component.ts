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
    IterableDiffer,
    OnChanges,
    Output,
    QueryList,
    SimpleChanges,
    ViewChildren,
} from '@angular/core';
import { AbstractControl, ControlValueAccessor, NG_VALUE_ACCESSOR, ValidationErrors } from '@angular/forms';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

import { Coil } from '../../coils/shared/coil.model';
import { CoilService } from '../../coils/shared/coil.service';
import { AcquisitionContrast } from '../../enum/acquisition-contrast.enum';
import { ContrastAgent } from '../../enum/contrast-agent.enum';
import { ExploredEntity } from '../../enum/explored-entity.enum';
import { MrSequenceApplication } from '../../enum/mr-sequence-application.enum';
import { MrSequencePhysics } from '../../enum/mr-sequence-physics.enum';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Option } from '../../shared/select/select.component';
import { StudyCardRule } from '../shared/study-card.model';
import { AssignmentField } from './action/action.component';
import { StudyCardRuleComponent } from './study-card-rule.component';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { MrDatasetNature } from '../../datasets/dataset/mr/dataset.mr.model';


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
      ]
})
export class StudyCardRulesComponent implements OnChanges, ControlValueAccessor {
    
    @Input() mode: Mode | 'select';
    rules: StudyCardRule[];
    @ViewChildren(StudyCardRuleComponent) ruleElements: QueryList<StudyCardRuleComponent>;
    private onTouchedCallback = () => {};
    onChangeCallback = (_: any) => {};
    @Input() manufModelId: number;
    fields: AssignmentField[];
    private coilOptionsSubject: Subject<Option<Coil>[]> = new BehaviorSubject<Option<Coil>[]>(null);
    private coilOptions: Observable<Option<Coil>[]> = this.coilOptionsSubject.asObservable();
    private allCoilsPromise: Promise<Coil[]>;
    @Input() showErrors: boolean = false;
    @Output() importRules: EventEmitter<void> = new EventEmitter();
    @Output() selectedRulesChange: EventEmitter<StudyCardRule[]> = new EventEmitter();
    selectedRules: Map<number, StudyCardRule> = new Map();
    rulesToAnimate: Set<number> = new Set();

    
    constructor(
            private coilService: CoilService, 
            private element: ElementRef,
            private confirmDialogService: ConfirmDialogService,
            private breadcrumbService: BreadcrumbsService) {
     
        this.allCoilsPromise = this.coilService.getAll();

        this.fields = [
            new AssignmentField('Dataset modality type', 'MODALITY_TYPE', [
                new Option<string>('MR', 'Mr'), 
                new Option<string>('PET', 'Pet')
            ]),
            new AssignmentField('Protocol name', 'PROTOCOL_NAME'),
            new AssignmentField('Protocol comment', 'PROTOCOL_COMMENT'),
            new AssignmentField('Transmitting coil', 'TRANSMITTING_COIL', this.coilOptions),
            new AssignmentField('Receiving coil', 'RECEIVING_COIL', this.coilOptions),
            new AssignmentField('Explored entity', 'EXPLORED_ENTITY', ExploredEntity.options),
            new AssignmentField('Acquisition contrast', 'ACQUISITION_CONTRAST', AcquisitionContrast.options),
            new AssignmentField('MR sequence application', 'MR_SEQUENCE_APPLICATION', MrSequenceApplication.options),
            new AssignmentField('MR sequence physics', 'MR_SEQUENCE_PHYSICS', MrSequencePhysics.options),
            new AssignmentField('New name for the dataset', 'NAME'),
            new AssignmentField('Dataset comment', 'COMMENT'),
            new AssignmentField('MR sequence name', 'MR_SEQUENCE_NAME'),
            new AssignmentField('Contrast agent used', 'CONTRAST_AGENT_USED', ContrastAgent.options),
            new AssignmentField('Mr Dataset Nature', 'MR_DATASET_NATURE', MrDatasetNature.options)
        ];

        if (this.breadcrumbService.currentStep.data.rulesToAnimate) 
            this.rulesToAnimate = this.breadcrumbService.currentStep.data.rulesToAnimate;
        else
            this.breadcrumbService.currentStep.data.rulesToAnimate = this.rulesToAnimate;
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.manufModelId) {
            if (this.manufModelId) {
                this.allCoilsPromise.then(() => {
                    let optionArr: Option<Coil>[] = [];
                    this.allCoilsPromise.then(allCoils => {
                        allCoils
                            .filter(coil => coil.manufacturerModel.id == this.manufModelId)
                            .forEach(coil => optionArr.push(new Option<Coil>(coil, coil.name)));
                        this.coilOptionsSubject.next(optionArr);
                    });
                });
            } else if (this.coilOptionsSubject) {
                this.coilOptionsSubject.next([]);

            }
        }
    }

    addNewRule() {
        let rule: StudyCardRule = new StudyCardRule();
        rule.conditions = [];
        rule.assignments = []; 
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
        throw new Error("Method not implemented.");
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
        let copy = this.rules.slice(index, index + 1)[0];
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
        const rules: StudyCardRule[] = control.value; 
        let errors: any = {};
        if (rules) {
            rules.forEach(rule => {
                if ((rule.conditions && rule.conditions.find(cond => !cond.dicomTag || !cond.operation || !cond.dicomValue))
                        || (rule.assignments && rule.assignments.find(ass => !ass.field || !ass.value))) {
                    errors.missingField = true;
                }
                if (!rule.assignments || rule.assignments.length == 0) {
                    errors.noAssignment = true;
                }
            });
        }
        return errors;
    }

    clickRule(i: number) {
        if (this.mode == 'select') {
            if (this.selectedRules.has(i)) this.selectedRules.delete(i);
            else (this.selectedRules.set(i, this.rules[i]));
            let rulesArr: StudyCardRule[] = [];
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
