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
import { Field } from 'src/app/shared/reflect/field.decorator';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { StudyCenter } from '../../studies/shared/study-center.model';


export class Center extends Entity {
    @Field() id: number;
    @Field() acquisitionEquipments: AcquisitionEquipment[];
    @Field() city: string;
    @Field() country: string;
    @Field() name: string;
    @Field() phoneNumber: string;
    @Field() postalCode: string;
    @Field() street: string;
    @Field() website: string;
    @Field() studyCenterList: StudyCenter[] = [];
}