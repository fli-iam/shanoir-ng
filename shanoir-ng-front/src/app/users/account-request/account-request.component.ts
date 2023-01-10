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

import { Location } from '@angular/common';
import { Component } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';
import { User } from '../shared/user.model';
import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { UserService } from '../shared/user.service'
import { FormGroup, Validators, FormBuilder, AbstractControl, ValidationErrors } from '@angular/forms';
import { ConsoleService } from 'src/app/shared/console/console.service';
import { ServiceLocator } from 'src/app/utils/locator.service';
import { Router } from '@angular/router';

@Component({
    selector: 'accountRequest',
    templateUrl: 'account-request.component.html',
    styleUrls: ['account-request.component.css']
})

export class AccountRequestComponent {
    
    public user: User;
    public form: FormGroup;

    public requestSent: boolean = false;
    public errorOnRequest: boolean = false;
    infoValid: boolean = false;
    protected router: Router;

    language: 'english' | 'french' = 'english';
    
    constructor(
            private fb: FormBuilder, 
            public userService: UserService,
            private location: Location,
            private consoleService: ConsoleService,) {
                this.router = ServiceLocator.injector.get(Router)
            }

    ngOnInit(): void {
        this.user = new User();
        this.user.accountRequestInfo = new AccountRequestInfo();
        this.buildForm();
    }

    buildForm(): void {
        const emailRegex = '^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$';
        this.form = this.fb.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex)]],
            'accountRequestInfo': [this.user.accountRequestInfo, [this.validateARInfo]]
        });
    }

    onInfoValidityUpdate(valid: boolean) {
        this.infoValid = valid;
        this.form.get('accountRequestInfo').updateValueAndValidity();
    }

    private validateARInfo = (control: AbstractControl): ValidationErrors | null => {
        if (!this.infoValid) {
            return { invalid: true}
        }
        return null;
    }

    accountRequest(): void {
        this.userService.requestAccount(this.user)
            .then((res) => {
                 this.requestSent = true;
            }, (err) => {
                if (err?.error?.details?.fieldErrors?.email != null) {
                    this.consoleService.log("error", "An user with this email already exists, please connect with it or use another one.")
                } else {
                    throw err;
                }
            });
    }

    getOut(): void {
        // Return to login
        window.location.href = AppUtils.LOGOUT_REDIRECT_URL;
    }

    cancelAccountRequest(): void {
        window.location.href = AppUtils.LOGOUT_REDIRECT_URL;
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
