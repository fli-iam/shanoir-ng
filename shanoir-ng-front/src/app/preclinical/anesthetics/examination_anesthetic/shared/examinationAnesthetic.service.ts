import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { ExaminationAnesthetic } from './examinationAnesthetic.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class ExaminationAnestheticService {
       
        constructor(private http: HttpClient) { }        
               
        getExaminationAnesthetics(examination_id:number): Promise<ExaminationAnesthetic[]>{
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
            return this.http.get<ExaminationAnesthetic[]>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting examination anesthetics ', error);
                        return Promise.reject(error.message || error);
            });
        }
 
        getExaminationAnesthetic(examination_id:number,eaid: number): Promise<ExaminationAnesthetic> {
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${eaid}`;
            return this.http.get<ExaminationAnesthetic>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting examination anesthetic ', error);
                        return Promise.reject(error.message || error);
            });
        }
       
        update(examination_id:number, examAnesthetic: ExaminationAnesthetic): Observable<ExaminationAnesthetic> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${examAnesthetic.id}`;
          return this.http
            .put<ExaminationAnesthetic>(url, JSON.stringify(examAnesthetic))
            .map(response => response);
        }
    
        create(examination_id:number, examAnesthetic: ExaminationAnesthetic): Observable<ExaminationAnesthetic> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}`;
            return this.http
            .post<ExaminationAnesthetic>(url, JSON.stringify(examAnesthetic))
            .map(res => res);
        }
        
        delete(examAnesthetic: ExaminationAnesthetic): Promise<void> {
        	const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examAnesthetic.examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${examAnesthetic.id}`;
          	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete ExaminationAnesthetic', error);
                	return Promise.reject(error);
            	});
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