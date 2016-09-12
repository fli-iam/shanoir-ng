import { Component } from '@angular/core';

import { LoginService } from './login/login.service';

@Component({
  selector: 'shanoir-ng-app',
  templateUrl: './app/app.html'
})

export class AppComponent {
    constructor(private loginService: LoginService) {
    }
    
    isAuthenticated(): boolean {
        return this.loginService.isAuthenticated();
    }

}