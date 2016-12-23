import { Component } from '@angular/core';

import { LoginService } from 'app/shared/login/login.service';

@Component({
    selector: 'navbar',
    moduleId: module.id,
    templateUrl: 'navbar.component.html',
    styleUrls: ['../css/common.css', 'navbar.component.css']
})

export class NavbarComponent {

    constructor(private loginService: LoginService) {
    }

    isUserAdmin(): boolean {
        return this.loginService.isUserAdmin();
    }
    
}