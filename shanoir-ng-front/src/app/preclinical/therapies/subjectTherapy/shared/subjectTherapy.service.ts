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

import { AnimalSubject } from '../../../animalSubject/shared/animalSubject.model';
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

    getSubjectTherapies(animalSubject: AnimalSubject): Promise<SubjectTherapy[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<SubjectTherapy[]>(url)
            .toPromise()
            .then(entities => entities?.map((entity) => this.toRealObject(entity)) || []);
    }
    
    getSubjectTherapy(animalSubject: AnimalSubject, tid: string): Promise<SubjectTherapy>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
            return this.http.get<SubjectTherapy>(url)
                .toPromise()
                .then((entity) => this.toRealObject(entity));
    }

    updateSubjectTherapy(animalSubject: AnimalSubject, subjectTherapy: SubjectTherapy): Promise<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return this.http
            .put<SubjectTherapy>(url, this.stringify(subjectTherapy))
            .toPromise()
            .then((entity) => entity? this.toRealObject(entity) : entity);
    }

    createSubjectTherapy(animalSubject: AnimalSubject, subjectTherapy: SubjectTherapy): Promise<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}`;
        return this.http
            .post<SubjectTherapy>(url, JSON.stringify(subjectTherapy))
            .toPromise()
            .then((entity) => this.toRealObject(entity));
    }
    
    deleteSubjectTherapy(animalSubject: AnimalSubject, subjectTherapy: SubjectTherapy): Promise<void> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return this.http.delete<void>(url)
            .toPromise()
    }
    
    deleteAllTherapiesForAnimalSubject(animalSubjectId: number): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.delete(url)
            .toPromise();
    }

     getAllSubjectForTherapy(tid: number): Promise<SubjectTherapy[]> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
    	return this.http.get<SubjectTherapy[]>(url)
            .toPromise()
            .then(entities => entities?.map((entity) => this.toRealObject(entity)) || []);
    }

}