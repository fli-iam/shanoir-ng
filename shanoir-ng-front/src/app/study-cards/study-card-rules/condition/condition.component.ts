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
import { Component, Input, OnInit } from '@angular/core';

import { StudyCardCondition, DicomTag } from '../../shared/study-card.model';
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
    @Input() mode: Mode = 'view';
    private tagOptions: Option<DicomTag>[];

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
}