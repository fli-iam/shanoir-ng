import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { AcquisitionEquipment } from './acquisition-equipment.model';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class AcquisitionEquipmentService {
    constructor(private http: HttpClient) { }

    getAcquisitionEquipments(): Promise<AcquisitionEquipment[]> {
        return this.http.get<AcquisitionEquipment[]>(AppUtils.BACKEND_API_ACQ_EQUIP_URL)
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete acqEquip', error);
                return Promise.reject(error.message || error);
            });
    }

    getAcquisitionEquipment(id: number): Promise<AcquisitionEquipment> {
        return this.http.get<AcquisitionEquipment>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting acqEquip', error);
                return Promise.reject(error.message || error);
            });
    }

    create(acqEquip: AcquisitionEquipment): Observable<AcquisitionEquipment> {
        return this.http.post<AcquisitionEquipment>(AppUtils.BACKEND_API_ACQ_EQUIP_URL, JSON.stringify(acqEquip))
            .map(res => res);
    }

    update(id: number, acqEquip: AcquisitionEquipment): Observable<AcquisitionEquipment> {
        return this.http.put<AcquisitionEquipment>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id, JSON.stringify(acqEquip))
            .map(response => response);
    }
}