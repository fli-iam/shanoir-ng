/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Component } from '@angular/core';

import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

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