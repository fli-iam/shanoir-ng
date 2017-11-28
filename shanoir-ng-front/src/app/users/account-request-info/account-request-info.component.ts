import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';

import { AccountRequestInfo } from './account-request-info.model';

@Component ({
    selector: 'account-request-info',
    templateUrl: 'account-request-info.component.html'
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
