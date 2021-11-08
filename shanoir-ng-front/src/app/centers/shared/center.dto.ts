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

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { StudyCenter, StudyCenterDTO } from '../../studies/shared/study-center.model';
import { Study } from '../../studies/shared/study.model';
import { Center } from './center.model';

@Injectable()
export class CenterDTOService {

    constructor() {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: CenterDTO, result?: Center): Promise<Center> {        
        if (!result) result = new Center();
        CenterDTOService.mapSyncFields(dto, result);
        return Promise.resolve(result);
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: CenterDTO[], result?: Center[]): Promise<Center[]>{
        if (!result) result = [];
        if (dtos) {
            if (dtos) {
                for (let dto of dtos) {
                    let entity = new Center();
                    CenterDTOService.mapSyncFields(dto, entity);
                    result.push(entity);
                }
            }
        }
        return Promise.resolve(result);
    }

    static mapSyncFields(dto: CenterDTO, entity: Center): Center {
        entity.id = dto.id;
        entity.name = dto.name;
        entity.acquisitionEquipments = dto.acquisitionEquipments;
        entity.city = dto.city;
        entity.country = dto.country;
        entity.phoneNumber = dto.phoneNumber;
        entity.postalCode = dto.postalCode;
        entity.street = dto.street;
        entity.website = dto.website;

        entity.studyCenterList = [];
        for (let scDto of dto.studyCenterList) {
            let studyCenter: StudyCenter = new StudyCenter();
            studyCenter.id = scDto.id;
            studyCenter.center = entity;
            //studyCenter.center.id = dto.id;
            studyCenter.study = new Study();
            studyCenter.study.id = scDto.study.id;
            entity.studyCenterList.push(studyCenter);
        }
        return entity;
    }
}


export class CenterDTO {

    acquisitionEquipments: AcquisitionEquipment[];
    city: string;
    country: string;
    id: number;
    name: string;
    phoneNumber: string;
    postalCode: string;
    street: string;
    website: string;
    studyCenterList: StudyCenterDTO[] = [];

    constructor(center: Center) {
        this.acquisitionEquipments = center.acquisitionEquipments;
        this.city = center.city;
        this.country = center.country;
        this.id = center.id;
        this.name = center.name;
        this.phoneNumber = center.phoneNumber;
        this.postalCode = center.postalCode;
        this.street = center.street;
        this.website = center.website;
        this.studyCenterList = [];
        for (let sc of center.studyCenterList) {
            this.studyCenterList.push(new StudyCenterDTO(sc));
        }
    }

}