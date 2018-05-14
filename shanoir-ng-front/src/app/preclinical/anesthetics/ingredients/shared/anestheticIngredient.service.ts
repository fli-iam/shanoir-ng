import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { AnestheticIngredient } from './anestheticIngredient.model';

import { Anesthetic } from '../../anesthetic/shared/anesthetic.model';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class AnestheticIngredientService {
         
        constructor(private http: HttpClient) { }               
    
        getIngredients(anesthetic:Anesthetic): Promise<AnestheticIngredient[]>{
            const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic.id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
            return this.http.get<AnestheticIngredient[]>(url)
                    .toPromise()
                    .then(response => response )
                    .catch((error) => {
                        console.error('Error while getting AnestheticIngredient list', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        
        getIngredient(anesthetic_id:number,ingredient_id: number): Promise<AnestheticIngredient>{
            return this.http.get<AnestheticIngredient>(PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL+"/"+anesthetic_id+"/"+PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT+"/"+ingredient_id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting AnestheticIngredient', error);
                        return Promise.reject(error.message || error);
            });
        }
     
        update(anesthetic_id:number,ingredient: AnestheticIngredient): Observable<AnestheticIngredient> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}/${ingredient.id}`;
          return this.http
            .put<AnestheticIngredient>(url, JSON.stringify(ingredient))
            .map(response => response);
        }
    
        create(anesthetic:Anesthetic, ingredient: AnestheticIngredient): Observable<AnestheticIngredient> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic.id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}`;
          return this.http
            .post<AnestheticIngredient>(url, JSON.stringify(ingredient))
            .map(res => res);
        }
        
        delete(anesthetic_id:number,ingredient_id: number): Promise<void> {
        	const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}/${ingredient_id}`;
          	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete AnestheticIngredient', error);
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