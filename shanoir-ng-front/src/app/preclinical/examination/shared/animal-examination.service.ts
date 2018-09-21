import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Examination } from '../../../examinations/shared/examination.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { Page, Pageable } from '../../../shared/components/table/pageable.model';
import * as AppUtils from '../../../utils/app.utils';

@Injectable()
export class AnimalExaminationService {
             
    constructor(private http: HttpClient) { }    
    
     getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.http.get<Page<Examination>>(
            AppUtils.BACKEND_API_EXAMINATION_PRECLINICAL_URL+'/1', 
            { 'params': pageable.toParams() }
        ).toPromise();
    }

    
    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete examination', error);
                return Promise.reject(error.message || error);
            });
    }
    
    create(examination: Examination): Observable<Examination> {
        return this.http.post<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL, JSON.stringify(examination))
            .map(response => response);
    }
    
    update(id: number, examination: Examination): Observable<Examination> {
        return this.http.put<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id, JSON.stringify(examination))
            .map(response => response);
    }
    
    
    
    getExamination(id: number): Promise<Examination> {
        return this.http.get<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting examination', error);
                return Promise.reject(error.message || error);
            });
    }
        
}