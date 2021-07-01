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

import { Component, OnInit, Output, EventEmitter, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { ExtensionRequestInfo } from './extension-request-info.model';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { Subscription } from 'rxjs';
import * as AppUtils from '../../utils/app.utils';

@Component({
    selector: 'extensionRequest',
    templateUrl: 'extension-request.component.html',
    styleUrls: ['extension-request.component.css']
})

export class ExtensionRequestComponent implements OnInit, OnDestroy {
    @Output() closing = new EventEmitter();
    public extensionRequestInfo: ExtensionRequestInfo = new ExtensionRequestInfo();
    extensionRequestForm: FormGroup;
    isDateValid: boolean = true;
    userId: number;
    selectedDateNormal: string = '';
    private infoSubscription: Subscription;
    requestSent: boolean = false;
    errorMessage: string;

    constructor(private router: Router, private route: ActivatedRoute,
        private userService: UserService, private fb: FormBuilder) {
    }

    ngOnInit(): void {
        this.buildForm();
    }

    cancelExtensionRequest(): void {
        window.location.href = AppUtils.LOGOUT_REDIRECT_URL;
    }

    extensionRequest(): void {
        this.submit();
        this.userService.requestExtension(this.extensionRequestInfo)
            .then((res) => {
                this.requestSent = true;
                this.errorMessage = null;
            }).catch(exception => {
                if (exception.status == 406) {
                    this.requestSent = true;
                    this.errorMessage = "This account is not disabled or has already an extension request pending. Please contact an administrator for more information."
                } else if (exception.status == 400) {
                    this.errorMessage = "No account associated to this email, please enter a valid email address."
                } else {
                    throw exception;
                }
                
            });
    }

    submit(): void {
        this.extensionRequestInfo = this.extensionRequestForm.value;
    }

    isEditUserFormValid(): boolean {
        if (this.extensionRequestForm.valid && this.isDateValid) {
            return true;
        } else {
            return false;
        }
    }

    buildForm(): void {
        this.extensionRequestForm = this.fb.group({
            'email': [this.extensionRequestInfo.email, [Validators.required]],
            'extensionDate': [this.extensionRequestInfo.extensionDate, [Validators.required]],
            'extensionMotivation': [this.extensionRequestInfo.extensionMotivation, [Validators.required]]
            });

        this.infoSubscription = this.extensionRequestForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.extensionRequestForm) { return; }
        const form = this.extensionRequestForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'extensionDate': '',
        'extensionMotivation': ''
    };

    getDateToDatePicker(extensionRequestInfo: ExtensionRequestInfo): void {
        if (extensionRequestInfo && extensionRequestInfo.extensionDate && !isNaN(new Date(extensionRequestInfo.extensionDate).getTime())) {
            let date: string = new Date(extensionRequestInfo.extensionDate).toLocaleDateString();
            this.selectedDateNormal = date;
        }
    }

    ngOnDestroy() {
        if (this.infoSubscription) this.infoSubscription.unsubscribe();
    }
}