import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { LoginService } from './login.service';

@Component({
    selector: 'shanoir-login',
    templateUrl: './app/login/login.html'
})

export class LoginComponent {
    loginForm: FormGroup;

    constructor(private loginService: LoginService, fb:FormBuilder) {
        this.loginForm = fb.group({
           'email': ['', Validators.required],
           'password': ['', Validators.required]
        });
    }
    
    login(): void {  
        this.loginService.login(this.loginForm.value.email,
                                this.loginForm.value.password);
    }
    
}