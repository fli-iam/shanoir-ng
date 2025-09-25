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
import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ConsoleService } from 'src/app/shared/console/console.service';
import { ServiceLocator } from 'src/app/utils/locator.service';

import * as AppUtils from '../../utils/app.utils';
import { AccountRequestInfo } from '../account-request-info/account-request-info.model';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';

@Component({
    selector: 'accountRequest',
    templateUrl: 'account-request.component.html',
    styleUrls: ['account-request.component.css'],
    standalone: false
})

export class AccountRequestComponent implements OnInit {

    public user: User;
    public form: UntypedFormGroup;

    public requestSent: boolean = false;
    public errorOnRequest: boolean = false;
    infoValid: boolean = false;
    protected router: Router;
    studyName: string; // optional : study display name
    invitationIssuer: string; // optional : issuer of the invitation (from the study details)
    function: string; // optional : operator/researcher
    language: 'english' | 'french' = 'english';
    loading: boolean = false;

    constructor(
            private fb: UntypedFormBuilder,
            public userService: UserService,
            private location: Location,
            private route: ActivatedRoute,
            private consoleService: ConsoleService) {
                this.router = ServiceLocator.injector.get(Router)
                this.studyName = this.route.snapshot.queryParams['study'];
                this.invitationIssuer = this.route.snapshot.queryParams['from'];
                this.function = this.route.snapshot.queryParams['function'];
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

    private validateARInfo = (): ValidationErrors | null => {
        if (!this.infoValid) {
            return { invalid: true}
        }
        return null;
    }

    accountRequest(): void {
        if (this.studyName) this.user.accountRequestInfo.studyName = this.studyName;
        if (this.invitationIssuer) this.user.accountRequestInfo.contact = this.invitationIssuer;
        if (this.function) this.user.accountRequestInfo.function = this.function;
        this.loading = true;
        this.userService.requestAccount(this.user)
            .then(() => {
                 this.requestSent = true;
            }, (err) => {
                if (err?.error?.details?.fieldErrors?.email != null) {
                    this.consoleService.log("error", "An account already exists for this email address. Please connect with your credentials or pass by the reset password process (link named 'Forgot password?').")
                } else {
                    throw err;
                }
            }).finally(() => {
                this.loading = false;
            });
    }

    getOut(): void {
        // Return to welcome page
        window.location.href = AppUtils.LOGOUT_REDIRECT_URL;
    }

    cancelAccountRequest(): void {
        // Return to welcome page
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
        const formError = this.formErrors(fieldName);
        if (formError) {
            for(const errorName of errors) {
                if(formError[errorName]) return true;
            }
        }
        return false;
    }


}
