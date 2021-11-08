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
    HostListener,
    Input,
    OnChanges,
    Output,
    QueryList,
    SimpleChanges,
    ViewChildren,
} from '@angular/core';

import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Option } from '../../shared/select/select.component';
import { StudyCardAssignment, StudyCardCondition, StudyCardRule } from '../shared/study-card.model';
import { AssignmentField, StudyCardActionComponent } from './action/action.component';


@Component({
    selector: 'study-card-rule',
    templateUrl: 'study-card-rule.component.html',
    styleUrls: ['study-card-rule.component.css']
})
export class StudyCardRuleComponent implements OnChanges {

    @Input() mode: Mode;
    @Input() rule: StudyCardRule;
    @Input() fields: AssignmentField[];
    @Output() change: EventEmitter<StudyCardRule> = new EventEmitter();
    @Output() moveUp: EventEmitter<void> = new EventEmitter();
    @Output() moveDown: EventEmitter<void> = new EventEmitter();
    @Output() copy: EventEmitter<void> = new EventEmitter();
    @Output() delete: EventEmitter<void> = new EventEmitter();
    @Input() showErrors: boolean = false;
    @ViewChildren(StudyCardActionComponent) assignmentChildren: QueryList<StudyCardActionComponent>;
    touched: boolean = false;

    fieldOptions: Option<string>[];
    assigmentOptions: Option<any>[];

    constructor(public elementRef: ElementRef) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.fields) {
            if (this.fields) {
                this.fieldOptions = this.fields.map(field => new Option<string>(field.field, field.label));
            } else {
                this.fieldOptions = [];
            }
        }
    }

    addNewCondition() {
        this.rule.conditions.push(new StudyCardCondition());
        this.change.emit(this.rule);
    }

    addNewAction() {
        this.rule.assignments.push(new StudyCardAssignment());
        this.change.emit(this.rule);
    }

    deleteCondition(index: number) {
        this.rule.conditions.splice(index, 1);
        this.change.emit(this.rule);
    }
    
    deleteAction(index: number) {
        let fieldOption: Option<string> = this.fieldOptions.find(opt => opt.value === this.rule.assignments[index].field);
        if (fieldOption) fieldOption.disabled = false;
        this.rule.assignments.splice(index, 1);
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