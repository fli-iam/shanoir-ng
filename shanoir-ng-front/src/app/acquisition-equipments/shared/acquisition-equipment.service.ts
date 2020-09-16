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
import * as AppUtils from '../../utils/app.utils';
import { AcquisitionEquipment } from './acquisition-equipment.model';

import { HttpClient } from '@angular/common/http';

@Injectable()
export class AcquisitionEquipmentService extends EntityService<AcquisitionEquipment> {

    API_URL = AppUtils.BACKEND_API_ACQ_EQUIP_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new AcquisitionEquipment(); }

    getAllByCenter(centerId: number): Promise<AcquisitionEquipment[]> {
        return this.http.get<AcquisitionEquipment[]>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/byCenter/' + centerId)
            .toPromise()
            .then(this.mapEntityList);
    }

    getAllByStudy(studyId: number): Promise<AcquisitionEquipment[]> {
        return this.http.get<AcquisitionEquipment[]>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/byStudy/' + studyId)
            .toPromise()
            .then(this.mapEntityList);
    }
}