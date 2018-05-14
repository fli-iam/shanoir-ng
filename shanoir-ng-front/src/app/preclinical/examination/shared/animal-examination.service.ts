import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Examination } from '../shared/examination.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { Pageable } from '../../../shared/components/table/pageable.model';
import * as AppUtils from '../../../utils/app.utils';

@Injectable()
export class AnimalExaminationService {
             
    constructor(private http: HttpClient) { }    
    
    
    getExaminations(pageable: Pageable): Promise<Examination[]> {
        return this.http.get<Examination[]>(AppUtils.BACKEND_API_EXAMINATION_URL + AppUtils.getPageableQuery(pageable))
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting examinations', error);
                return Promise.reject(error.message || error);
            });
    }
    
    
    countExaminations(): Promise<number> {
        return this.http.get<number>(AppUtils.BACKEND_API_EXAMINATION_COUNT_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while counting examinations', error);
                return Promise.reject(error.message || error);
            });
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