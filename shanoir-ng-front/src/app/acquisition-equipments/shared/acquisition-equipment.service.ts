/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { Center } from 'src/app/centers/shared/center.model';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { ShanoirError } from "../../shared/models/error.model";
import { StudyCard } from "../../study-cards/shared/study-card.model";

import { AcquisitionEquipment } from './acquisition-equipment.model';
import { ManufacturerModel } from "./manufacturer-model.model";

@Injectable()
export class AcquisitionEquipmentService extends EntityService<AcquisitionEquipment> {

    API_URL = AppUtils.BACKEND_API_ACQ_EQUIP_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() {
        return new AcquisitionEquipment();
    }

    getAllByCenter(centerId: number): Promise<AcquisitionEquipment[]> {
        return this.http.get<AcquisitionEquipment[]>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/byCenter/' + centerId)
            .toPromise()
            .catch(this.arrayFrom404)
            .then(this.mapEntityList);
    }

    getAllByStudy(studyId: number): Promise<AcquisitionEquipment[]> {
        return this.http.get<AcquisitionEquipment[]>(AppUtils.BACKEND_API_ACQ_EQUIP_URL + '/byStudy/' + studyId)
            .toPromise()
            .then(this.mapEntityList);
    }

    delete(id: number): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            this.http
                .get<StudyCard[]>(AppUtils.BACKEND_API_STUDY_CARD_URL + '/byAcqEq/' + id)
                .toPromise()
                .then(cards => {
                    if (cards?.length == 1) {
                        throw new ShanoirError({ error: { code: 422, message: 'This acquisition-equipment is linked to the study card n°' + cards[0].id + '.' } })
                        reject();
                    }
                    else if (cards?.length > 1) {
                        throw new ShanoirError({ error: { code: 422, message: 'This acquisition-equipment is linked to ' + cards.length + ' study cards, more info in the details.', details: 'Study cards : ' + cards.map(card => card.id).join(', ') } })
                        reject();
                    }
                    return super.delete(id)
                        .then(() => resolve())
                        .catch(err => reject(err));
                })
                .catch(err => {
                    throw this.consoleService.log('warn', err.message);
                });
        });
    }

    checkDuplicate(serialNumber: string, manufacturerModel: ManufacturerModel, center: Center): Promise<boolean> {
        return firstValueFrom(
            this.http.get<AcquisitionEquipment[]>(
                `${AppUtils.BACKEND_API_ACQ_EQUIP_URL}/bySerialNumber/${serialNumber}`
            )
        ).then(equipments => {
            return equipments.some(equipment =>
                equipment.manufacturerModel.id === manufacturerModel.id
                && equipment.serialNumber === serialNumber
                && equipment.center.id === center.id
            );
        }).catch(error => {
            if (error instanceof HttpErrorResponse && error.status === 404) {
                console.log("catch 404 error: no equipments found for this serial number");
                return false; // No equipments found, so no duplicates
            }
            // Handle other errors
            throw new ShanoirError({error: {code: error.status, message: error.message}});
        });
    }
}
