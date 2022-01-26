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
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../../shared/date-picker/date-picker.component';
import { Study } from '../../../studies/shared/study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from '../../../subjects/shared/subject.service';
import { Dataset } from '../../shared/dataset.model';
import { ExploredEntity } from '../../../enum/explored-entity.enum';
import { Option } from '../../../shared/select/select.component';
import { DatasetModalityType } from '../../../enum/dataset-modality-type.enum';
import { DatasetProcessingType } from '../../../enum/dataset-processing-type.enum';
import { ProcessedDatasetType } from '../../../enum/processed-dataset-type.enum';
import { DatasetType } from '../../shared/dataset-type.model';

@Component({
    selector: 'common-dataset-details',
    templateUrl: 'dataset.common.component.html'
})
export class CommonDatasetComponent implements OnChanges {

    @Input() mode: Mode;
    @Input() dataset: Dataset;
    @Input() parentFormGroup: FormGroup;
    subjects: Subject[] = [];
    studies: Study[] = [];
    
    exploredEntityOptions: Option<ExploredEntity>[];
    datasetTypes: Option<DatasetType>[];
    processedDatasetTypeOptions: Option<ProcessedDatasetType>[];

    constructor(
            private studyService: StudyService,
            private subjectService: SubjectService,
            private formBuilder: FormBuilder) {

        this.exploredEntityOptions = ExploredEntity.options;
        this.datasetTypes = DatasetType.options;
        this.processedDatasetTypeOptions = ProcessedDatasetType.options;
    }

    completeForm() {
        this.parentFormGroup.addControl('subject', new FormControl(this.dataset.subject, [Validators.required]));
        this.parentFormGroup.addControl('study', new FormControl(this.dataset.study, [Validators.required]));
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
        } else if(changes['dataset'] && this.mode == 'edit') {
            this.parentFormGroup.updateValueAndValidity();
        }
    }

    private fetchOneSubject() {
        this.subjects = this.dataset.subject ? [this.dataset.subject] : [];
    }

    private fetchOneStudy() {
        this.studies = this.dataset.study ? [this.dataset.study] : [];
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

}