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
import { Study, StudyDTO } from '../../studies/shared/study.model';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';
import { StudyCardService } from './study-card.service';

export class StudyCard extends Entity {

    id: number;
    name: string;
    study: Study;
    center: Center;
    acquisitionEquipment: AcquisitionEquipment;
    niftiConverter: NiftiConverter;

    service: StudyCardService = ServiceLocator.injector.get(StudyCardService);

    // Override
    public stringify() {
        return JSON.stringify(new StudyCardDTO(this), this.replacer);
    }
}

export class StudyCardDTO {

    id: number;
    name: string;
    study: StudyDTO;
    center: Center;
    acquisitionEquipment: AcquisitionEquipment;
    niftiConverter: NiftiConverter;

    constructor(studyCard: StudyCard) {
        this.id = studyCard.id;
        this.name = studyCard.name;
        this.study = new StudyDTO(studyCard.study);
        this.center = studyCard.center;
        this.acquisitionEquipment = studyCard.acquisitionEquipment;
        this.niftiConverter = studyCard.niftiConverter;
    }
}