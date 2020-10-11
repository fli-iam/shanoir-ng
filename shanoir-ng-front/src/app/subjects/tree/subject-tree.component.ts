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
import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { IdName } from '../../shared/models/id-name.model';
import { Subject } from '../shared/subject.model';




@Component({
    selector: 'subject-tree',
    templateUrl: 'subject-tree.component.html',
    styleUrls: ['subject-tree.component.css'],
})

export class SubjectTreeComponent {

    constructor(private router: Router) {}
   
    @Input() subject: Subject;
    @Input() studies: IdName[];

    showStudyDetails(studyId: number) {
        this.router.navigate(['/study/details/' + studyId]);
    }
}