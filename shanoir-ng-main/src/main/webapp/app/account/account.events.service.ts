import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {Account} from './account';

@Injectable()
export class AccountEventsService extends Subject<any> {
    constructor() {
        super();
    }
    
    loginSuccess(account:any) {
        console.log("loginSuccess");
        if(account) {
            account.authenticated = true;
            console.log("account.authenticated = true");
            super.next(account);
        }
    }
    
    logout(account:any) {
        console.log("logout - account: " + account);
        if(account) {
            account.authenticated = false;
            console.log("account.authenticated = false");
            super.next(account);
        }
    }

}
