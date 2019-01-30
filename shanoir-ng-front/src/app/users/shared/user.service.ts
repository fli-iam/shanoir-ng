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
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { ErrorObservable } from 'rxjs/observable/ErrorObservable';

import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { User } from './user.model';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class UserService {

    constructor(private http: HttpClient) { }

    confirmAccountRequest(id: number, user: User): Observable<User> {
        return this.http.put<User>(AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL,
            JSON.stringify(user))
            .map(response => response);
    }

    create(user: User): Observable<User> {
        return this.http.post<User>(AppUtils.BACKEND_API_USER_URL, JSON.stringify(user))
            .map(res => res);
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_USER_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete user', error);
                return Promise.reject(error.message || error);
            });
    }

    denyAccountRequest(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL)
            .toPromise()
            .catch((error) => {
                console.error('Error deny user account request', error);
                return Promise.reject(error.message || error);
            });
    }

    getUser(id: number): Promise<User> {
        return this.http.get<User>(AppUtils.BACKEND_API_USER_URL + '/' + id)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting user', error);
                return Promise.reject(error.message || error);
            });
    }

    getUsers(): Promise<User[]> {
        return this.http.get<User[]>(AppUtils.BACKEND_API_USER_URL)
            .toPromise();
    }

    requestAccount(user: User): Observable<User> {
        return this.http.post<User>(AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, JSON.stringify(user));
    }

    requestExtension(extensionRequestInfo: ExtensionRequestInfo): Promise<void | ErrorObservable> {
        return this.http.put<void>(AppUtils.BACKEND_API_USER_EXTENSION_REQUEST_URL, JSON.stringify(extensionRequestInfo))
            .toPromise();
    }

    update(id: number, user: User): Observable<User> {
        return this.http.put<User>(AppUtils.BACKEND_API_USER_URL + '/' + id, JSON.stringify(user))
            .map(response => response);
    }

}