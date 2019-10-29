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
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Study } from '../../studies/shared/study.model';
import { StudyCardDTO } from './study-card.dto'
import { ServiceLocator } from '../../utils/locator.service';
import { StudyCardService } from './study-card.service';


export class StudyCard extends Entity {

    id: number;
    name: string;
    study: Study;
    acquisitionEquipment: AcquisitionEquipment;
    niftiConverter: NiftiConverter;

    service: StudyCardService = ServiceLocator.injector.get(StudyCardService);

    // Override
    public stringify() {
        return JSON.stringify(new StudyCardDTO(this), this.replacer);
    }
}