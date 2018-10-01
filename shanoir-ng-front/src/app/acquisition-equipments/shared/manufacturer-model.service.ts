import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { IdNameObject } from '../../shared/models/id-name-object.model';
import * as AppUtils from '../../utils/app.utils';
import { ManufacturerModel } from './manufacturer-model.model';

@Injectable()
export class ManufacturerModelService {
    constructor(private http: HttpClient) { }

    getManufacturerModels(): Promise<ManufacturerModel[]> {
        return this.http.get<ManufacturerModel[]>(AppUtils.BACKEND_API_MANUF_MODEL_URL)
            .map(entities => entities.map((entity) => Object.assign(new ManufacturerModel(), entity)))
            .toPromise();
    }

    getManufacturerModelsNames(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_MANUF_MODEL_NAMES_URL)
            .toPromise();
    }

    getCenterManufacturerModelsNames(centerId:Number): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_CENTER_MANUF_MODEL_NAMES_URL+ '/' + centerId)
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id)
            .toPromise();
    }

    getManufacturerModel(id: number): Promise<ManufacturerModel> {
        return this.http.get<ManufacturerModel>(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id)
            .map((entity) => Object.assign(new ManufacturerModel(), entity))
            .toPromise();
    }

    create(manufModel: ManufacturerModel): Promise<ManufacturerModel> {
        return this.http.post<ManufacturerModel>(AppUtils.BACKEND_API_MANUF_MODEL_URL, JSON.stringify(manufModel))
            .map((entity) => Object.assign(new ManufacturerModel(), entity))
            .toPromise();
    }

    update(id: number, manufModel: ManufacturerModel): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id, JSON.stringify(manufModel))
            .toPromise();
    }
}