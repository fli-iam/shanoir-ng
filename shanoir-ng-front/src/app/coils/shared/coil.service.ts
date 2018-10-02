import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';
import { Coil } from './coil.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';

@Injectable()
export class CoilService extends EntityService<Coil> {

    API_URL = AppUtils.BACKEND_API_COIL_URL;

    getEntityInstance() { return new Coil(); }
}