import { Injectable } from '@angular/core';
import { ErrorObservable } from 'rxjs/observable/ErrorObservable';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { ExtensionRequestInfo } from '../extension-request/extension-request-info.model';
import { User } from './user.model';

@Injectable()
export class UserService extends EntityService<User>{

    API_URL = AppUtils.BACKEND_API_USER_URL;

    getEntityInstance() { return new User(); }

    confirmAccountRequest(id: number, user: User): Promise<User> {
        return this.http.put<User>(this.API_URL + '/' + id + AppUtils.BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL, JSON.stringify(user))
            .toPromise();
    }

    denyAccountRequest(id: number): Promise<void> {
        return this.http.delete<void>(this.API_URL + '/' + id + AppUtils.BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL)
            .toPromise();
    }

    requestAccount(user: User): Promise<User> {
        return this.http.post<User>(AppUtils.BACKEND_API_USER_ACCOUNT_REQUEST_URL, JSON.stringify(user)).toPromise();
    }

    requestExtension(extensionRequestInfo: ExtensionRequestInfo): Promise<void | ErrorObservable> {
        return this.http.put<void>(AppUtils.BACKEND_API_USER_EXTENSION_REQUEST_URL, JSON.stringify(extensionRequestInfo))
            .toPromise();
    }
}