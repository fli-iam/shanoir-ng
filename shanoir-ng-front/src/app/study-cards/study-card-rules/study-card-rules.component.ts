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
import { Component, ElementRef, forwardRef, HostListener, Input, OnChanges, SimpleChanges, QueryList, ViewChildren } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, AbstractControl, ValidationErrors } from '@angular/forms';

import { Coil } from '../../coils/shared/coil.model';
import { CoilService } from '../../coils/shared/coil.service';
import { AcquisitionContrast } from '../../enum/acquisition-contrast.enum';
import { ContrastAgent } from '../../enum/contrast-agent.enum';
import { ExploredEntity } from '../../enum/explored-entity.enum';
import { MrSequenceApplication } from '../../enum/mr-sequence-application.enum';
import { MrSequencePhysics } from '../../enum/mr-sequence-physics.enum';
import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Option } from '../../shared/select/select.component';
import { StudyCardRule } from '../shared/study-card.model';
import { AssignmentField } from './action/action.component';
import { StudyCardRuleComponent } from './study-card-rule.component';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { Observable, Subscriber, Subject, BehaviorSubject } from 'rxjs';


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
    
    @Input() mode: Mode;
    rules: StudyCardRule[];
    @ViewChildren(StudyCardRuleComponent) ruleElements: QueryList<StudyCardRuleComponent>;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() manufModelId: number;
    fields: AssignmentField[];
    private coilOptionsSubject: Subject<Option<Coil>[]> = new BehaviorSubject<Option<Coil>[]>(null);
    private coilOptions: Observable<Option<Coil>[]> = this.coilOptionsSubject.asObservable();
    private allCoilsPromise: Promise<Coil[]>;
    @Input() showErrors: boolean = false;
    
    
    constructor(
            private coilService: CoilService, 
            private element: ElementRef,
            private confirmDialogService: ConfirmDialogService) {
        
        this.allCoilsPromise = this.coilService.getAll();
        
        this.fields = [
            new AssignmentField('Dataset modality type', 'datasetMetadata.modalityType', [
                new Option<string>('Mr', 'Mr'), 
                new Option<string>('Pet', 'Pet')
            ]),
            new AssignmentField('Protocol name', 'mrProtocolMetadata.name'),
            new AssignmentField('Protocol comment', 'mrProtocolMetadata.comment'),
            new AssignmentField('Transmitting coil', 'mrProtocolMetadata.transmittingCoilId', this.coilOptions),
            new AssignmentField('Receiving coil', 'mrProtocolMetadata.receivingCoilId', this.coilOptions),
            new AssignmentField('Explored entity', 'datasetMetadata.exploredEntity', ExploredEntity.toOptions()),
            new AssignmentField('Acquisition contrast', 'mrProtocolMetadata.acquisitionContrast', AcquisitionContrast.toOptions()),
            new AssignmentField('MR sequence application', 'mrProtocolMetadata.mrSequenceApplication', MrSequenceApplication.toOptions()),
            new AssignmentField('MR sequence physics', 'mrProtocolMetadata.mrSequencePhysics', MrSequencePhysics.toOptions()),
            new AssignmentField('New name for the dataset', 'datasetMetadata.name'),
            new AssignmentField('Dataset comment', 'datasetMetadata.comment'),
            new AssignmentField('MR sequence name', 'mrProtocolMetadata.mrSequenceName'),
            new AssignmentField('Contrast agent used', 'mrProtocolMetadata.contrastAgentUsed', ContrastAgent.toOptions())
        ];
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
        this.onChangeCallback(this.rules);
    }

    writeValue(obj: any): void {
        this.rules = obj;
    }

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
}