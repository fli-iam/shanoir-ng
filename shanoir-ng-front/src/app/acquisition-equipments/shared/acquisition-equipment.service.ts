import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { AcquisitionEquipment } from './acquisition-equipment.model';

@Injectable()
export class AcquisitionEquipmentService extends EntityService<AcquisitionEquipment> {

    API_URL = AppUtils.BACKEND_API_ACQ_EQUIP_URL ;

    getEntityInstance() { return new AcquisitionEquipment(); }
}