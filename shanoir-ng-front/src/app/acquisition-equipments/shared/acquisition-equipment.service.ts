import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';
import { AcquisitionEquipment } from './acquisition-equipment.model';

@Injectable()
export class AcquisitionEquipmentService {
    constructor(private http: HttpClient) { }

    getAcquisitionEquipments(): Promise<AcquisitionEquipment[]> {
        return this.http.get<AcquisitionEquipment[]>(AppUtils.BACKEND_API_ACQ_EQUIP_URL)
            .map(acqs => acqs.map(acq => Object.assign(new AcquisitionEquipment(), acq))) 
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id)
            .toPromise();
    }

    getAcquisitionEquipment(id: number): Promise<AcquisitionEquipment> {
        return this.http.get<AcquisitionEquipment>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id)
            .map(acq => Object.assign(new AcquisitionEquipment(), acq)) 
            .toPromise();
    }

    create(acqEquip: AcquisitionEquipment): Promise<AcquisitionEquipment> {
        return this.http.post<AcquisitionEquipment>(AppUtils.BACKEND_API_ACQ_EQUIP_URL, JSON.stringify(acqEquip))
            .map(acq => Object.assign(new AcquisitionEquipment(), acq)) 
            .toPromise();
    }

    update(id: number, acqEquip: AcquisitionEquipment): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/' + id, JSON.stringify(acqEquip))
            .toPromise();
    }
}