import { Injectable } from '@angular/core';
import { Response, Headers, Http } from '@angular/http';

import { Role } from './role.model';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class RoleService {
    
    constructor(private http: Http) { }

    getRoles(): Promise<Role[]> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_ROLE_ALL_URL, { headers: headers })
            .toPromise()
            .then(response => response.json() as Role[])
            .catch((error) => {
                console.error('Error while getting roles', error);
                return Promise.reject(error.message || error);
            });
    }
}