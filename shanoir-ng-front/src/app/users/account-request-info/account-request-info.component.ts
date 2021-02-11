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
import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, FormBuilder, FormControl, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { StudyService } from '../../studies/shared/study.service';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

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
export class AccountRequestInfoComponent implements ControlValueAccessor {

    @Input() editMode: boolean = false;
    public isChallenge: boolean;
    info: AccountRequestInfo = new AccountRequestInfo;
    form: FormGroup;
    private onChange = (_: any) => {};
    private onTouch = () => {};
    public challengeOptions:  Option<number>[];

    constructor(private formBuilder: FormBuilder,
                private route: ActivatedRoute,
                private studyService: StudyService,
                private msgService: MsgBoxService) {
        this.isChallenge = this.route.snapshot.data['isChallenge'];
    }

    writeValue(obj: any): void {
        this.info = obj;
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouch = fn;
    }

    ngOnInit() {
        if (this.isChallenge) {
            this.studyService.getChallenges()
                .then(result => this.challengeOptions = result.map(
                    element => new Option(element.id, element.name)
                )).catch(e => {
                  this.form.disable;
                  this.msgService.log('warn', 'No challenges available for the moment. Please retry later.');
                });
        }
        this.form = this.formBuilder.group({
            'institution': [this.info.institution, [Validators.required, Validators.maxLength(200)]],
            'service': [this.info.service, [Validators.required, Validators.maxLength(200)]],
            'function': [this.info.function, this.isChallenge ? [] :[Validators.required, Validators.maxLength(200)]],
            'study': [this.info.study, this.isChallenge ? [] : [Validators.required, Validators.maxLength(200)]],
            'contact': [this.info.contact, this.isChallenge ? [] : [Validators.required, Validators.maxLength(200)]],
            'work': [this.info.work, this.isChallenge ? [] : [Validators.required, Validators.maxLength(200)]],
            'challenge': [this.info.challenge, !this.isChallenge ? [] : [Validators.required]]
        });
        this.form.valueChanges.subscribe(() => {
            if (this.form.valid) {
                this.onChange(this.info);
            } else {
                this.onChange(null);
            }
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
