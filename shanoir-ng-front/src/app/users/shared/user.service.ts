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

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';

import { User } from './user.model';



@Injectable()
export class UserService extends EntityService<User> {

    API_URL = AppUtils.BACKEND_API_USER_URL;

    constructor(protected http: HttpClient) {
        super(http);
    }

    getEntityInstance() { return new User(); }

    confirmAccountRequest(id: number, user: User): Promise<User> {
        return firstValueFrom(this.http.put<User>(this.API_URL + '/' + id + AppUtils.BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL, JSON.stringify(user)));
    }

    denyAccountRequest(id: number): Promise<void> {
        return firstValueFrom(this.http.delete<void>(this.API_URL + '/' + id + AppUtils.BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL));
    }

    requestAccount(user: User): Promise<User> {
        return firstValueFrom(this.http.post<User>(AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, JSON.stringify(user)));
    }

    requestExtension(extensionRequestInfo: ExtensionRequestInfo): Promise<void> {
        return firstValueFrom(this.http.post<void>(AppUtils.BACKEND_API_USER_EXTENSION_REQUEST_URL, JSON.stringify(extensionRequestInfo)));
    }

    getAllAccountRequests(): Promise<User[]> {
        return firstValueFrom(this.http.get<any[]>(this.API_URL + '/accountRequests'))
            .then(this.mapEntityList);
    }

    countAllUsers(): Promise<number> {
        return firstValueFrom(this.http.get<number>(AppUtils.BACKEND_API_USER_PUBLIC_COUNT))
            .then((count: number) => {
                return count;
            });
    }

    countLastMonthEvents(): Promise<number> {
        const param = new HttpParams().set('days', AppUtils.BACKEND_API_EVENTS_COUNT_DAYS_PARAM);
        return firstValueFrom(this.http.get<number>(AppUtils.BACKEND_API_USER_PUBLIC_COUNT_LAST_MONTH_EVENTS, { params: param }))
            .then((count: number) => {
                return count;
            });
    }

}
