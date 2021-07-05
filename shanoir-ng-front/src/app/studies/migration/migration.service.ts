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
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { BidsElement } from '../../bids/model/bidsElement.model';
import { DataUserAgreement } from '../../dua/shared/dua.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class MigrationService {

    API_URL = AppUtils.BACKEND_API_MIGRATION_URL;

    constructor(protected http: HttpClient, private keycloakService: KeycloakService) {

    }

    migrate(url: string, username: string, password: string, studyId: number, userId: number): Promise<any> {
        const endpoint = this.API_URL + "/migrate";

        let params = new HttpParams().append('shanoirUrl', url).append('username', username).append('userPassword', password).append('studyId', '' + studyId).append('userId', '' + userId);

        return this.http.get<any>(endpoint, {params: params}).toPromise();
    }

}