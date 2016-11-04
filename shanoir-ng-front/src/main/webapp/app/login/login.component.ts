import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { LoginService } from './login.service';
import { Account } from '../account/account';
import { AccountEventsService } from '../account/account.events.service';

@Component({
    selector: 'shanoir-login',
    templateUrl: './app/login/login.html'
})

export class LoginComponent {
    loginForm: FormGroup;
    account:Account;
    error:string;

    constructor(private router: Router, private loginService: LoginService, accountEventsService: AccountEventsService, fb:FormBuilder) {
        this.loginForm = fb.group({
           'email': ['', Validators.required],
           'password': ['', Validators.required]
        });
        
        accountEventsService.subscribe((account) => {
            if(!account.authenticated) {
                if(account.error) {
                    if(account.error.indexOf('BadCredentialsException') !== -1) {
                        this.error = 'Username and/or password are invalid !';
                    } else {
                        this.error = account.error;
                    }
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