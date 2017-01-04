import { Injectable } from '@angular/core';
import { Response, Headers, Http, RequestOptions } from '@angular/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';

import { Account } from 'app/users/account/account';
import { AccountEventsService } from 'app/users/account/account.events.service';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class LoginService {
    constructor(private http:Http, private router: Router, private accountEventsService:AccountEventsService) {
        this.accountEventsService = accountEventsService;
    }

    login(login:string, password:string): Observable<Account> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
        
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_AUTHENTICATE_PATH,  JSON.stringify({login:login, password:password}), { headers: headers, withCredentials: true })
            .map((res) => {
                localStorage.setItem(AppUtils.STORAGE_ACCOUNT_TOKEN, res.text());
                localStorage.setItem(AppUtils.STORAGE_TOKEN, res.json().token);
 
                let account:Account = new Account(res.json());
                this.sendLoginSuccess(account);
                return account;
            })
            .catch((error) => {
                if(error.status === 401) {
                    this.accountEventsService.logout({error: error.text()});
                }
                return Observable.throw(error.message || error || 'Authentication fails');
            });
    }
    
    sendLoginSuccess(account?:Account): void {
        if(!account) {
            account = new Account(JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN)));
        }
        this.accountEventsService.loginSuccess(account);
    }
    
    logout(): void {
        let headersLogout = new Headers();
        headersLogout.append('Content-Type', 'application/json');
        headersLogout.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        
        this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_LOGOUT_PATH, "", new RequestOptions({ headers : headersLogout, withCredentials: true })).subscribe(
            () => {
                this.accountEventsService.logout(new Account(JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN))));
                this.removeAccount();
                this.router.navigate(['/login']);
            }
        );
    }

    removeAccount():void {
        localStorage.removeItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
        localStorage.removeItem(AppUtils.STORAGE_TOKEN);
    }

    isAuthenticated():boolean {
        return !!localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
    }

    getLoggedUser(): Account {
        if (!!localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN)) {
            return JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN));
        }
        this.router.navigate(['/login']);
    }

    isUserAdmin(): boolean {
        let account = this.getLoggedUser();
        if (account && account.authorities.indexOf("adminRole") != -1) {
            return true;
        }
        return false;
    }

}