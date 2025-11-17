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
import { Component, EventEmitter, forwardRef, Input, Output, OnInit, DestroyRef } from '@angular/core';
import { ControlValueAccessor, UntypedFormBuilder, UntypedFormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ConfirmDialogService } from 'src/app/shared/components/confirm-dialog/confirm-dialog.service';

import { StudyService } from '../../studies/shared/study.service';
import { Option } from '../../shared/select/select.component';

import { AccountRequestInfo } from './account-request-info.model';

@Component ({
    selector: 'account-request-info',
    templateUrl: 'account-request-info.component.html',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => AccountRequestInfoComponent),
            multi: true,
        }
    ],
    standalone: false
})
export class AccountRequestInfoComponent implements ControlValueAccessor, OnInit {

    @Input() editMode: boolean = false;
    @Output() valid: EventEmitter<boolean> = new EventEmitter();
    info: AccountRequestInfo = new AccountRequestInfo();
    form: UntypedFormGroup;
    onChange: (any) => void = () => { return; };
    onTouch: () => void = () => { return; };
    public studyOptions:  Option<number>[];
    studyName: string;
    presetStudyId: boolean;

    constructor(private formBuilder: UntypedFormBuilder,
                private studyService: StudyService,
                private activatedRoute: ActivatedRoute,
                private location: Location,
                private confirmDialogService: ConfirmDialogService,
                private destroyRef: DestroyRef
            ) { }

    setDisabledState?(_isDisabled: boolean): void { 
        return; 
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
            this.studyService.getPublicStudiesData().then(result => {
                if (result && result.length > 0) {
                    this.studyOptions = result.map(element => new Option(element.id, element.name));
                } else {
                    this.studyOptions = [];
                    this.confirmDialogService.error("ERROR","No public studies available for the moment. Please ask a direct link to a study manager to create your account.")
                    .then(() => this.location.back());
                }
            });
        }
        this.form = this.formBuilder.group({
            'institution': [this.info.institution, [Validators.required, Validators.maxLength(200)]],
            // 'function': [this.info.function, [Validators.required, Validators.maxLength(200)]],
            // 'contact': [this.info.contact, [Validators.maxLength(200)]],
            'studyId': [this.info.studyId, [Validators.required]],
            'studyName': [this.info.studyName]
        });
        this.form.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
            this.valid.emit(this.form.valid);
        });
    }

    onInfoChange() {
        const info: AccountRequestInfo = new AccountRequestInfo();
        info.contact = this.form.value.contact;
        info.function = this.form.value.function;
        info.institution = this.form.value.institution;
        info.studyId = this.form.value.studyId;
        info.studyName = this.form.value.studyName;
        this.info = info;
        this.onChange(this.info);
    }

    onInstitutionChange(institution: string) {
        this.info.institution = institution;
        this.onInfoChange();
    }

    onStudyChange(option: Option<number>) {
        this.info.studyName = option.label;
        this.onInfoChange();
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[]) {
        const formError = this.formErrors(fieldName);
        if (formError) {
            for(const errorName of errors) {
                if(formError[errorName]) return true;
            }
        }
        return false;
    }
}
