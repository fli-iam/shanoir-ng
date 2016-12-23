import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { LoginService } from 'app/shared/login/login.service';

@Injectable()
export class AuthAdminGuard implements CanActivate {
    constructor(private loginService: LoginService) { }

    canActivate() {
        return Observable.of(this.loginService.isUserAdmin());
    }

}