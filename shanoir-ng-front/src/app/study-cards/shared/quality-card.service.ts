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

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';

import { QualityCardDTOService } from './quality-card.dto';
import { QualityCardDTO } from './quality-card.dto.model';
import { QualityCard } from './quality-card.model';

export class Interval {
    constructor(public from: number, public to: number) {}
};

@Injectable()
export class QualityCardService extends EntityService<QualityCard> {

    API_URL = AppUtils.BACKEND_API_QUALITY_CARD_URL;

    private qualityCardDTOService: QualityCardDTOService = inject(QualityCardDTOService);
    
    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new QualityCard(); }

    getAllForStudy(studyId: number): Promise<QualityCard[]> {
        return firstValueFrom(this.http.get<any[]>(this.API_URL + '/byStudy/' + studyId))
            .then(this.mapEntityList);
    }

    protected mapEntity = (dto: QualityCardDTO, result?: QualityCard): Promise<QualityCard> => {
        if (result == undefined) result = this.getEntityInstance();
        return this.qualityCardDTOService.toEntity(dto, result);
    }

    protected mapEntityList = (dtos: QualityCardDTO[], result?: QualityCard[]): Promise<QualityCard[]> => {
        if (result == undefined) result = [];
        if (dtos) return this.qualityCardDTOService.toEntityList(dtos, result);
    }

    public stringify(entity: QualityCard) {
        const dto = new QualityCardDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }

    applyOnStudy(qualityCardId: number): Promise<any> {
        return firstValueFrom(this.http.get<any[]>(this.API_URL + '/apply/' + qualityCardId));
    }

    testOnStudy(qualityCardId: number, start?: number, stop?: number): Promise<any> {
        return firstValueFrom(this.http.get<any[]>(this.API_URL + '/test/' + qualityCardId 
            + (start != null && start != undefined && stop != null && stop != undefined
                ? '/' + (start - 1) + '/' + stop : '')));
    }

    testOnExamination(qualityCardId: number, examinationId: number): Promise<any> {
        return firstValueFrom(this.http.get<any[]>(this.API_URL + '/test/' + qualityCardId + '/exam/' + examinationId));
    }

}
