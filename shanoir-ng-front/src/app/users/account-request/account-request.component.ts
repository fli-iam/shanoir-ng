import { Component } from '@angular/core';

import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

const GUEST_ROLE_ID: number = 2;

@Component({
    selector: 'accountRequest',
    templateUrl: 'account-request.component.html'
})

export class AccountRequestComponent {
    public shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_BLACK_LOGO_PATH;
    public requestSent: boolean = false;
    public errorOnRequest: boolean = false;

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