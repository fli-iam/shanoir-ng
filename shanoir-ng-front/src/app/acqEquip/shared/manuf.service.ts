import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Manufacturer } from './manuf.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class ManufacturerService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    getManufacturers(): Promise<Manufacturer[]> {
        return this.http.get(AppUtils.BACKEND_API_MANUF_ALL_URL)
            .toPromise()
            .then(response => response.json() as Manufacturer[])
            .catch((error) => {
                console.error('Error while getting manufs', error);
                return Promise.reject(error.message || error);
        });
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_MANUF_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete manuf', error);
                return Promise.reject(error.message || error);
        });
    }

    getManufacturer (id: number): Promise<Manufacturer> {
        return this.http.get(AppUtils.BACKEND_API_MANUF_URL + '/' + id)
            .toPromise()
            .then(res => res.json() as Manufacturer)
            .catch((error) => {
                console.error('Error while getting manuf', error);
                return Promise.reject(error.message || error);
        });
    }

    create(manuf: Manufacturer): Observable<Manufacturer> {
        return this.http.post(AppUtils.BACKEND_API_MANUF_URL, JSON.stringify(manuf))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, manuf: Manufacturer): Observable<Manufacturer> {
        return this.http.put(AppUtils.BACKEND_API_MANUF_URL + '/' + id, JSON.stringify(manuf))
            .map(response => response.json() as Manufacturer)
            .catch(this.handleErrorService.handleError);
    }
}