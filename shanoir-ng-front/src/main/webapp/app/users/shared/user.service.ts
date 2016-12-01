import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import { User } from './user.model';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class UserService {
    
    constructor(private http:Http) { }

    getUsers(): Promise<User[]> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_ALL_URL, { headers: headers })
            .toPromise()
            .then(response => response.json() as User[])
            .catch((error) => {
                console.error('Error while getting users', error);
                return Promise.reject(error.message || error);
            });
    }
    
}