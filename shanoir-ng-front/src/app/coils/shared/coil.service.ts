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

import * as AppUtils from '../../utils/app.utils';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';

import { Coil, CoilDTO } from './coil.model';

@Injectable()
export class CoilService extends EntityService<Coil> {

    API_URL = AppUtils.BACKEND_API_COIL_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new Coil(); }

    findByCenter(centerId: number): Promise<Coil[]> {
        return this.http.get<any>(this.API_URL + '/byCenter/' + centerId)
            .toPromise()
            .then(list => this.mapEntityList(list, null));
    }

    public stringify(entity: Coil) {
        let dto = new CoilDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}