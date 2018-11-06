import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../../shared/components/entity/entity.abstract.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { Reference } from './reference.model';



@Injectable()
export class ReferenceService extends EntityService<Reference>{    
    API_URL = PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL;

    getEntityInstance() { return new Reference(); }
    
       
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
            return this.http.get<string[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/category/"+category+"/types")
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting references types by category', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        getReferencesByCategory(category: string): Promise<Reference[]> {
          return this.http
               .get<Reference[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/category/"+category)
               .toPromise()
               .then(response => response)
               .catch((error) => {
                        console.error('Error while getting references', error);
                        return Promise.reject(error.message || error);
            });
        }
    
        getReferencesByCategoryAndType(category: string,reftype: string): Promise<Reference[]> {
          return this.http
               .get<Reference[]>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/category/"+category+"/"+reftype)
               .toPromise()
               .then(response => response)
               .catch((error) => {
                        console.error('Error while getting references', error);
                        return Promise.reject(error.message || error);
            });
        }

        getReferenceByCategoryTypeAndValue(category: string, reftype: string, value: string): Observable<Reference> {
          return this.http
               .get<Reference>(PreclinicalUtils.PRECLINICAL_API_REFERENCES_URL+"/category/"+category+"/"+reftype+"/"+value)
               .map(response => response);
        }
       
        
    
    
}