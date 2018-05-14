import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Pathology } from './pathology.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class PathologyService {
         
        constructor(private http: HttpClient) { }    
           
    
        getPathologies(): Promise<Pathology[]>{
            return this.http.get<Pathology[]>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_ALL_URL)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting pathologies', error);
                        return Promise.reject(error.message || error);
            });
        }
           
        getPathology(id: string): Promise<Pathology>{
            return this.http.get<Pathology>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL+"/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting Pathology', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        update(pathology: Pathology): Observable<Pathology> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL}/${pathology.id}`;
          return this.http
            .put<Pathology>(url, JSON.stringify(pathology))
            .map(response => response);
        }
    
        create(pathology: Pathology): Observable<Pathology> {
       		 const headers = new HttpHeaders().set('Content-Type', 'application/json' );
       		
          return this.http
            .post<Pathology>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL, JSON.stringify(pathology), { headers })
            .map(res => res);
        }
    
                
        delete(id: number): Promise<void> {
        	return this.http.delete<void>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL + '/' + id)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete pathology', error);
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