import { Component } from '@angular/core';

const GUEST_ROLE_ID: number = 2;

@Component({
    selector: 'accountRequest',
    templateUrl: 'account.request.component.html',
    styleUrls: ['account.request.component.css']
})

export class AccountRequestComponent {
    private shanoirLogoUrl: string;

    constructor() {
        this.shanoirLogoUrl = 'assets/images/logo.shanoir.white.png';
    }

}