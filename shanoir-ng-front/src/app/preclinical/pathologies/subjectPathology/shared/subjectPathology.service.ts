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
import { SubjectPathology } from './subjectPathology.model';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class SubjectPathologyService extends EntityService<SubjectPathology>{

    API_URL = PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }
    
    getEntityInstance() { return new SubjectPathology(); }

    getSubjectPathologies(preclinicalSubject: PreclinicalSubject): Promise<SubjectPathology[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<SubjectPathology[]>(url)
            .toPromise()
            .then(entities => entities.map((entity) => this.toRealObject(entity)));
    }
    
    
    getSubjectPathology(preclinicalSubject: PreclinicalSubject, pid: string): Promise<SubjectPathology>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${pid}`;
        return this.http.get<SubjectPathology>(url)
            .toPromise()
            .then((entity) => this.toRealObject(entity));
    }
  


    updateSubjectPathology(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Promise<SubjectPathology> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${subjectPathology.id}`;
        return this.http
            .put<SubjectPathology>(url, this.stringify(subjectPathology))
            .toPromise()
            .then((entity) => entity? this.toRealObject(entity) : entity);
    }

    createSubjectPathology(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Promise<SubjectPathology> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}`;
        return this.http
            .post<SubjectPathology>(url, JSON.stringify(subjectPathology))
            .toPromise()
            .then((entity) => this.toRealObject(entity));
    }

    deleteSubjectPathology(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${subjectPathology.id}`;
        return this.http.delete<void>(url)
            .toPromise();
    }
    
    deleteAllPathologiesForAnimalSubject(animalSubjectId: number): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.delete(url)
            .toPromise()
            .then(res => res);
    }

    getAllSubjectForPathologyModel(pid: number): Promise<SubjectPathology[]> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/model/${pid}/`;
    	return this.http.get<SubjectPathology[]>(url)
            .toPromise()
            .then(entities => entities.map((entity) => this.toRealObject(entity)));      
    }

}