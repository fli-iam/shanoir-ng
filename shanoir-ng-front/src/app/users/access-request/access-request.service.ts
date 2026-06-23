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

import { AccessRequest } from './access-request.model';

@Injectable()
export class AccessRequestService extends EntityService<AccessRequest> implements OnDestroy {

    
    getEntityInstance(): AccessRequest {
        return new AccessRequest();
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

    getAccessRequestsForAdmin(): Promise<AccessRequest[]> {
        return firstValueFrom(this.http.get<AccessRequest[]>(AppUtils.BACKEND_API_USER_ACCESS_REQUEST_BY_ADMIN))
            .then((typeResult: AccessRequest[]) => {
                this._accessRequests = typeResult?.length;
                this.accessRequets.next(typeResult?.length);
                return typeResult;
            }).then((typeResult: AccessRequest[]) => this.mapEntityList(typeResult));
    }

    public findByStudy(studyId: number): Promise<AccessRequest[]> {
        return firstValueFrom(this.http.get<AccessRequest[]>(this.API_URL+"/byStudy/" + studyId))
            .then(this.mapEntityList);
    }

    public resolveRequest(id: number, value: boolean, expiration: Date | null): Promise<any> {
        return firstValueFrom(this.http.put(AppUtils.BACKEND_API_ACCESS_REQUEST_RESOLVE + id, "" 
            + JSON.stringify({response: value, expiration: expiration})));
    }

    protected toRealObject(entity: any): AccessRequest {
        const trueObject: AccessRequest = super.toRealObject(entity);
        trueObject.expiration = entity.expiration ? new Date(entity.expiration) : null;
        return trueObject;
    }

    ngOnDestroy() {
        for(const subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
        clearInterval(this.refreshTimeout);
    }
}