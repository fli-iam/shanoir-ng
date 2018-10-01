import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';
import { Manufacturer } from './manufacturer.model';

@Injectable()
export class ManufacturerService {
    constructor(private http: HttpClient) { }

    getManufacturers(): Promise<Manufacturer[]> {
        return this.http.get<Manufacturer[]>(AppUtils.BACKEND_API_MANUF_URL)
            .map(entities => entities.map((entity) => Object.assign(new Manufacturer(), entity)))
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_MANUF_URL + '/' + id)
            .toPromise();
    }

    getManufacturer(id: number): Promise<Manufacturer> {
        return this.http.get<Manufacturer>(AppUtils.BACKEND_API_MANUF_URL + '/' + id)
            .map((entity) => Object.assign(new Manufacturer(), entity))
            .toPromise();
    }

    create(manuf: Manufacturer): Promise<Manufacturer> {
        return this.http.post<Manufacturer>(AppUtils.BACKEND_API_MANUF_URL, JSON.stringify(manuf))
            .map((entity) => Object.assign(new Manufacturer(), entity))
            .toPromise();
    }

    update(id: number, manuf: Manufacturer): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_MANUF_URL + '/' + id, JSON.stringify(manuf))
            .toPromise();
    }
}