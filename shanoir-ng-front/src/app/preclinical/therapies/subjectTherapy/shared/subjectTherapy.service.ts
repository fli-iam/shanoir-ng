import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { SubjectTherapy } from './subjectTherapy.model';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';


import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class SubjectTherapyService {

    constructor(private http: HttpClient) { }

    getSubjectTherapies(preclinicalSubject: PreclinicalSubject): Promise<SubjectTherapy[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<SubjectTherapy[]>(url)
            .toPromise();
    }
    
    getSubjectTherapy(preclinicalSubject: PreclinicalSubject, tid: string): Promise<SubjectTherapy>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
            return this.http.get<SubjectTherapy>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting SubjectTherapy', error);
                        return Promise.reject(error.message || error);
            });
        }

    update(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Observable<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        return this.http
            .put<SubjectTherapy>(url, JSON.stringify(subjectTherapy))
            .map(response => response);
    }

    create(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Observable<SubjectTherapy> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}`;
        return this.http
            .post<SubjectTherapy>(url, JSON.stringify(subjectTherapy))
            .map(res => res);
    }
    
    delete(preclinicalSubject: PreclinicalSubject, subjectTherapy: SubjectTherapy): Promise<void> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${subjectTherapy.id}`;
        	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete SubjectTherapy', error);
                	return Promise.reject(error);
            	});
    }
    
    deleteAllTherapiesForAnimalSubject(animalSubjectId: number): Observable<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_THERAPY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.delete(url)
            .map(res => res);
    }

     getAllSubjectForTherapy(tid: number): Promise<SubjectTherapy[]> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_THERAPY}/${tid}`;
    	return this.http.get<SubjectTherapy[]>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting SubjectTherapy for a Therapy', error);
                        return Promise.reject(error.message || error);
         			});
    }

}