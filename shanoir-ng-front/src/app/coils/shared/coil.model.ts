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

import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { Center } from '../../centers/shared/center.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Id } from '../../shared/models/id.model';
import { ServiceLocator } from '../../utils/locator.service';
import { CoilType } from './coil-type.enum';
import { CoilService } from './coil.service';

export class Coil extends Entity {

    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;
    coilType:CoilType;

    service: CoilService = ServiceLocator.injector.get(CoilService);

    // Override
    public stringify() {
        return JSON.stringify(new CoilDTO(this), this.replacer);
    }
}

export class CoilDTO {

    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: Id;
    manufacturerModel: Id;
    coilType: CoilType;

    constructor(coil: Coil) {
        this.id = coil.id;
        this.name = coil.name;
        this.numberOfChannels = coil.numberOfChannels;
        this.serialNumber = coil.serialNumber;
        this.center = new Id(coil.center.id);
        this.manufacturerModel = new Id(coil.manufacturerModel.id);
        this.coilType = coil.coilType;
    }
}