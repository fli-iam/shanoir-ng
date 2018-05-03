import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';


import { SubjectPathology } from './subjectPathology.model';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { HandleErrorService } from '../../../../shared/utils/handle-error.service';

@Injectable()
export class SubjectPathologyService {

    constructor(private http: HttpClient, private handleErrorService: HandleErrorService) { }

    getSubjectPathologies(preclinicalSubject: PreclinicalSubject): Promise<SubjectPathology[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<SubjectPathology[]>(url)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting subject pathologies ', error);
                return Promise.reject(error.message || error);
            });
    }
    
    
    getSubjectPathology(preclinicalSubject: PreclinicalSubject, pid: string): Promise<SubjectPathology>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${pid}`;
        return this.http.get<SubjectPathology>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting SubjectPathology', error);
                        return Promise.reject(error.message || error);
         			});
    }
  


    update(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Observable<SubjectPathology> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${subjectPathology.id}`;
        return this.http
            .put<SubjectPathology>(url, JSON.stringify(subjectPathology))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }

    create(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Observable<SubjectPathology> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}`;
        return this.http
            .post<SubjectPathology>(url, JSON.stringify(subjectPathology))
            .map(res => res)
            .catch(this.handleErrorService.handleError);
    }

    delete(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Observable<void> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${subjectPathology.id}`;
        return this.http.delete(url)
            .map(res => res)
            .catch(this.handleErrorService.handleError);
    }
    
    deleteAllPathologiesForAnimalSubject(animalSubjectId: number): Observable<void> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.delete(url)
            .map(res => res)
            .catch(this.handleErrorService.handleError);
    }

    /*This method is to avoid unexpected error if returned object is null*/
    private extractData(res: Response) {
        let body;
        // check if empty, before call json
        if (res.text()) {
            body = res.json();
        }
        return body || {};
    }

}