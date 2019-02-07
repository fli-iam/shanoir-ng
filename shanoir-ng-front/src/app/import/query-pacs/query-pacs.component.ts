import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ValidatorFn, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { DicomQuery, ImportJob } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';

export const atLeastOne = (validator: ValidatorFn) => ( group: FormGroup ): ValidationErrors | null => {
    const hasAtLeastOne = group && group.controls && Object.keys(group.controls)
      .some(k => !validator(group.controls[k]));
    return hasAtLeastOne ? null : { atLeastOne: true };
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
        private breadcrumbsService: BreadcrumbsService,
        private router: Router,
        private importService: ImportService,
        private importDataService: ImportDataService,
        private formBuilder: FormBuilder) {
            breadcrumbsService.nameStep('1. Query');
            breadcrumbsService.markMilestone();
            this.buildForm();
    }

    queryPACS(): void {
        this.importService.queryPACS(this.dicomQuery).then((importJob: ImportJob) => {
            if (importJob && importJob.patients.length > 0) {
                this.importDataService.patientList = importJob;
                this.router.navigate(['imports/series']);
            }
        })
    }

    buildForm(): void {
        const pacsDatePattern = /^\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/;
        this.form = this.formBuilder.group({
            'patientName': [this.dicomQuery.patientName, Validators.maxLength(64)],
            'patientID': [this.dicomQuery.patientID, Validators.maxLength(64)],
            'patientBirthDate': [this.dicomQuery.patientBirthDate, Validators.pattern(pacsDatePattern)],
            'studyDescription': [this.dicomQuery.studyDescription, Validators.maxLength(64)],
            'studyDate': [this.dicomQuery.studyDate, Validators.pattern(pacsDatePattern)]
        }, { validator: atLeastOne(Validators.required) });
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