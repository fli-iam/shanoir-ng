import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { Manufacturer } from './manufacturer.model';

@Injectable()
export class ManufacturerService extends EntityService<Manufacturer> {
    
    API_URL = AppUtils.BACKEND_API_MANUF_URL;

    getEntityInstance() { return new Manufacturer(); }
}