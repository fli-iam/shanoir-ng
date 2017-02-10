import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';

import { AccountRequestInfo } from './account.request.info.model';

@Component ({
    moduleId: module.id,
    selector: 'account-request-info',
    template: `
    <td [formGroup]="accountRequestInfoForm" class="required">
        <fieldset>
            <ol>
                <li class="required">
                    <label i18n="Edit user|Contact label" class="required-label">Contact: </label> 
                    <span class="right-col">
                        <input type="text" id="contact" required formControlName="contact"/>
                        <label *ngIf="formErrors.contact.includes('required')" class="form-validation-alert" i18n="Edit user|ContactRequiredError label">Contact is required!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Function label" class="required-label">Function: </label> 
                    <span class="right-col">
                        <input type="text" id="function" required formControlName="function"/>
                        <label *ngIf="formErrors.function.includes('required')" class="form-validation-alert" i18n="Edit user|FunctionRequiredError label">Function is required!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Institution label" class="required-label">Institution: </label> 
                    <span class="right-col">
                        <input type="text" id="institution" required formControlName="institution"/>
                        <label *ngIf="formErrors.institution.includes('required')" class="form-validation-alert" i18n="Edit user|InstitutionRequiredError label">Institution is required!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Service label" class="required-label">Service: </label> 
                    <span class="right-col">
                        <input type="text" id="service" required formControlName="service"/>
                        <label *ngIf="formErrors.service.includes('required')" class="form-validation-alert" i18n="Edit user|ServiceRequiredError label">Service is required!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Study label" class="required-label">Study: </label> 
                    <span class="right-col">
                        <input type="text" id="study" required formControlName="study"/>
                        <label *ngIf="formErrors.study.includes('required')" class="form-validation-alert" i18n="Edit user|StudyRequiredError label">Study is required!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Work label" class="required-label">Work: </label> 
                    <span class="right-col">
                        <input type="text" id="work" required formControlName="work"/>
                        <label *ngIf="formErrors.work.includes('required')" class="form-validation-alert" i18n="Edit user|WorkRequiredError label">Work is required!</label>
                    </span>
                </li>
            </ol>
        </fieldset>
    </td>
    `
})
export class AccountRequestInfoComponent implements OnInit {
    @Output() accountRequestInfo: EventEmitter<AccountRequestInfo> = new EventEmitter<AccountRequestInfo>();
    @Output() isValid: EventEmitter<Boolean> = new EventEmitter<Boolean>();
    private accountRequestInfoForm: FormGroup;
    ari: AccountRequestInfo = new AccountRequestInfo();

    constructor(private formBuilder: FormBuilder) { 
    }

    ngOnInit() {
    this.accountRequestInfoForm = this.formBuilder.group({
        'contact': ['', Validators.required],
        'function': ['', Validators.required],
        'institution': ['', Validators.required],
        'service': ['', Validators.required],
        'study': ['', Validators.required],
        'work': ['', Validators.required]
    });
    this.accountRequestInfoForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
    this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.accountRequestInfoForm) { return; }
        const form = this.accountRequestInfoForm;
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

        this.ari = this.accountRequestInfoForm.value;
        this.accountRequestInfo.emit(this.ari);
       
        if (this.accountRequestInfoForm.valid) {
            this.isValid.emit(true);
        } else {
            this.isValid.emit(false);
        }
    }

    formErrors = {
        'contact': '',
        'function': '',
        'institution': '',
        'service': '',
        'study': '',
        'work': ''
    };
}
