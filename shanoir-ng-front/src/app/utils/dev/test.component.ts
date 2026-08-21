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

import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { DatasetAcquisitionService } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { ExaminationService } from 'src/app/examinations/shared/examination.service';
import { StudyService } from 'src/app/studies/shared/study.service';

@Component({
    selector: 'dev-test',
    templateUrl: 'test.component.html',
    standalone: true,
    imports: [FormsModule]
})

export class TestComponent {

    constructor(
        private studyService: StudyService,
        private examinationService: ExaminationService,
        private acqService: DatasetAcquisitionService,
        private datasetService: DatasetService,
        private http: HttpClient
    ) { }

    id: number;

    test1() {
        this.datasetService.get(this.id);
    }

    test2() {
        this.http.get<any>('/datasets/' + this.id)
    }
}