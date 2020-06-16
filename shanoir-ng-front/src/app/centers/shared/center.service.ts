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

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import * as AppUtils from '../../utils/app.utils';
import { Center } from './center.model';


@Injectable()
export class CenterService extends EntityService<Center> {

    API_URL = AppUtils.BACKEND_API_CENTER_URL;

    getEntityInstance() { return new Center(); }

    getCentersNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    getCentersNamesByStudyId(studyId: number): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL + "/" + studyId)
            .toPromise();
    }
}