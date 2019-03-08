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

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { Study } from '../../../studies/shared/study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from '../../../subjects/shared/subject.service';
import { Dataset } from '../../shared/dataset.model';
import { DatepickerComponent } from '../../../shared/date/date.component';


@Component({
    selector: 'common-dataset-details',
    templateUrl: 'dataset.common.component.html'
})

export class CommonDatasetComponent implements OnChanges {

    @Input() private mode: Mode;
    @Input() private dataset: Dataset;
    @Input() private parentFormGroup: FormGroup;
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    

    constructor(
            private studyService: StudyService,
            private subjectService: SubjectService,
            private formBuilder: FormBuilder) {}

    completeForm() {
        this.parentFormGroup.addControl('subject', new FormControl(this.dataset.subjectId, [Validators.required]));
        this.parentFormGroup.addControl('study', new FormControl(this.dataset.studyId, [Validators.required]));
        this.parentFormGroup.addControl('creationDate', new FormControl(this.dataset.creationDate, [DatepickerComponent.validator]));
        
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['parentFormGroup']) {
            this.completeForm();
        }
        if (changes['mode']) {
            if (this.mode != 'view')  {
                this.fetchAllSubjects();
                this.fetchAllStudies();
            } else if (this.dataset) {
                this.fetchOneSubject();
                this.fetchOneStudy();
            }
        }
        if (changes['dataset'] && this.mode == 'view') {
            if (changes['dataset'].firstChange || changes['dataset'].previousValue.subjectId != changes['dataset'].currentValue.subjectId) {
                this.fetchOneSubject();
            }
            if (changes['dataset'].firstChange || changes['dataset'].previousValue.studyId != changes['dataset'].currentValue.studyId) {
                this.fetchOneStudy();
            }

        }
    }

    private fetchOneSubject() {
        if (!this.dataset.subjectId) return;
        this.subjectService.get(this.dataset.subjectId).then(subject => {
            this.subjects = [subject];
        });
    }

    private fetchOneStudy() {
        if (!this.dataset.studyId) return;
        this.studyService.get(this.dataset.studyId).then(study => {
            this.studies = [study];
        });
    }

    private fetchAllSubjects() {
        this.subjectService.getAll().then(subjects => {
            this.subjects = subjects;
        });
    }

    private fetchAllStudies() {
        this.studyService.getAll().then(studies => {
            this.studies = studies;
        });
    }

    private getSubjectName(id: number): string {
        if (!this.subjects || this.subjects.length == 0 || !id) return null;
        for (let subject of this.subjects) {
            if (subject.id == id) return subject.name;
        }
        throw new Error('Cannot find subject for id = ' + id);
    }

    private getStudyName(id: number): string {
        if (!this.studies || this.studies.length == 0 || !id) return null;
        for (let study of this.studies) {
            if (study.id == id) return study.name;
        }
        throw new Error('Cannot find study for id = ' + id);
    }

}