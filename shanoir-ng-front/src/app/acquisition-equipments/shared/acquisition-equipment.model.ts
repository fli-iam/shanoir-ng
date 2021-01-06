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

import { Center } from '../../centers/shared/center.model';
import { ManufacturerModel } from './manufacturer-model.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { AcquisitionEquipmentService } from './acquisition-equipment.service';
import { ServiceLocator } from '../../utils/locator.service';

export class AcquisitionEquipment extends Entity {
    id: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;

    service: AcquisitionEquipmentService = ServiceLocator.injector.get(AcquisitionEquipmentService);
}