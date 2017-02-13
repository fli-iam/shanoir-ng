import { Injectable, Injector } from '@angular/core';
import { Http, XHRBackend, RequestOptions, Request, RequestOptionsArgs, Response, Headers } from '@angular/http';
import { Router } from '@angular/router';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { LoginService } from 'app/shared/login/login.service';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class SecureHttp extends Http {

    constructor (backend: XHRBackend, options: RequestOptions, private injector: Injector) {
        super(backend, options);
    }

    request(url: string|Request, options?: RequestOptionsArgs): Observable<Response> {
        let token: string = sessionStorage.getItem(AppUtils.STORAGE_TOKEN);
        let refreshToken: string = sessionStorage.getItem(AppUtils.STORAGE_REFRESH_TOKEN);
        let authentRequest:boolean = false;
        if (typeof url === 'string') {
            if (!options) {
                // Let's make option object
                options = {headers: new Headers()};
            }
            options.headers.append('Content-Type', 'application/json');
            if (url.indexOf('/authenticate') != -1) {
                authentRequest = true;
                options.headers.set('X-Authorization', `Bearer ${refreshToken}`);
            } else {
                // Meaning we have to add the token to the options, not in url
                options.headers.set('X-Authorization', `Bearer ${token}`);
            }
        } else {
            url.headers.append('Content-Type', 'application/json');
            if (url.url.indexOf('/authenticate') != -1 || url.url.indexOf(AppUtils.BACKEND_API_USER_URL+ AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL) != -1) {
                authentRequest = true;
                url.headers.set('X-Authorization', `Bearer ${refreshToken}`);
            } else {
                // >e have to add the token to the url object
                url.headers.set('X-Authorization', `Bearer ${token}`);
            }
        }
        
        if (!authentRequest && this.loginService.hasTokenExpired()) {
            // Try to refresh token
            return this.loginService.refreshAuthToken()
                .flatMap((res) => {
                    // Token refreshed
                    if (typeof url === 'string') {
                        // >e have to add the new token to the options
                        options.headers.set('X-Authorization', `Bearer ${res.json().token}`);
                    } else {
                        // >e have to add the new token to the url object
                        url.headers.set('X-Authorization', `Bearer ${res.json().token}`);
                    }
                    return super.request(url, options);
                })
                .catch((err) => {
                    // Refresh token has expired
                    this.loginService.removeAccount();
                    return Observable.throw(err);
                });
        } else {
            return super.request(url, options).catch(this.catchAuthError(this));
        }
    }

    public get loginService(): LoginService { 
        // This creates loginService property on your service.
        return this.injector.get(LoginService);
    }

    private catchAuthError (self: SecureHttp) {
        // We have to pass HttpService's own instance here as `self`
        return (res: Response) => {
            if (res.status === 401) {
                // If not authenticated
                this.loginService.removeAccount();
            }
            return Observable.throw(res);
        };
    }

}