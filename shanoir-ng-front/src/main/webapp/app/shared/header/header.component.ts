import { Component } from '@angular/core';

import { LoginService } from '../login/login.service';

@Component({
    selector: 'header',
    moduleId: module.id,
    templateUrl: 'header.component.html',
    styleUrls: ['../css/common.css', 'header.component.css']
})

export class HeaderComponent {
    shanoirLogoUrl: string;

    constructor(private loginService: LoginService) {
        this.shanoirLogoUrl = '/images/logo.shanoir.white.png';
    }

    logout(event): void {
        event.preventDefault();
        this.loginService.logout();
    }
}