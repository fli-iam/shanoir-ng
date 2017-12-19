import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Role } from './role.model';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class RoleService {

    constructor(private http: HttpClient) { }

    getRoles(): Promise<Role[]> {
        return this.http.get<Role[]>(AppUtils.BACKEND_API_ROLE_ALL_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting roles', error);
                return Promise.reject(error.message || error);
            });
    }
}