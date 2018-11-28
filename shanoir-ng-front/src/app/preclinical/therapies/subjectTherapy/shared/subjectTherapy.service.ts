import { Injectable } from '@angular/core';

import { SubjectTherapy } from './subjectTherapy.model';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class SubjectTherapyService extends EntityService<SubjectTherapy>{
    API_URL = PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL;

    getEntityInstance() { return new SubjectTherapy(); }

    getSubjectTherapies(preclinicalSubject: PreclinicalSubject): Promise<SubjectTherapy[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<SubjectTherapy[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))    
            .toPromise();
    }
    
    getSubjectTherapy(preclinicalSubject: PreclinicalSubject, tid: string): Promise<SubjectTherapy>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
            return this.http.get<SubjectTherapy>(url)
                .map((entity) => this.toRealObject(entity))        
                .toPromise();
    }

    updateSubjectTherapy(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return this.http
            .put<SubjectTherapy>(url, JSON.stringify(subjectTherapy))
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }

    createSubjectTherapy(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}`;
        return this.http
            .post<SubjectTherapy>(url, JSON.stringify(subjectTherapy))
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }
    
    deleteSubjectTherapy(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<void> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return this.http.delete<void>(url)
            .toPromise()
    }
    
    deleteAllTherapiesForAnimalSubject(animalSubjectId: number): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.delete(url)
            .map(res => res).toPromise();
    }

     getAllSubjectForTherapy(tid: number): Promise<SubjectTherapy[]> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
    	return this.http.get<SubjectTherapy[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))            
            .toPromise();
    }

}