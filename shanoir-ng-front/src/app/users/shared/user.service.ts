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

import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Subject } from 'rxjs';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { AccessRequest } from '../../users/access-request/access-request.model'

import { User } from './user.model';



@Injectable()
export class UserService extends EntityService<User> implements OnDestroy {

    public accessRequets: Subject<number> = new Subject();
    private _accessRequests: number = 0;
    private refreshTimeout;

    API_URL = AppUtils.BACKEND_API_USER_URL;

    constructor(protected http: HttpClient) {
        super(http);
        this.refreshTimeout = setInterval(() => {
            this.getAccessRequestsForAdmin();
        }, 1000 * 60 * 2);
    }

    ngOnDestroy(): void {
        clearInterval(this.refreshTimeout);
    }

    decreaseAccessRequests() {
        this._accessRequests --;
        this.accessRequets.next(this._accessRequests);
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

    requestExtension(extensionRequestInfo: ExtensionRequestInfo): Promise<void> {
        return this.http.post<void>(AppUtils.BACKEND_API_USER_EXTENSION_REQUEST_URL, JSON.stringify(extensionRequestInfo))
            .toPromise();
    }

    getAllAccountRequests(): Promise<User[]> {
        return this.http.get<any[]>(this.API_URL + '/accountRequests')
            .toPromise()
            .then(this.mapEntityList);
    }

    getAccessRequests(): Promise<AccessRequest[]> {
        return this.http.get<AccessRequest[]>(AppUtils.BACKEND_API_USER_ACCESS_REQUEST_BY_USER)
            .toPromise().then((typeResult: AccessRequest[]) => {
                return typeResult;
            });
    }

    getAccessRequestsForAdmin(): Promise<AccessRequest[]> {
        return this.http.get<AccessRequest[]>(AppUtils.BACKEND_API_USER_ACCESS_REQUEST_BY_ADMIN)
            .toPromise().then((typeResult: AccessRequest[]) => {
                this._accessRequests = typeResult?.length;
                this.accessRequets.next(typeResult?.length);
                return typeResult;
            });
    }

    countAllUsers(): Promise<number> {
        return this.http.get<number>(AppUtils.BACKEND_API_USER_PUBLIC_COUNT)
            .toPromise().then((count: number) => {
                return count;
            });
    }

    countLastMonthEvents(): Promise<number> {
        const param = new HttpParams().set('days', AppUtils.BACKEND_API_EVENTS_COUNT_DAYS_PARAM);
        return this.http.get<number>(AppUtils.BACKEND_API_USER_PUBLIC_COUNT_LAST_MONTH_EVENTS, { params: param })
            .toPromise().then((count: number) => {
                return count;
            });
    }
}
