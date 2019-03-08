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
import { IdNameObject } from '../../shared/models/id-name-object.model';
import * as AppUtils from '../../utils/app.utils';
import { ManufacturerModel } from './manufacturer-model.model';

@Injectable()
export class ManufacturerModelService extends EntityService<ManufacturerModel> {
    
    API_URL = AppUtils.BACKEND_API_MANUF_MODEL_URL;

    getEntityInstance() { return new ManufacturerModel(); }

    getManufacturerModelsNames(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_MANUF_MODEL_NAMES_URL)
            .toPromise();
    }

    getCenterManufacturerModelsNames(centerId:Number): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_CENTER_MANUF_MODEL_NAMES_URL+ '/' + centerId)
            .toPromise();
    }
}