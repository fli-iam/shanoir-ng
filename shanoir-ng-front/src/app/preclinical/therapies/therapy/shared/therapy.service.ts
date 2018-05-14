import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';


import { Therapy } from './therapy.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class TherapyService {
         
        constructor(private http: HttpClient) { }    
           
    
        getTherapies(): Promise<Therapy[]>{
            return this.http.get<Therapy[]>(PreclinicalUtils.PRECLINICAL_API_THERAPIES_ALL_URL)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting therapies', error);
                        return Promise.reject(error.message || error);
            });
        }
        getTherapy(id: string): Promise<Therapy>{
            return this.http.get<Therapy>(PreclinicalUtils.PRECLINICAL_API_THERAPIES_URL+"/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting Therapy', error);
                        return Promise.reject(error.message || error);
            });
        }
     
        update(therapy: Therapy): Observable<Therapy> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_THERAPIES_URL}/`+therapy.id;
          return this.http
            .put<Therapy>(url, JSON.stringify(therapy))
            .map(response => response);
        }
    
        create(therapy: Therapy): Observable<Therapy> {
          return this.http
            .post<Therapy>(PreclinicalUtils.PRECLINICAL_API_THERAPIES_URL, JSON.stringify(therapy))
            .map(res => res);
        }
    
        delete(id: number): Promise<void> {
        	const url = `${PreclinicalUtils.PRECLINICAL_API_THERAPIES_URL}/`+id;
        	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete Therapy', error);
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