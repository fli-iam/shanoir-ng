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
import { Injectable } from '@angular/core';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import { StudyCardDTO, StudyCardDTOService } from './study-card.dto';
import { StudyCard } from './study-card.model';


@Injectable()
export class StudyCardService extends EntityService<StudyCard> {

    API_URL = AppUtils.BACKEND_API_STUDY_CARD_URL;

    private studyCardDTOService: StudyCardDTOService = ServiceLocator.injector.get(StudyCardDTOService);
    
    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new StudyCard(); }

    getAllForStudy(studyId: number): Promise<StudyCard[]> {
        return this.http.get<any[]>(this.API_URL + '/byStudy/' + studyId)
            .toPromise()
            .then(this.mapEntityList);
    }

    protected mapEntity = (dto: StudyCardDTO, result?: StudyCard): Promise<StudyCard> => {
        if (result == undefined) result = this.getEntityInstance();
        return this.studyCardDTOService.toEntity(dto, result);
    }

    protected mapEntityList = (dtos: StudyCardDTO[], result?: StudyCard[]): Promise<StudyCard[]> => {
        if (result == undefined) result = [];
        if (dtos) return this.studyCardDTOService.toEntityList(dtos, result);
    }

    public stringify(entity: StudyCard) {
        let dto = new StudyCardDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}