import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { User } from './user.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class UserService {
    
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    getUsers(): Promise<User[]> {
        return this.http.get(AppUtils.BACKEND_API_USER_ALL_URL)
            .toPromise()
            .then(response => response.json() as User[])
            .catch((error) => {
                console.error('Error while getting users', error);
                return Promise.reject(error.message || error);
        });
    }

    getUser(id: number): Promise<User> {
        return this.http.get(AppUtils.BACKEND_API_USER_URL + '/' + id)
            .toPromise()
            .then(response => response.json() as User)
            .catch((error) => {
                console.error('Error while getting user', error);
                return Promise.reject(error.message || error);
        });
    }

    create(user: User): Observable<User> {
        return this.http.post(AppUtils.BACKEND_API_USER_URL, JSON.stringify(user))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    requestAccount(user: User): Observable<User> {
        return this.http.post(AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, JSON.stringify(user))
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, user: User): Observable<User> {
        return this.http.put(AppUtils.BACKEND_API_USER_URL + '/' + id, JSON.stringify(user))
            .map(response => response.json() as User)
            .catch(this.handleErrorService.handleError);
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_USER_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete user', error);
                return Promise.reject(error.message || error);
        });
    }

    confirmAccountRequest(id: number, user: User): Observable<User> {
        return this.http.put(AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL, 
                JSON.stringify(user))
            .map(response => response.json() as User)
            .catch(this.handleErrorService.handleError);
    }

    denyAccountRequest(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL)
            .toPromise()
            .catch((error) => {
                console.error('Error deny user account request', error);
                return Promise.reject(error.message || error);
        });
    }
}