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
import { Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Option } from '../../shared/select/select.component';
import { SuperPromise } from '../../utils/super-promise';
import { QualityCardRule } from '../shared/quality-card.model';
import { StudyCardCondition } from '../shared/study-card.model';

import { ShanoirMetadataField } from './action/action.component';


@Component({
    selector: 'quality-card-rule',
    templateUrl: 'quality-card-rule.component.html',
    styleUrls: ['study-card-rule.component.css'],
    standalone: false
})
export class QualityCardRuleComponent implements OnChanges {

    @Input() mode: Mode;
    @Input() rule: QualityCardRule;
    private rulePromise: SuperPromise<QualityCardRule> = new SuperPromise(); 
    @Input() conditionFields: ShanoirMetadataField[];
    @Output() change: EventEmitter<QualityCardRule> = new EventEmitter();
    @Output() moveUp: EventEmitter<void> = new EventEmitter();
    @Output() moveDown: EventEmitter<void> = new EventEmitter();
    @Output() copy: EventEmitter<void> = new EventEmitter();
    @Output() delete: EventEmitter<void> = new EventEmitter();
    @Input() showErrors: boolean = false;
    touched: boolean = false;
    tagOptions = [new Option('VALID', 'Valid', undefined, 'green', 'fa-solid fa-circle-check'), 
            new Option('WARNING', 'Warning', undefined, 'chocolate', 'fa-solid fa-triangle-exclamation'), 
            new Option('ERROR', 'Error', undefined, 'red', 'fa-solid fa-times-circle')];
    conditionFieldOptions: Option<string>[];
    @Input() addSubForm: (subForm: FormGroup) => FormGroup;

    constructor(public elementRef: ElementRef) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.rule && this.rule) {
            this.rulePromise.resolve(this.rule);
        }
        if (changes.conditionFields) {
            this.rulePromise.then(() => {
                if (this.conditionFields) {
                    this.conditionFieldOptions = this.conditionFields
                        .map(field => new Option<string>(field.field, field.label, field.scope));
                } else {
                    this.conditionFieldOptions = [];
                }
            });
        }
    }

    addNewCondition() {
        let cond = new StudyCardCondition('StudyCardDICOMConditionOnDatasets');
        cond.values = [null];
        this.rule.conditions.push(cond);
        this.change.emit(this.rule);
    }

    deleteCondition(index: number) {
        this.rule.conditions.splice(index, 1);
        this.change.emit(this.rule);
    }

    @HostListener('document:click', ['$event.target'])
    public onClick(targetElement) {
        const clickedInside = this.elementRef.nativeElement.contains(targetElement);
        if (!clickedInside) {
            this.touched = true;
        }
    }

}