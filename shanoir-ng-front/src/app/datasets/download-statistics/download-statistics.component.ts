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

import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { DatasetService } from '../shared/dataset.service';

@Component({
    selector: 'download-statistics',
    templateUrl: 'download-statistics.component.html'
})

export class DownloadStatisticsComponent{

    private form: FormGroup;

    constructor(private datasetService: DatasetService,
            private breadcrumbsService: BreadcrumbsService, 
            private formBuilder: FormBuilder) {

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Download statistics';
        });
        this.buildForm();
    }

    downloadStatistics(): void {
        const control = this.form.get('studyNameRegExp');
        this.datasetService.downloadStatistics(control.value);
    }

    buildForm(): void {
        this.form = this.formBuilder.group({
            'studyNameRegExp': ['', [Validators.maxLength(255)]]
        });
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[]) {
        let formError = this.formErrors(fieldName);
        if (formError) {
            for(let errorName of errors) {
                if(formError[errorName]) return true;
            }
        }
        return false;
    }
}