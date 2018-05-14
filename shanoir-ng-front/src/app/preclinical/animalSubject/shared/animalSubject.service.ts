import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { AnimalSubject } from './animalSubject.model';
import { Subject }    from '../shared/subject.model';
import { PreclinicalSubject } from './preclinicalSubject.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import * as AppUtils from '../../../utils/app.utils';

@Injectable()
export class AnimalSubjectService {
             
        constructor(private http: HttpClient) { }    
    
    
        getAnimalSubjects(): Promise<AnimalSubject[]>{
            return this.http.get<AnimalSubject[]>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_ALL_URL)
                    .toPromise()
                    .then(response => response )
                    .catch((error) => {
                        console.error('Error while getting animal subjects', error);
                        return Promise.reject(error.message || error);
            });
        }
        
        getSubjects(): Promise<Subject[]> {
        	return this.http.get<Subject[]>(AppUtils.BACKEND_API_SUBJECT_URL)
            	.toPromise()
            	.then(response => response)
            	.catch((error) => {
                	console.error('Error while getting subjects', error);
                	return Promise.reject(error.message || error);
            	});
    	}
  
        getAnimalSubject(id: number): Promise<AnimalSubject>{
            return this.http.get<AnimalSubject>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL+"/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting AnimalSubject', error);
                        return Promise.reject(error.message || error);
            });
        }
        
        getSubject(id: number): Promise<Subject> {
        	return this.http.get<Subject>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + id)
            	.toPromise()
            	.then(res => res)
            	.catch((error) => {
                	console.error('Error while getting subject', error);
                	return Promise.reject(error.message || error);
            	});
    	}
  
    
        update(animalSubject: AnimalSubject): Observable<AnimalSubject> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/`+animalSubject.id;
          return this.http
            .put<AnimalSubject>(url, JSON.stringify(animalSubject))
            .map(response => response);
        }
        
        updateSubject(id: number, subject: Subject): Observable<Subject> {
        	return this.http.put<Subject>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + id, JSON.stringify(subject))
            	.map(response => response);
    	}
        
    
        create(animalSubject: AnimalSubject): Observable<AnimalSubject> {
        	return this.http.post<AnimalSubject>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL, JSON.stringify(animalSubject))
            	.map(res => res);
    	}
    	
    	createSubject(subject: Subject): Observable<Subject> {
    		return this.http.post<Subject>(AppUtils.BACKEND_API_SUBJECT_URL, JSON.stringify(subject))
            	.map(res => res);
    	}
    
        
        delete(id: number): Promise<void> {
        	return this.http.delete<void>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL + '/' + id)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete animalSubject', error);
                	return Promise.reject(error);
            	});
    	}
    	
    	deleteSubject(id: number): Promise<void> {
        	return this.http.delete<void>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + id)
           		.toPromise()
            	.catch((error) => {
                	console.error('Error delete subject', error);
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