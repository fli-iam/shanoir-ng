import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { LoginService } from './login.service';
import { Account } from '../account/account';

@Component({
    selector: 'shanoir-login',
    templateUrl: './app/login/login.html'
})

export class LoginComponent {
    loginForm: FormGroup;
    account:Account;

    constructor(private router: Router, private loginService: LoginService, fb:FormBuilder) {
        this.loginForm = fb.group({
           'email': ['', Validators.required],
           'password': ['', Validators.required]
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