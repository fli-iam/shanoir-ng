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

    create(user: User): Observable<User> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        headers.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_CREATE_USER_URL, JSON.stringify(user), new RequestOptions({ headers: headers, withCredentials: true }))
            .map(this.extractData)
            .catch(this.handleError);
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
            let errDetails = body.details.formErrors || '';
            for (let errDetail of errDetails) {
                errMsg += "; " + errDetail.fieldName + " " + errDetail.errorCodes[0];
            }
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
    }
}