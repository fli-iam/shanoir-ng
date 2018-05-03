import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Anesthetic } from './anesthetic.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { HandleErrorService } from '../../../../shared/utils/handle-error.service';

@Injectable()
export class AnestheticService {
             
        constructor(private http: HttpClient,private handleErrorService: HandleErrorService) { }    
        
        getAnesthetics(): Promise<Anesthetic[]>{
            return this.http.get<Anesthetic[]>(PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_ALL_URL)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting anesthetics', error);
                        return Promise.reject(error.message || error);
            });
        }
  
        getAnesthetic(id:number): Promise<Anesthetic>{
            return this.http.get<Anesthetic>(PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL+"/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting anesthetic', error);
                        return Promise.reject(error.message || error);
            });
        }
  
    
        update(anesthetic: Anesthetic): Observable<Anesthetic> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/`+anesthetic.id;
          return this.http
            .put<Anesthetic>(url, JSON.stringify(anesthetic))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
        }
    
        create(anesthetic: Anesthetic): Observable<Anesthetic> {
          return this.http
            .post<Anesthetic>(PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL, JSON.stringify(anesthetic))
            .map(res => res)
            .catch(this.handleErrorService.handleError);
        }
        
        delete(id: number): Promise<void> {
        	const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/`+id;
        	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete Anesthetic', error);
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