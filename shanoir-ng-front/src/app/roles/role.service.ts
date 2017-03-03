import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';

import { Role } from './role.model';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class RoleService {
    
    constructor(private http: Http) { }

    getRoles(): Promise<Role[]> {
        return this.http.get(AppUtils.BACKEND_API_ROLE_ALL_URL)
            .toPromise()
            .then(response => response.json() as Role[])
            .catch((error) => {
                console.error('Error while getting roles', error);
                return Promise.reject(error.message || error);
            });
    }
}