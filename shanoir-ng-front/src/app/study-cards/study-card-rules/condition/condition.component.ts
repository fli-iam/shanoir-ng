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
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';

import { StudyCardCondition, DicomTag, Operation } from '../../shared/study-card.model';
import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { DicomService } from '../../shared/dicom.service';
import { Option } from '../../../shared/select/select.component';



@Component({
    selector: 'condition',
    templateUrl: 'condition.component.html',
    styleUrls: ['condition.component.css']
})
export class StudyCardConditionComponent implements OnInit {
    
    @Input() condition: StudyCardCondition;
    @Output() conditionChange: EventEmitter<StudyCardCondition> = new EventEmitter();
    @Input() mode: Mode = 'view';
    public tagOptions: Option<DicomTag>[];
    operations: Operation[] = ['STARTS_WITH', 'EQUALS', 'ENDS_WITH', 'CONTAINS', 'SMALLER_THAN', 'BIGGER_THAN'];
    @Output() delete: EventEmitter<void> = new EventEmitter();

    @Input() showErrors: boolean;
    tagTouched: boolean = false;
    operationTouched: boolean = false;
    valueTouched: boolean = false;

    constructor(
            private dicomService: DicomService) {}
    
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

    onConditionChange() {
        this.conditionChange.emit(this.condition);
    }

    get tagError(): boolean {
        return !this.condition.dicomTag && (this.tagTouched || this.showErrors)
    }

    get operationError(): boolean {
        return !this.condition.operation && (this.operationTouched || this.showErrors)
    }

    get valueError(): boolean {
        return !this.condition.dicomValue && (this.valueTouched || this.showErrors)
    }
}