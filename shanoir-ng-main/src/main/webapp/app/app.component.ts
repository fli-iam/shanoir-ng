import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { LoginService } from './login/login.service';

@Component({
    selector: 'shanoir-ng-app',
    templateUrl: './app/app.html'
})

export class AppComponent {
    constructor(private router: Router, private loginService: LoginService) {
        router.events.subscribe(e => {
            if(e.url !== '/login') {
                if(!this.loginService.isAuthenticated()) {
                    this.router.navigate(['/login']);
                }
            }
        });
    }
    
    isAuthenticated(): boolean {
        return this.loginService.isAuthenticated();
    }

}