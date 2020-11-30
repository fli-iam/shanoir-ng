/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Injectable } from "@angular/core";
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { switchMap } from 'rxjs/operators';

import { KeycloakService } from "./keycloak.service";

/**
 * This provides a wrapper over the ng2 Http class that insures tokens are refreshed on each request.
 */
@Injectable()
export class KeycloakHttpInterceptor implements HttpInterceptor {

    constructor(
        private keycloakService: KeycloakService
    ) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let authReq: HttpRequest<any> = req.clone();
        // Bearer needed for private URL only (".../accountrequest" is a public URL)
        if (!req.url.endsWith('/accountrequest')) {
            authReq = this.setAuthHeader(authReq);
        }
        // Do not add Content-Type application/json for Form Data
        if (!(req.body instanceof FormData)) {
            authReq = authReq.clone({ headers: authReq.headers.set('Content-Type', 'application/json') });
        }
        // Pass on the cloned request instead of the original request.
        return next.handle(authReq).pipe(catchError((err: HttpErrorResponse): Observable<HttpEvent<any>> => { // return null }
      // (err: any) => {
            if (err instanceof HttpErrorResponse) {
                if (err.status === 401) {
                    return new Observable((observer) => {
                        this.keycloakService.getToken().then((token: string) => {
                            authReq = this.setAuthHeader(authReq);
                            observer.next();
                            observer.complete();
                        }).catch(() => {
                            this.keycloakService.logout();
                        });                        
                    }).pipe(switchMap(() => {
                        return next.handle(authReq);
                    }))
                }
                throw(err);
            }
        }
        ));
    }

    private setAuthHeader(req: HttpRequest<any>): HttpRequest<any> {
        const authHeader = KeycloakService.auth.authz ? KeycloakService.auth.authz.token : null;
        return req.clone({
            setHeaders: {
                Authorization: `Bearer ${authHeader}`
            }
        });
    }

}