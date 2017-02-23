import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class AuthAdminGuard implements CanActivate {
    constructor() { }

    canActivate() {
        return Observable.of(true);
        //return Observable.of(this.loginService.isUserAdmin());
    }

}