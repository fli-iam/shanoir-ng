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
import { Component, EventEmitter, forwardRef, Input, Output, OnInit } from '@angular/core';
import { ControlValueAccessor, FormBuilder, FormControl, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { StudyService } from '../../studies/shared/study.service';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { ConsoleService } from '../../shared/console/console.service';

import { AccountRequestInfo } from './account-request-info.model';

@Component ({
    selector: 'account-request-info',
    templateUrl: 'account-request-info.component.html',
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => AccountRequestInfoComponent),
          multi: true,
        }]  
})
export class AccountRequestInfoComponent implements ControlValueAccessor, OnInit {

    @Input() editMode: boolean = false;
    @Output() valid: EventEmitter<boolean> = new EventEmitter();
    info: AccountRequestInfo = new AccountRequestInfo;
    form: FormGroup;
    onChange = (_: any) => {};
    onTouch = () => {};
    public studyOptions:  Option<number>[];
    studyName: string;
    presetId: boolean

    constructor(private formBuilder: FormBuilder,
                private studyService: StudyService,
                private consoleService: ConsoleService,
                private activatedRoute: ActivatedRoute) {
    }

    writeValue(obj: any): void {
        this.studyName = null;
        if (obj.challenge && obj.challenge != this.info.studyId) {
            this.getStudyName(obj.challenge).then(name => this.studyName = name);
            this.info.studyName = this.studyName;
        }
        this.info = obj;
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouch = fn;
    }

    ngOnInit() {
        if (this.activatedRoute.snapshot.params['id'] && this.activatedRoute.snapshot.params['id'] != 0) {
            this.presetId = true;
            this.info.studyId = this.activatedRoute.snapshot.params['id'];
        }
        this.studyService.getPublicStudies().then(result => {
            if (result) {
                this.studyOptions = result.map(element => new Option(element.id, element.name));
            } else {
                this.studyOptions = [];
                this.consoleService.log('warn', 'No public studies available for the moment. Please ask a direct link to the study manager to create your account.');
            }
        });
        this.form = this.formBuilder.group({
            'institution': [this.info.institution, [Validators.required, Validators.maxLength(200)]],
            'service': [this.info.service, [Validators.required, Validators.maxLength(200)]],
            'function': [this.info.function, [Validators.required, Validators.maxLength(200)]],
            'contact': [this.info.contact, [Validators.required, Validators.maxLength(200)]],
            'work': [this.info.work, [Validators.required, Validators.maxLength(200)]],
            'studyId': [this.info.studyId, [Validators.required]],
            'studyName': [this.info.studyName, [Validators.required]]
        });
        this.form.valueChanges.subscribe(() => {
            this.valid.emit(this.form.valid);
        });
    }

    getStudyName(id: number): Promise<string> {
        return this.studyService.get(id).then(study => study ? study.name : null);
    }

    onInfoChange() {
        this.onChange(this.info);
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
