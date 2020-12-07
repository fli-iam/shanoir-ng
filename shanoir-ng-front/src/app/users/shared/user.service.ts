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

import { Injectable } from '@angular/core';
import { ErrorObservable } from 'rxjs/observable/ErrorObservable';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { User } from './user.model';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class UserService extends EntityService<User>{

    API_URL = AppUtils.BACKEND_API_USER_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new User(); }

    confirmAccountRequest(id: number, user: User): Promise<User> {
        return this.http.put<User>(this.API_URL + '/' + id + AppUtils.BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL, JSON.stringify(user))
            .toPromise();
    }

    denyAccountRequest(id: number): Promise<void> {
        return this.http.delete<void>(this.API_URL + '/' + id + AppUtils.BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL)
            .toPromise();
    }

    requestAccount(user: User): Promise<User> {
        return this.http.post<User>(AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, JSON.stringify(user)).toPromise();
    }

    requestExtension(extensionRequestInfo: ExtensionRequestInfo): Promise<void | ErrorObservable> {
        return this.http.post<void>(AppUtils.BACKEND_API_USER_EXTENSION_REQUEST_URL, JSON.stringify(extensionRequestInfo))
            .toPromise();
    }
}