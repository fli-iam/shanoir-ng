import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';

import { AccountRequestInfo } from './account.request.info.model';

@Component ({
    selector: 'account-request-info',
    template: `
        <fieldset [formGroup]="accountRequestInfoForm">
            <ol>
                <li class="required">
                    <label i18n="Edit user|Institution label" class="required-label">Institution</label> 
                    <span class="right-col">
                        <input *ngIf="!requestAccountMode" type="text" id="institution" required formControlName="institution" [(ngModel)]="userAccountRequestInfo.institution" readonly/>
                        <input *ngIf="requestAccountMode" type="text" required maxlength="200" formControlName="institution"/>
                        <label *ngIf="requestAccountMode && formErrors.institution.includes('required')" class="form-validation-alert" i18n="Edit user|InstitutionRequiredError label">Institution is required!</label>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('length')" class="form-validation-alert" i18n="Edit user|InstitutionLengthError label">Institution length must be less than 200!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Service label" class="required-label">Service</label> 
                    <span class="right-col">
                        <input *ngIf="!requestAccountMode" type="text" id="service" required formControlName="service" [(ngModel)]="userAccountRequestInfo.service" readonly/>
                        <input *ngIf="requestAccountMode" type="text" required maxlength="200" formControlName="service"/>
                        <label *ngIf="requestAccountMode && formErrors.service.includes('required')" class="form-validation-alert" i18n="Edit user|ServiceRequiredError label">Service is required!</label>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('length')" class="form-validation-alert" i18n="Edit user|ServiceLengthError label">Service length must be less than 200!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Function label" class="required-label">Function</label> 
                    <span class="right-col">
                        <input *ngIf="!requestAccountMode" type="text" id="function" required formControlName="function" [(ngModel)]="userAccountRequestInfo.function" readonly/>
                        <input *ngIf="requestAccountMode" type="text" required maxlength="200" formControlName="function"/>
                        <label *ngIf="requestAccountMode && formErrors.function.includes('required')" class="form-validation-alert" i18n="Edit user|FunctionRequiredError label">Function is required!</label>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('length')" class="form-validation-alert" i18n="Edit user|FunctionLengthError label">Function length must be less than 200!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Study label" class="required-label">Concerned study</label> 
                    <span class="right-col">
                        <input *ngIf="!requestAccountMode" type="text" id="study" required formControlName="study" [(ngModel)]="userAccountRequestInfo.study" readonly/>
                        <input *ngIf="requestAccountMode" type="text" required maxlength="200" formControlName="study"/>
                        <label *ngIf="requestAccountMode && formErrors.study.includes('required')" class="form-validation-alert" i18n="Edit user|StudyRequiredError label">Study is required!</label>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('length')" class="form-validation-alert" i18n="Edit user|StudyLengthError label">Study length must be less than 200!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Contact label" class="required-label">My contact in Shanoir</label> 
                    <span class="right-col">
                        <input *ngIf="!requestAccountMode" type="text" id="contact" required formControlName="contact" [(ngModel)]="userAccountRequestInfo.contact" readonly/>
                        <input *ngIf="requestAccountMode" type="text" required maxlength="200" formControlName="contact"/>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('required')" class="form-validation-alert" i18n="Edit user|ContactRequiredError label">Contact is required!</label>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('length')" class="form-validation-alert" i18n="Edit user|ContactLengthError label">Contact length must be less than 200!</label>
                    </span>
                </li>
                <li class="required">
                    <label i18n="Edit user|Work label" class="required-label">Role in the study</label> 
                    <span class="right-col">
                        <input *ngIf="!requestAccountMode" type="text" id="work" required formControlName="work" [(ngModel)]="userAccountRequestInfo.work" readonly/>
                        <input *ngIf="requestAccountMode" type="text" required maxlength="200" formControlName="work"/>    
                        <label *ngIf="requestAccountMode && formErrors.work.includes('required')" class="form-validation-alert" i18n="Edit user|WorkRequiredError label">Work is required!</label>
                        <label *ngIf="requestAccountMode && formErrors.contact.includes('length')" class="form-validation-alert" i18n="Edit user|WorkLengthError label">Work length must be less than 200!</label>
                    </span>
                </li>
            </ol>
        </fieldset>
    `
})
export class AccountRequestInfoComponent implements OnInit {
    @Input() userAccountRequestInfo: AccountRequestInfo;
    @Input() accountRequestDemand: boolean;
    @Input() requestAccountMode: boolean;
    @Output() accountRequestInfo: EventEmitter<AccountRequestInfo> = new EventEmitter<AccountRequestInfo>();
    @Output() isValid: EventEmitter<boolean> = new EventEmitter<boolean>();
    public accountRequestInfoForm: FormGroup;
    ari: AccountRequestInfo = new AccountRequestInfo();

    constructor(private formBuilder: FormBuilder) { 
    }

    ngOnInit() {
        let contactFC, functionFC, institutionFC, serviceFC, studyFC, workFC: FormControl;
        if (this.requestAccountMode) {
            contactFC = new FormControl('', [Validators.required, Validators.maxLength(200)]);
            functionFC = new FormControl('', [Validators.required, Validators.maxLength(200)]); 
            institutionFC = new FormControl('', [Validators.required, Validators.maxLength(200)]);
            serviceFC = new FormControl('', [Validators.required, Validators.maxLength(200)]);
            studyFC = new FormControl('', [Validators.required, Validators.maxLength(200)]);
            workFC = new FormControl('', [Validators.required, Validators.maxLength(200)]);
        } else {
            contactFC = new FormControl('');
            functionFC = new FormControl(''); 
            institutionFC = new FormControl('');
            serviceFC = new FormControl('');
            studyFC = new FormControl('');
            workFC = new FormControl('');
        }
        this.accountRequestInfoForm = this.formBuilder.group({
            'contact': contactFC,
            'function': functionFC,
            'institution': institutionFC,
            'service': serviceFC,
            'study':studyFC,
            'work': workFC
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

        if(this.userAccountRequestInfo !== null) {
            this.ari = this.accountRequestInfoForm.value;
            this.accountRequestInfo.emit(this.ari);
        
            if (this.accountRequestInfoForm.valid) {
                this.isValid.emit(true);
            } else {
                this.isValid.emit(false);
            }
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
