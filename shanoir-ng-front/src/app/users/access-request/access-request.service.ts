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
import { Subscription } from 'rxjs';

import { IdName } from 'src/app/shared/models/id-name.model';
import { KeycloakService } from 'src/app/shared/keycloak/keycloak.service';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';

import { AccessRequest } from './access-request.model';

@Injectable()
export class AccessRequestService extends EntityService<AccessRequest> implements OnDestroy {

    getEntityInstance(entity?: AccessRequest): AccessRequest {
        return new AccessRequest();
    }

    API_URL = AppUtils.BACKEND_API_USER_ACCESS_REQUEST;

    subscribtions: Subscription[] = [];
    
    constructor(protected http: HttpClient) {
        super(http);
    }

    public inviteUser(mail: string, func: string, study: IdName): Promise<AccessRequest> {
        const formData: FormData = new FormData();
        formData.set("email", mail);
        formData.set("studyId", "" + study.id);
        formData.set("studyName", study.name);
        formData.set("issuer", KeycloakService.auth.authz.tokenParsed.name);
        formData.set("role", func);
        return this.http.put(this.API_URL + "/invitation/", formData).toPromise()
            .then(response =>
            {
                if (response){
                    return this.mapEntity(response);
                }
                return null;
            });

    }

    public findByStudy(studyId: number): Promise<AccessRequest[]> {
        return this.http.get<AccessRequest[]>(this.API_URL+"/byStudy/" + studyId).toPromise();
    }

    public resolveRequest(id: number, value: boolean): Promise<any> {
        return this.http.put(AppUtils.BACKEND_API_ACCESS_REQUEST_RESOLVE + id, "" + value).toPromise();
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}