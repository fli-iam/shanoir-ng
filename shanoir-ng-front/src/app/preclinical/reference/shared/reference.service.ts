import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';


import { Reference } from './reference.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';

@Injectable()
export class ReferenceService {    
    
        constructor(private http: HttpClient) { }    
    
        getReferences(): Promise<Reference[]>{
            return this.http.get<Reference[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_ALL_URL)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting references', error);
                        return Promise.reject(error.message || error);
            });
        }
        
    
        getReference(id: number): Promise<Reference>{
            return this.http.get<Reference>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/reference/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting Reference', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        getCategories(): Promise<string[]> {
            return this.http.get<string[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_CATEGORIES_ALL_URL)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting references categories', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        getTypesByCategory(category: string): Promise<string[]> {
            return this.http.get<string[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/"+category+"/types")
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting references types by category', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        getReferencesByCategory(category: string): Promise<Reference[]> {
          return this.http
               .get<Reference[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/"+category)
               .toPromise()
               .then(response => response)
               .catch((error) => {
                        console.error('Error while getting references', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        getReferencesByCategoryAndType(category: string,reftype: string): Promise<Reference[]> {
          return this.http
               .get<Reference[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/"+category+"/"+reftype)
               .toPromise()
               .then(response => response)
               .catch((error) => {
                        console.error('Error while getting references', error);
                        return Promise.reject(error.message || error);
            });
        }

        getReferenceByCategoryTypeAndValue(category: string, reftype: string, value: string): Observable<Reference> {
          return this.http
               .get<Reference>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/"+category+"/"+reftype+"/"+value)
               .map(response => response);
        }
       
        
    
        update(reference: Reference): Observable<Reference> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL}/reference/${reference.id}`;
          return this.http
            .put<Reference>(url, JSON.stringify(reference))
            .map(response => response);
        }
    
        create(reference: Reference): Observable<Reference> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL}`;
          return this.http
            .post<Reference>(url, JSON.stringify(reference))
            .map(res => res);
        }
    
       
     
      delete(reference: Reference): Promise<void> {
      	const url = `${PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL}/reference/${reference.id}`;
        return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete reference', error);
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