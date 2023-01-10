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
import { ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { StudyService } from '../../studies/shared/study.service';
import { Option } from '../../shared/select/select.component';
import { Location } from '@angular/common';
import { AccountRequestInfo } from './account-request-info.model';
import { ConfirmDialogService } from 'src/app/shared/components/confirm-dialog/confirm-dialog.service';

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
    info: AccountRequestInfo = new AccountRequestInfo();
    form: FormGroup;
    onChange = (_: any) => {};
    onTouch = () => {};
    public studyOptions:  Option<number>[];
    studyName: string;
    presetStudyId: boolean

    constructor(private formBuilder: FormBuilder,
                private studyService: StudyService,
                private activatedRoute: ActivatedRoute,
                private location: Location,
                private confirmDialogService: ConfirmDialogService) {
    }

    setDisabledState?(isDisabled: boolean): void {
    }
    writeValue(obj: any): void {
        this.info = obj;
        if (this.activatedRoute.snapshot.params['id'] && this.activatedRoute.snapshot.params['id'] != 0) {
            this.presetStudyId = true;
            this.info.studyId = this.activatedRoute.snapshot.params['id'];
        }
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouch = fn;
    }

    ngOnInit() {
        // If study is preselected (from invitation), do not load available studies
        if (this.activatedRoute.snapshot.params['id'] && this.activatedRoute.snapshot.params['id'] != 0) {
            this.presetStudyId = true;
            this.info.studyId = this.activatedRoute.snapshot.params['id'];
        } else {
            this.studyService.getPublicStudies().then(result => {
                if (result && result.length > 0) {
                    this.studyOptions = result.map(element => new Option(element.id, element.name));
                } else {
                    this.studyOptions = [];
                    this.confirmDialogService.error("ERROR","No public studies available for the moment. Please ask a direct link to a study manager to create your account.")
                    .then(result => this.location.back());
                }
            });
        }
        this.form = this.formBuilder.group({
            'institution': [this.info.institution, [Validators.required, Validators.maxLength(200)]],
            'function': [this.info.function, [Validators.required, Validators.maxLength(200)]],
            'contact': [this.info.contact, [Validators.maxLength(200)]],
            'studyId': [this.info.studyId, [Validators.required]],
            'studyName': [this.info.studyName]
        });
        this.form.valueChanges.subscribe(() => {
            this.valid.emit(this.form.valid);
        });
    }

    onInfoChange() {
        this.onChange(this.info);
    }

    onStudyIdChange(event) {
        this.info.studyName = event.name;
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
