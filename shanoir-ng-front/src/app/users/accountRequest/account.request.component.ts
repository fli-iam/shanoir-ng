import { Component } from '@angular/core';

const GUEST_ROLE_ID: number = 2;

@Component({
    selector: 'accountRequest',
    templateUrl: 'account.request.component.html',
    styleUrls: ['account.request.component.css']
})

export class AccountRequestComponent {
    private requestSent: boolean = false;
    private errorOnRequest: boolean = false;

    closeAccountRequest(res: any) {
        if (!res) {
            this.errorOnRequest = true;
        }
        this.requestSent = true;
    }

    getOut() {
        window.location.href = process.env.LOGOUT_REDIRECT_URL;
    }

}