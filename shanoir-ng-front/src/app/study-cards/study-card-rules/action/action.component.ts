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
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { Option } from '../../../shared/select/select.component';
import { StudyCardAssignment } from '../../shared/study-card.model';



@Component({
    selector: 'action',
    templateUrl: 'action.component.html',
    styleUrls: ['action.component.css']
})
export class StudyCardActionComponent {
    
    @Input() assignment: StudyCardAssignment;
    @Output() assignmentChange: EventEmitter<StudyCardAssignment> = new EventEmitter();
    @Input() mode: Mode = 'view';
    @Input() fieldOptions: Option<string>[];
    @Input() fields: AssignmentField[];
    assigmentOptions: Option<any>[];

    onChangeField(field: string) {
        let assignmentField: AssignmentField = this.fields.find(assF => assF.field == field);
        if (assignmentField && assignmentField.options) {
            this.assigmentOptions = assignmentField.options;
        } else {
            this.assigmentOptions = null;
        }    
        this.assignmentChange.emit(this.assignment);
    }

    onSelectFieldOption(option: Option<any>) {
        option.disabled = true;
    }

    onDeSelectFieldOption(option: Option<any>) {
        option.disabled = false;
    }

    onChangeValue() {
        this.assignmentChange.emit(this.assignment);
    }
}

export class AssignmentField {
    constructor(
        public label: string, 
        public field: string,
        public options?: any[]) {}
}


