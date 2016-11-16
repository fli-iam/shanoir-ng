import { Component } from '@angular/core';

import { LoginService } from 'app/shared/login/login.service';

@Component({
    selector: 'shanoir-home',
    moduleId: module.id,
    templateUrl: 'home.component.html'
})

export class HomeComponent {

    constructor(private loginService: LoginService) {
    }
    
    logout(event): void {  
        event.preventDefault();
        this.loginService.logout();
    }
    
}