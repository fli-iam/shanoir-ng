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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

import { Mode } from '../../shared/components/entity/entity.component.abstract';
import { Option } from '../../shared/select/select.component';
import { StudyCardAssignment, StudyCardCondition, StudyCardRule } from '../shared/study-card.model';
import { AssignmentField } from './action/action.component';


@Component({
    selector: 'study-card-rule',
    templateUrl: 'study-card-rule.component.html',
    styleUrls: ['study-card-rule.component.css']
})
export class StudyCardRuleComponent implements OnChanges {

    @Input() mode: Mode;
    @Input() rule: StudyCardRule;
    @Input() fields: AssignmentField[];

    fieldOptions: Option<string>[];
    assigmentOptions: Option<any>[];

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
    }

    addNewAction() {
        this.rule.assignments.push(new StudyCardAssignment());
    }

    deleteCondition(index: number) {
        this.rule.conditions.splice(index, 1);
    }
    
    deleteAction(index: number) {
        let fieldOption: Option<string> = this.fieldOptions.find(opt => opt.value === this.rule.assignments[index].field);
        console.log(fieldOption)
        console.log(index, this.fieldOptions, this.rule.assignments[index])
        if (fieldOption) fieldOption.disabled = false;
        this.rule.assignments.splice(index, 1);
    }
}