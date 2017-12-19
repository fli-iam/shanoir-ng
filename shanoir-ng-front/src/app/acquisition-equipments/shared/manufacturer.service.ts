import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Manufacturer } from './manufacturer.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle-error.service';

@Injectable()
export class ManufacturerService {
    constructor(private http: HttpClient, private handleErrorService: HandleErrorService) { }

    getManufacturers(): Promise<Manufacturer[]> {
        return this.http.get<Manufacturer[]>(AppUtils.BACKEND_API_MANUF_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting manufs', error);
                return Promise.reject(error.message || error);
            });
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_MANUF_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete manuf', error);
                return Promise.reject(error.message || error);
            });
    }

    getManufacturer(id: number): Promise<Manufacturer> {
        return this.http.get<Manufacturer>(AppUtils.BACKEND_API_MANUF_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting manuf', error);
                return Promise.reject(error.message || error);
            });
    }

    create(manuf: Manufacturer): Observable<Manufacturer> {
        return this.http.post<Manufacturer>(AppUtils.BACKEND_API_MANUF_URL, JSON.stringify(manuf))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, manuf: Manufacturer): Observable<Manufacturer> {
        return this.http.put<Manufacturer>(AppUtils.BACKEND_API_MANUF_URL + '/' + id, JSON.stringify(manuf))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }
}