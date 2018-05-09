import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { ManufacturerModel } from './manufacturer-model.model';
import * as AppUtils from '../../utils/app.utils';
import { IdNameObject } from '../../shared/models/id-name-object.model';

@Injectable()
export class ManufacturerModelService {
    constructor(private http: HttpClient) { }

    getManufacturerModels(): Promise<ManufacturerModel[]> {
        return this.http.get<ManufacturerModel[]>(AppUtils.BACKEND_API_MANUF_MODEL_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting manufModels', error);
                return Promise.reject(error.message || error);
            });
    }

    getManufacturerModelsNames(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_MANUF_MODEL_NAMES_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting manufacturer models', error);
                return Promise.reject(error.message || error);
            });
    }

    getCenterManufacturerModelsNames(centerId:Number): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_CENTER_MANUF_MODEL_NAMES_URL+ '/' + centerId)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting center manufacturer models', error);
                return Promise.reject(error.message || error);
            });
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete manufModel', error);
                return Promise.reject(error.message || error);
            });
    }

    getManufacturerModel(id: number): Promise<ManufacturerModel> {
        return this.http.get<ManufacturerModel>(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting manufModel', error);
                return Promise.reject(error.message || error);
            });
    }

    create(manufModel: ManufacturerModel): Observable<ManufacturerModel> {
        return this.http.post<ManufacturerModel>(AppUtils.BACKEND_API_MANUF_MODEL_URL, JSON.stringify(manufModel))
            .map(response => response);
    }

    update(id: number, manufModel: ManufacturerModel): Observable<ManufacturerModel> {
        return this.http.put<ManufacturerModel>(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id, JSON.stringify(manufModel))
            .map(response => response);
    }
}