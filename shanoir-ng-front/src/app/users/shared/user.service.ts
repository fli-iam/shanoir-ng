import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { User } from './user.model';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class UserService {
    
    constructor(private http: Http) { }

    getUsers(): Promise<User[]> {
        return this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_ALL_URL)
            .toPromise()
            .then(response => response.json() as User[])
            .catch((error) => {
                console.error('Error while getting users', error);
                return Promise.reject(error.message || error);
        });
    }

    getUser(id: number): Promise<User> {
        return this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id)
            .toPromise()
            .then(response => response.json() as User)
            .catch((error) => {
                console.error('Error while getting user', error);
                return Promise.reject(error.message || error);
        });
    }

    create(user: User): Observable<User> {
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL, JSON.stringify(user))
            .map(this.extractData)
            .catch(this.handleError);
    }

    requestAccount(user: User): Observable<User> {
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL+ AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, JSON.stringify(user))
            .map(this.extractData)
            .catch(this.handleError);
    }

    update(id: number, user: User): Observable<User> {
        return this.http.put(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id, JSON.stringify(user))
            .map(response => response.json() as User)
            .catch(this.handleError);
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete user', error);
                return Promise.reject(error.message || error);
        });
    }

    confirmAccountRequest(id: number, user: User): Observable<User> {
        return this.http.put(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL, 
                JSON.stringify(user))
            .map(response => response.json() as User)
            .catch(this.handleError);
    }

    denyAccountRequest(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL)
            .toPromise()
            .catch((error) => {
                console.error('Error deny user account request', error);
                return Promise.reject(error.message || error);
        });
    }

    private extractData(res: Response) {
        let body = res.json();
        return body.data || { };
    }

    private handleError(error: Response | any) {
        let errMsg: string;
        if (error instanceof Response) {
            const body = error.json() || '';
            errMsg= "[" + body.code + "]: " + body.message;
            if (body.details) {
                let errDetails = body.details.fieldErrors || '';
                for (var errKey in errDetails) {
                    errMsg += "; " + errKey + " should be ";
                    var errDetailsByKey = errDetails[errKey][0];
                    for (var errDetail in errDetailsByKey) {
                        if (errDetail === "code")
                        errMsg += errDetailsByKey[errDetail];
                    }
                }
            }
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
    }
}