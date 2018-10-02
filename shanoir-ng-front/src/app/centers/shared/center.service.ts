import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import * as AppUtils from '../../utils/app.utils';
import { Center } from './center.model';

@Injectable()
export class CenterService extends EntityService<Center> {

    API_URL = AppUtils.BACKEND_API_CENTER_URL;

    getEntityInstance() { return new Center(); }

    getCentersNames(): Promise<Center[]> {
        return this.http.get<Center[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    getCentersNamesForExamination(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }
}