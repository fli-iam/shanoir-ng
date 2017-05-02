import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { AcquisitionEquipment } from './acqEquip.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class AcquisitionEquipmentService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    getAcquisitionEquipments(): Promise<AcquisitionEquipment[]> {
        return this.http.get(AppUtils.BACKEND_API_ACQ_EQUIP_ALL_URL)
            .toPromise()
            .then(response => response.json() as AcquisitionEquipment[])
            .catch((error) => {
                console.error('Error while getting acqEquips', error);
                return Promise.reject(error.message || error);
        });
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete acqEquip', error);
                return Promise.reject(error.message || error);
        });
    }

    getAcquisitionEquipment (id: number): Promise<AcquisitionEquipment> {
        return this.http.get(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id)
            .toPromise()
            .then(res => res.json() as AcquisitionEquipment)
            .catch((error) => {
                console.error('Error while getting acqEquip', error);
                return Promise.reject(error.message || error);
        });
    }

    create(acqEquip: AcquisitionEquipment): Observable<AcquisitionEquipment> {
        return this.http.post(AppUtils.BACKEND_API_ACQ_EQUIP_URL, JSON.stringify(acqEquip))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, acqEquip: AcquisitionEquipment): Observable<AcquisitionEquipment> {
        return this.http.put(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id, JSON.stringify(acqEquip))
            .map(response => response.json() as AcquisitionEquipment)
            .catch(this.handleErrorService.handleError);
    }
}