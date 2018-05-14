import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import * as AppUtils from '../../utils/app.utils';
import { Coil } from './coil.model';

@Injectable()
export class CoilService {
    constructor(private http: HttpClient) { }

    getCoils(): Promise<Coil[]> {
        return this.http.get<Coil[]>(AppUtils.BACKEND_API_COIL_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting coils', error);
                return Promise.reject(error.message || error);
            });
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_COIL_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete coil', error);
                return Promise.reject(error.message || error);
            });
    }

    getCoil(id: number): Promise<Coil> {
        return this.http.get<Coil>(AppUtils.BACKEND_API_COIL_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting coil', error);
                return Promise.reject(error.message || error);
            });
    }

    create(coil: Coil): Observable<Coil> {
        return this.http.post<Coil>(AppUtils.BACKEND_API_COIL_URL, JSON.stringify(coil))
            .map(response => response);
    }

    update(id: number, coil: Coil): Observable<Coil> {
        return this.http.put<Coil>(AppUtils.BACKEND_API_COIL_URL + '/' + id, JSON.stringify(coil))
            .map(response => response);
    }
}