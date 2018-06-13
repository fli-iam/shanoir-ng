import { Injectable } from "@angular/core";
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Observable } from 'rxjs';

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
        return next.handle(authReq).catch((err: any) => {
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
                    }).switchMap(() => {
                        return next.handle(authReq);
                    })
                }
                return Observable.throw(err);
            }
        });
    }

    private setAuthHeader(req: HttpRequest<any>): HttpRequest<any> {
        const authHeader = KeycloakService.auth.authz.token;
        return req.clone({
            setHeaders: {
                Authorization: `Bearer ${authHeader}`
            }
        });
    }

}