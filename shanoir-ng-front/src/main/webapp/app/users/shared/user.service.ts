import { Injectable } from '@angular/core';
import { Response, Headers, Http, RequestOptions } from '@angular/http';

import { User } from './user.model';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class UserService {
    
    constructor(private http: Http) { }

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

    create(user: User): Promise<User> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_CREATE_USER_URL, JSON.stringify(user), new RequestOptions({ headers: headers, withCredentials: true }))
            .toPromise()
            .then((res:Response) => res.json())
            .catch((error) => {
                console.error('Error while creating users', error);
                return Promise.reject(error.message || error);
            });
}