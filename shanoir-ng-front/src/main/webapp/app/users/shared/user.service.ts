import { Injectable } from '@angular/core';
import { Response, Headers, Http, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';

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

    getUser(id: number): Promise<User> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id, { headers: headers })
            .toPromise()
            .then(response => response.json() as User)
            .catch((error) => {
                console.error('Error while getting user', error);
                return Promise.reject(error.message || error);
        });
    }

    create(user: User): Observable<User> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL, JSON.stringify(user), new RequestOptions({ headers: headers, withCredentials: true }))
            .map(this.extractData)
            .catch(this.handleError);
    }

    update(id: number, user: User): Promise<User> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.put(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id, JSON.stringify(user), new RequestOptions({ headers: headers, withCredentials: true }))
            .toPromise()
            .then(response => response.json() as User)
            .catch((error) => {
                console.error('Error while updating user', error);
                return Promise.reject(error.message || error);
        });
    }

    handleAccountRequest(id: number, acceptRequest: boolean): Promise<User> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));

        return this.http.patch(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_USER_URL + '/' + id + AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, acceptRequest, new RequestOptions({ headers: headers, withCredentials: true }))
            .toPromise()
            .then(response => response.json() as User)
            .catch((error) => {
                console.error('Error while getting user', error);
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
                let errDetails = body.details.formErrors || '';
                for (let errDetail of errDetails) {
                    errMsg += "; " + errDetail.fieldName + " " + errDetail.errorCodes[0];
                }
            }
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
    }
}