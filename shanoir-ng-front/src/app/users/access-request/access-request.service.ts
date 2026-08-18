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
import { HttpClient } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { Subject, Subscription, firstValueFrom } from 'rxjs';

import { IdName } from 'src/app/shared/models/id-name.model';
import { KeycloakService } from 'src/app/shared/keycloak/keycloak.service';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { User } from '../shared/user.model';
import { AccountRequestInfo } from '../account-request-info/account-request-info.model';

import { AccessRequest } from './access-request.model';

@Injectable()
export class AccessRequestService extends EntityService<AccessRequest> implements OnDestroy {


    getEntityInstance(): AccessRequest {
        const accessRequest: AccessRequest = new AccessRequest();
        accessRequest.user = new User();
        accessRequest.user.accountRequestInfo = new AccountRequestInfo();
        return accessRequest;
    }

    API_URL = AppUtils.BACKEND_API_USER_ACCESS_REQUEST;

    public accessRequets: Subject<number> = new Subject();
    private _accessRequests: number = 0;
    private refreshTimeout;
    subscribtions: Subscription[] = [];


    constructor(protected http: HttpClient) {
        super(http);
        this.refreshTimeout = setInterval(() => {
            this.getAccessRequestsForAdmin();
        }, 1000 * 60 * 2);
    }

    decreaseAccessRequests() {
        this._accessRequests --;
        this.accessRequets.next(this._accessRequests);
    }

    public inviteUser(mail: string, func: string, study: IdName): Promise<AccessRequest> {
        const formData: FormData = new FormData();
        formData.set("email", mail);
        formData.set("studyId", "" + study.id);
        formData.set("studyName", study.name);
        formData.set("issuer", KeycloakService.auth.authz.tokenParsed.name);
        formData.set("role", func);
        return firstValueFrom(this.http.put(this.API_URL + "/invitation/", formData))
            .then(response =>
            {
                if (response){
                    return this.mapEntity(response);
                }
                return null;
            });

    }

    getAccessRequests(): Promise<AccessRequest[]> {
        return firstValueFrom(this.http.get<AccessRequest[]>(AppUtils.BACKEND_API_USER_ACCESS_REQUEST_BY_USER))
            .then((typeResult: AccessRequest[]) => {
                return typeResult;
            });
    }

    openAccessExtensionModal(currentExpirationDate: Date | undefined, study: { id: number, name: string }, extensionDate?: Date): Promise<any> {
        return this.getAccessRequests().then(accessRequests => {
            const hasAlreadyAsk: boolean = !!accessRequests?.find(ar => ar.studyId == study.id && ar.status == 2);
            if (!hasAlreadyAsk) {
                const expired: boolean = currentExpirationDate ? currentExpirationDate.getTime() < new Date().getTime() : false;
                this.askForExtendDate(expired, study, extensionDate);
            } else {
                this.confirmDialogService.inform('Access request pending', 'You already have asked an access extension for this study, wait for the administrator to confirm your access.');
            }
        });
    }

    private askForExtendDate(expired: boolean, study: { id: number, name: string }, extensionDate: Date = new Date(new Date().getTime() + 6 * 30 * 24 * 60 * 60 * 1000)) {
        this.confirmDialogService.extendDate(
            expired ? 'Access expired' : 'Access extension request',
            expired ? `Your access to this study (${study.name}) has expired, do you want ask the administrator to extend it ?` :
                      `Do you want to ask the administrator to extend your access to this study (${study.name})?`,
            'Ask for extension',
            extensionDate)
            .then(result => {
                if (result.response) {
                    this.requestAccessExtension(study.id, result.date).then(() => {
                        this.confirmDialogService.inform('Access request sent', 'Your access request has been sent to the administrator.');
                    });
                }
            });
    }

    getAccessRequestsForAdmin(): Promise<AccessRequest[]> {
        return firstValueFrom(this.http.get<AccessRequest[]>(AppUtils.BACKEND_API_USER_ACCESS_REQUEST_BY_ADMIN))
            .then((typeResult: AccessRequest[]) => {
                this._accessRequests = typeResult?.length;
                this.accessRequets.next(typeResult?.length);
                return typeResult;
            }).then((typeResult: AccessRequest[]) => {
                return this.mapEntityList(typeResult);
            });
    }

    public findByStudy(studyId: number): Promise<AccessRequest[]> {
        return firstValueFrom(this.http.get<AccessRequest[]>(this.API_URL+"/byStudy/" + studyId))
            .then(this.mapEntityList);
    }

    public resolveRequest(id: number, value: boolean, expiration: Date | null): Promise<any> {
        return firstValueFrom(this.http.put(AppUtils.BACKEND_API_ACCESS_REQUEST_RESOLVE + id, ""
            + JSON.stringify({accept: value, expiration: expiration})));
    }

    public requestAccessExtension(studyId: number, expiration: Date): Promise<any> {
        const endpoint = AppUtils.BACKEND_API_ACCESS_REQUEST_EXTENSION;
        const formData: {studyId: number, extensionDate: string} = {studyId: studyId, extensionDate: EntityService.datePattern(expiration)};
        return firstValueFrom(this.http.post(endpoint, null, {
            params: formData
        }));
    }

    protected toRealObject(entity: any): AccessRequest {
        const trueObject: AccessRequest = super.toRealObject(entity);
        trueObject.expirationDate = entity.expirationDate ? new Date(entity.expirationDate) : null;
        return trueObject;
    }

    ngOnDestroy() {
        for(const subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
        clearInterval(this.refreshTimeout);
    }
}
