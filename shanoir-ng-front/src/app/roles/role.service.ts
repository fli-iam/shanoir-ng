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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Role } from './role.model';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class RoleService {

    constructor(private http: HttpClient) { }

    getRoles(): Promise<Role[]> {
        return this.http.get<Role[]>(AppUtils.BACKEND_API_ROLE_ALL_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting roles', error);
                return Promise.reject(error.message || error);
            });
    }
}