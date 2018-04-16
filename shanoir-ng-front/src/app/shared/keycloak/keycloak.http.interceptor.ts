import { Injectable } from "@angular/core";
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Observable } from 'rxjs';

import { KeycloakService } from "./keycloak.service";

/**
 * This provides a wrapper over the ng2 Http class that insures tokens are refreshed on each request.
 */
@Injectable()
export class KeycloakHttpInterceptor implements HttpInterceptor {

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Get the auth header from the service.
        const authHeader = KeycloakService.auth.authz.token;
        // Clone the request to add the new header.
        let authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${authHeader}`
            }
        });
        // Do not add Content-Type application/json for Form Data
        if (!(req.body instanceof FormData)) {
            authReq = authReq.clone({ headers: authReq.headers.set('Content-Type', 'application/json') });
        }
        // Pass on the cloned request instead of the original request.
        return next.handle(authReq).catch((err: any) => {
            if (err instanceof HttpErrorResponse) {
                if (err.status === 401) {
                    window.location.href = process.env.LOGOUT_REDIRECT_URL;
                }
                return Observable.throw(err);
            }
        });
    }

}