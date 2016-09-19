import { Injectable } from '@angular/core';
import { Response, Headers, Http } from '@angular/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';

import { Account } from '../account/account';
import { AccountEventsService } from '../account/account.events.service';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class LoginService {
    
    constructor(private http:Http, private router: Router, private accountEventsService:AccountEventsService) {
        this.accountEventsService = accountEventsService;
    }

    login(login:string, password:string): Observable<Account> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');
 
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_AUTHENTICATE_PATH,  JSON.stringify({login:login, password:password}), {headers:headers})
            .map((res:Response) => {
                localStorage.setItem(AppUtils.STORAGE_ACCOUNT_TOKEN, res.text());
 
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
        this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_LOGOUT_PATH).subscribe(() => {
            this.accountEventsService.logout(new Account(JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN))));
            this.removeAccount();
            this.router.navigate(['/login']);
        });
    }

    removeAccount():void {
        localStorage.removeItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
    }

    isAuthenticated():boolean {
        return !!localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
    }

}