import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { LoginService } from 'app/shared/login/login.service';
import { Account } from '../account/account';
import { AccountEventsService } from '../account/account.events.service';

@Component({
    selector: 'login',
    moduleId: module.id,
    templateUrl: 'login.component.html',
    styleUrls: ['../../shared/css/common.css', 'login.component.css']
})

export class LoginComponent {
    loginForm: FormGroup;
    account: Account;
    shanoirNGImageUrl: string;
    errorCode: string;

    constructor(private router: Router, private loginService: LoginService, accountEventsService: AccountEventsService, fb:FormBuilder) {
        this.shanoirNGImageUrl = '/images/logo.shanoir.white.png';

        this.loginForm = fb.group({
           'email': ['', Validators.required],
           'password': ['', Validators.required]
        });

        accountEventsService.subscribe((account) => {
            if(!account.authenticated) {
                if(account.error) {
                    this.errorCode = account.error.message;
                }
            }
        });
    }

    login(): void {
        this.loginService.login(this.loginForm.value.email,
                                this.loginForm.value.password)
            .subscribe((account) => {
                this.account = account;
                console.log('Successfully logged',account);
                this.router.navigate(['/home']);
            });
    }

}