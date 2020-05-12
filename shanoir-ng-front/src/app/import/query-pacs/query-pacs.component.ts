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
import { Router } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { DicomQuery, ImportJob } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

export const atLeastOneNotBlank = (validator: ValidatorFn) => ( group: FormGroup ): ValidationErrors | null => {
    const hasAtLeastOneNotBlank = group && group.controls && Object.keys(group.controls)
      .some(key => !validator(group.controls[key]) && group.controls[key].value.trim().length != 0);
    return hasAtLeastOneNotBlank ? null : { atLeastOneNotBlank: true };
};

@Component({
    selector: 'query-pacs',
    templateUrl: 'query-pacs.component.html',
    styleUrls: ['../shared/import.step.css'],
    animations: [slideDown]
})

export class QueryPacsComponent{

    private dicomQuery: DicomQuery = new DicomQuery();
    private form: FormGroup;

    constructor(
            private breadcrumbsService: BreadcrumbsService, private router: Router,
            private importService: ImportService, private importDataService: ImportDataService,
            private formBuilder: FormBuilder, private msgBoxService: MsgBoxService) {

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Query';
        });
        breadcrumbsService.currentStep.importStart = true;
        this.buildForm();
    }

    queryPACS(): void {
        this.importService.queryPACS(this.dicomQuery).then((importJob: ImportJob) => {
            if (importJob && importJob.patients.length > 0) {
                this.importDataService.patientList = importJob;
                this.router.navigate(['imports/series']);
            } else {
                this.msgBoxService.log('warn', 'Nothing found. Please change your query parameters.'); 
            }
        })
    }

    buildForm(): void {
        const pacsDatePattern = /^\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/;
        // The wildcard search is not allowed for patientName and patientID
        const noWildcardPattern = /^((?!\*).)*$/;
        this.form = this.formBuilder.group({
            'patientName': [this.dicomQuery.patientName, [Validators.maxLength(64), Validators.pattern(noWildcardPattern)]],
            'patientID': [this.dicomQuery.patientID, [Validators.maxLength(64), Validators.pattern(noWildcardPattern)]],
            'patientBirthDate': [this.dicomQuery.patientBirthDate, Validators.pattern(pacsDatePattern)],
            'studyDescription': [this.dicomQuery.studyDescription, [Validators.maxLength(64), Validators.minLength(4)]],
            'studyDate': [this.dicomQuery.studyDate, Validators.pattern(pacsDatePattern)]
        }, { validator: atLeastOneNotBlank(Validators.required) });
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