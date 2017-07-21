import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { ExtensionRequestInfo } from './extension.request.info.model';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';

@Component({
    selector: 'extensionRequest',
    templateUrl: 'extension.request.component.html'
})

export class ExtensionRequestComponent implements OnInit {
    @Output() closing = new EventEmitter();
    extensionRequestInfo: ExtensionRequestInfo = new ExtensionRequestInfo();
    extensionRequestForm: FormGroup;
    isDateValid: boolean = true;
    userId: number;
    selectedDateNormal: string = '';

    constructor(private router: Router, private location: Location, private route: ActivatedRoute,
        private userService: UserService, private fb: FormBuilder) {
    }

    ngOnInit(): void {
        this.getUser();
        this.buildForm();
    }

     getUser(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                return this.userService.getUser(KeycloakService.auth.userId);
            })
            .subscribe((user: User) => {
                this.extensionRequestInfo.extensionDate = new Date();
                if (user.expirationDate) {
                    this.extensionRequestInfo.extensionDate = new Date(user.expirationDate);
                }
                this.getDateToDatePicker(this.extensionRequestInfo);
            });
    }

    extensionRequest(): void {
        this.submit();
        this.userService.requestExtension(this.extensionRequestInfo)
            .then(() => {
                this.router.navigate(['/home']);
            }, (err: String) => {
                console.log(err);
            });
    }

    submit(): void {
        this.extensionRequestInfo = this.extensionRequestForm.value;
        this.setDateFromDatePicker();
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
            'extensionDate': [this.extensionRequestInfo.extensionDate, [Validators.required]],
            'extensionMotivation': [this.extensionRequestInfo.extensionMotivation, [Validators.required]],
            'extensionRequest': new FormControl('true')
        });

        this.extensionRequestForm.valueChanges
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

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    onDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedDateNormal = event.formatted;
        }
    }

    onInputFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isDateValid = false;
            } else {
                this.isDateValid = true;
            }
        } else {
            this.isDateValid = true;
            this.selectedDateNormal = null;
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedDateNormal) {
            var from = this.selectedDateNormal.valueOf().split("/");
            this.extensionRequestInfo.extensionDate = new Date(from[2], from[1] - 1, from[0]);
        } else {
            this.extensionRequestInfo.extensionDate = null;
        }
    }

    getDateToDatePicker(extensionRequestInfo: ExtensionRequestInfo): void {
        if (extensionRequestInfo && extensionRequestInfo.extensionDate && !isNaN(new Date(extensionRequestInfo.extensionDate).getTime())) {
            let date: string = new Date(extensionRequestInfo.extensionDate).toLocaleDateString();
            this.selectedDateNormal = date;
        }
    }
}