import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { ManufacturerModel } from './manufModel.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class ManufacturerModelService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    getManufacturerModels(): Promise<ManufacturerModel[]> {
        return this.http.get(AppUtils.BACKEND_API_MANUF_MODEL_ALL_URL)
            .toPromise()
            .then(response => response.json() as ManufacturerModel[])
            .catch((error) => {
                console.error('Error while getting manufModels', error);
                return Promise.reject(error.message || error);
        });
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete manufModel', error);
                return Promise.reject(error.message || error);
        });
    }

    getManufacturerModel (id: number): Promise<ManufacturerModel> {
        return this.http.get(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id)
            .toPromise()
            .then(res => res.json() as ManufacturerModel)
            .catch((error) => {
                console.error('Error while getting manufModel', error);
                return Promise.reject(error.message || error);
        });
    }

    create(manufModel: ManufacturerModel): Observable<ManufacturerModel> {
        return this.http.post(AppUtils.BACKEND_API_MANUF_MODEL_URL, JSON.stringify(manufModel))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, manufModel: ManufacturerModel): Observable<ManufacturerModel> {
        return this.http.put(AppUtils.BACKEND_API_MANUF_MODEL_URL + '/' + id, JSON.stringify(manufModel))
            .map(response => response.json() as ManufacturerModel)
            .catch(this.handleErrorService.handleError);
    }
}