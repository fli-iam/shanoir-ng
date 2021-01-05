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
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import { ExaminationAnesthetic } from './examinationAnesthetic.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ExaminationAnestheticService extends EntityService<ExaminationAnesthetic>{
    API_URL = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL;   
    
    constructor(protected http: HttpClient) {
        super(http)
    }
    
    getEntityInstance() { return new ExaminationAnesthetic(); }      
    
    getExaminationAnesthetics(examination_id:number): Promise<ExaminationAnesthetic[]>{
        const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<ExaminationAnesthetic[]>(url)
        .toPromise()
        .then(entities => entities.map((entity) => this.toRealObject(entity)));
    }

    
    
    getExaminationAnesthetic(examination_id:number,eaid: number): Promise<ExaminationAnesthetic> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${eaid}`;
        return this.http.get<ExaminationAnesthetic>(url)
        .toPromise()
        .then((entity) => this.toRealObject(entity));
    }
    
    getAllExaminationForAnesthetic(aid: number): Promise<ExaminationAnesthetic[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${aid}`;
        return this.http.get<ExaminationAnesthetic[]>(url)
                .toPromise()
                .then(response => response)
                .catch((error) => {
                    console.error('Error while getting ExaminationAnesthetic for an Anesthetic', error);
                    return Promise.reject(error.message || error);
                });
    }
    
    updateAnesthetic(examination_id:number, examAnesthetic: ExaminationAnesthetic): Promise<ExaminationAnesthetic> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${examAnesthetic.internal_id}`;
        return this.http
            .put<ExaminationAnesthetic>(url, JSON.stringify(examAnesthetic)) 
            .toPromise();
        }
    
    createAnesthetic(examination_id:number, examAnesthetic: ExaminationAnesthetic): Promise<ExaminationAnesthetic> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}`;
            return this.http
            .post<ExaminationAnesthetic>(url, JSON.stringify(examAnesthetic))
            .toPromise();
        }

    deleteAnesthetic(examAnesthetic: ExaminationAnesthetic): Promise<void> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examAnesthetic.examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${examAnesthetic.id}`;
        return this.http.delete<void>(url)
            .toPromise()
    }
    
}