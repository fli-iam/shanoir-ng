import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
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
         return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_AUTHENTICATE_PATH, 
                JSON.stringify({login:login, password:password}))
            .map((res) => {
                sessionStorage.setItem(AppUtils.STORAGE_ACCOUNT_TOKEN, res.text());
                sessionStorage.setItem(AppUtils.STORAGE_TOKEN, res.json().token);
                sessionStorage.setItem(AppUtils.STORAGE_TOKEN_TIMEOUT, this.getTokenTimeoutDateStr(res.json().tokenExpirationTime));
                sessionStorage.setItem(AppUtils.STORAGE_REFRESH_TOKEN, res.json().refreshToken);
 
                let account:Account = new Account(res.json());
                this.sendLoginSuccess(account);
                return account;
            })
            .catch((error) => {
                if(error.status === 401) {
                    this.accountEventsService.logout({error: error.json()});
                }
                return Observable.throw(error.message || error || 'Authentication fails');
            });
    }
    
    refreshAuthToken(): Observable<Response> {
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_REFRESH_AUTH_TOKEN_PATH, "")
            .map((res) => {
                sessionStorage.setItem(AppUtils.STORAGE_TOKEN, res.json().token);
                sessionStorage.setItem(AppUtils.STORAGE_TOKEN_TIMEOUT, this.getTokenTimeoutDateStr(res.json().tokenExpirationTime));
                sessionStorage.setItem(AppUtils.STORAGE_REFRESH_TOKEN, res.json().refreshToken);
                return res;
            });
    }
    
    sendLoginSuccess(account?:Account): void {
        if(!account) {
            account = new Account(JSON.parse(sessionStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN)));
        }
        this.accountEventsService.loginSuccess(account);
    }
    
    logout(): void {
        this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_LOGOUT_PATH, "").subscribe(
            () => {
                this.accountEventsService.logout(new Account(JSON.parse(sessionStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN))));
                this.removeAccount();
            }
        );
    }

    removeAccount():void {
        sessionStorage.removeItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
        sessionStorage.removeItem(AppUtils.STORAGE_TOKEN);
        this.router.navigate(['/login']);
    }

    isAuthenticated():boolean {
        return !!sessionStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
    }

    hasTokenExpired():boolean {
        let tokenTimeout: string = sessionStorage.getItem(AppUtils.STORAGE_TOKEN_TIMEOUT);
        if (tokenTimeout) {
            return (new Date(+tokenTimeout) < new Date());
        }
        return true;
    }

    getLoggedUser(): Account {
        if (!!sessionStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN)) {
            return JSON.parse(sessionStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN));
        }
        this.router.navigate(['/login']);
    }

    isUserAdmin(): boolean {
        return true;
        /*let account = this.getLoggedUser();
        if (account && account.authorities.indexOf("adminRole") != -1) {
            return true;
        }
        return false;*/
    }

    getTokenTimeoutDateStr(tokenExpirationTime: number): string {
        let tokenTimeoutDate: Date = new Date();
        if (tokenExpirationTime) {
            tokenTimeoutDate.setTime(tokenTimeoutDate.getTime() + tokenExpirationTime*60000);
        }
        return tokenTimeoutDate.getTime().toString();
    }

}