import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import { ExaminationAnesthetic } from './examinationAnesthetic.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class ExaminationAnestheticService extends EntityService<ExaminationAnesthetic>{
    API_URL = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL;   
    getEntityInstance() { return new ExaminationAnesthetic(); }      
        
        getExaminationAnesthetics(examination_id:number): Promise<ExaminationAnesthetic[]>{
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
            return this.http.get<ExaminationAnesthetic[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))
            .toPromise()
        }
 
        
        
        getExaminationAnesthetic(examination_id:number,eaid: number): Promise<ExaminationAnesthetic> {
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${eaid}`;
            return this.http.get<ExaminationAnesthetic>(url)
            .map((entity) => this.toRealObject(entity))
            .toPromise();
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
        
        updateAnesthetic(examination_id:number, examAnesthetic: ExaminationAnesthetic): Observable<ExaminationAnesthetic> {
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${examAnesthetic.id}`;
            return this.http
              .put<ExaminationAnesthetic>(url, JSON.stringify(examAnesthetic))
              .map(response => response);
          }
      
        createAnesthetic(examination_id:number, examAnesthetic: ExaminationAnesthetic): Observable<ExaminationAnesthetic> {
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}`;
              return this.http
              .post<ExaminationAnesthetic>(url, JSON.stringify(examAnesthetic))
              .map(res => res);
          }

        deleteAnesthetic(examAnesthetic: ExaminationAnesthetic): Promise<void> {
        	const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examAnesthetic.examination_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC}/${examAnesthetic.id}`;
          	return this.http.delete<void>(url)
            	.toPromise()
    	}
    
}