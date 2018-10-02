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