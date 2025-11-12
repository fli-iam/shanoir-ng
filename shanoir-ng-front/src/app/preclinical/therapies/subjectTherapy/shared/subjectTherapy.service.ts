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
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

import { SubjectTherapy } from './subjectTherapy.model';


@Injectable()
export class SubjectTherapyService extends EntityService<SubjectTherapy>{
    API_URL = PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }
    
    getEntityInstance() { return new SubjectTherapy(); }

    getSubjectTherapies(preclinicalSubject: PreclinicalSubject): Promise<SubjectTherapy[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return firstValueFrom(this.http.get<SubjectTherapy[]>(url))
            .then(entities => entities?.map((entity) => this.toRealObject(entity)) || []);
    }
    
    getSubjectTherapy(preclinicalSubject: PreclinicalSubject, tid: string): Promise<SubjectTherapy>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
            return firstValueFrom(this.http.get<SubjectTherapy>(url))
                .then((entity) => this.toRealObject(entity));
    }

    updateSubjectTherapy(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return firstValueFrom(this.http
            .put<SubjectTherapy>(url, this.stringify(subjectTherapy)))
            .then((entity) => entity? this.toRealObject(entity) : entity);
    }

    createSubjectTherapy(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}`;
        return firstValueFrom(this.http
            .post<SubjectTherapy>(url, JSON.stringify(subjectTherapy)))
            .then((entity) => this.toRealObject(entity));
    }
    
    deleteSubjectTherapy(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<void> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return firstValueFrom(this.http.delete<void>(url))
    }
    
    deleteAllTherapiesForAnimalSubject(animalSubjectId: number): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return firstValueFrom(this.http.delete(url));
    }

     getAllSubjectForTherapy(tid: number): Promise<SubjectTherapy[]> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
    	return firstValueFrom(this.http.get<SubjectTherapy[]>(url))
            .then(entities => entities?.map((entity) => this.toRealObject(entity)) || []);
    }

}