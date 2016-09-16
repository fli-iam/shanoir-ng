import { Injectable } from '@angular/core';
import { Response, Headers, Http } from '@angular/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';

import { Account } from '../account/account';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class LoginService {

    constructor(private http:Http, private router: Router) {
    }

    login(login:string, password:string):Observable<Account> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
 
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_AUTHENTICATE_PATH,  JSON.stringify({login:login, password:password}), {headers:headers})
            .map((res:Response) => {
                localStorage.setItem(AppUtils.STORAGE_ACCOUNT_TOKEN, res.text());
 
                let account:Account = new Account(res.json());
                account.authenticated = true;
                return account;
            });
    }
    
    logout(): void {
        this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_LOGOUT_PATH).subscribe(() => {
            let account:Account = new Account(JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN)));
            if (account) {
                account.authenticated = false;
            }
            localStorage.removeItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
            this.router.navigate(['/login']);
        });
    }

    isAuthenticated():boolean {
        return !!localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
    }

}