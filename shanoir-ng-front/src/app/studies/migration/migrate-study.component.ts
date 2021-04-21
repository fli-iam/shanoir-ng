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

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { DatasetService } from '../../datasets/shared/dataset.service';
import { Option } from '../../shared/select/select.component';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { MigrationService } from './migration.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'download-statistics',
    templateUrl: 'migrate-study.component.html'
})
export class MigrateStudyComponent implements OnInit {

    public form: FormGroup;
    public studyOptions: Option<Study>[] = [];
    public error: string;

    constructor(private studyService: StudyService,
            private datasetService: DatasetService,
            private breadcrumbsService: BreadcrumbsService, 
            private formBuilder: FormBuilder,
            private migrationService: MigrationService) {
                setTimeout(() => {
                    this.breadcrumbsService.currentStepAsMilestone();
                    this.breadcrumbsService.currentStep.label = 'Migrate study';
                });
                this.buildForm();
    }

    ngOnInit() {
        this.studyService.findStudiesByUserId().then(studies => {
            this.studyOptions = studies.map(study => new Option(study, study.name));
        });
    }

    connect(): void {
       this.migrationService.connect(this.form.get('url').value, this.form.get('username').value, this.form.get('password').value, this.form.get('study').value.id)
       .then(() => {
            this.error = "";
        }).catch(exception => {
        if (exception.error.message.indexOf('401') != -1) {
            this.error = "Authentication failed, please check username and password";
        } else {
            this.error = "An unexpected error occured, please check distant URL";
        }
       }
    )};

    buildForm(): void {
        this.form = this.formBuilder.group({
            'study': ['', [Validators.maxLength(255)]],
            'username': ['', [Validators.maxLength(255)]],
            'password': ['', [Validators.maxLength(255)]],
            'url': ['', [Validators.maxLength(255)]]
        });
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

}