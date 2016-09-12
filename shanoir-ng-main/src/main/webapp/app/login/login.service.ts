import { Injectable } from '@angular/core';

@Injectable()
export class LoginService {
    authenticated = false;

    isAuthenticated(): boolean {
        return this.authenticated;
    }

    login(login: String, password: String): void {
        if (login != '' && password != '') {
            console.log("OK");
            this.authenticated = true;
        } else {
            console.log("NOK");
            this.authenticated = false;
        }
    }

    logout(): void {
        this.authenticated = false;
    }
}