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

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../../acquisition-equipments/shared/acquisition-equipment.service';
import { DatasetDTO, DatasetDTOService } from '../../datasets/shared/dataset.dto';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetUtils } from '../../datasets/shared/dataset.utils';
import { ExaminationDTO, ExaminationDTOService } from '../../examinations/shared/examination.dto';
import { Examination } from '../../examinations/shared/examination.model';
import { StudyService } from '../../studies/shared/study.service';
import { StudyCardDTOService } from '../../study-cards/shared/study-card.dto';
import { StudyCardDTO } from '../../study-cards/shared/study-card.dto.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { CtDatasetAcquisition } from '../modality/ct/ct-dataset-acquisition.model';
import { CtProtocol } from '../modality/ct/ct-protocol.model';
import { MrDatasetAcquisition } from '../modality/mr/mr-dataset-acquisition.model';
import { MrProtocol } from '../modality/mr/mr-protocol.model';
import { PetDatasetAcquisition } from '../modality/pet/pet-dataset-acquisition.model';
import { PetProtocol } from '../modality/pet/pet-protocol.model';
import { XaDatasetAcquisition } from '../modality/xa/xa-dataset-acquisition.model';
import { XaProtocol } from '../modality/xa/xa-protocol.model';
import { DatasetAcquisition } from './dataset-acquisition.model';
import { DatasetAcquisitionUtils } from './dataset-acquisition.utils';

@Injectable()
export class DatasetAcquisitionDTOService {

    constructor(
        private acqEqService: AcquisitionEquipmentService,
        private studyService: StudyService,
        private datasetDtoService: DatasetDTOService) {}

    /**
     * Convert from DatasetAcquisitionDTO to DatasetAcquisition
     * Warning : DO NOT USE THIS IN A LOOP, use toDatasetAcquisitions instead
     * @param result can be used to get an immediate temporary result without async data
     */
    public toDatasetAcquisition(dto: DatasetAcquisitionDTO, result?: DatasetAcquisition): Promise<DatasetAcquisition> {
        if (!result) result = DatasetAcquisitionUtils.getNewDAInstance(dto.type);
        DatasetAcquisitionDTOService.mapSyncFields(dto, result);
        let promises = [];
        if(dto.acquisitionEquipmentId > 0){
            promises.push(this.acqEqService.get(dto.acquisitionEquipmentId).then(acqEq => result.acquisitionEquipment = acqEq));
        }else{
            result.acquisitionEquipment = new AcquisitionEquipment();
        }
        promises.push( this.studyService.get(dto.examination.studyId).then(study => result.examination.study = study));
        return Promise.all(promises).then(([]) => {
            return result;
        });
    }

    /**
     * Convert from DatasetAcquisitionDTO list to DatasetAcquisition list
     * @param result can be used to get an immediate temporary result without async data
     */
    public toDatasetAcquisitions(dtos: DatasetAcquisitionDTO[], result?: DatasetAcquisition[]): Promise<DatasetAcquisition[]>{
        if (!result) result = [];
        for (let dto of dtos ? dtos : []) {
            let entity = DatasetAcquisitionUtils.getNewDAInstance(dto.type);
            DatasetAcquisitionDTOService.mapSyncFields(dto, entity);
            if ((dto as DatasetAcquisitionDatasetsDTO).datasets) {
                entity.datasets = (dto as DatasetAcquisitionDatasetsDTO).datasets.map(dsdto => {
                    let simpleDataset: Dataset = DatasetUtils.getDatasetInstance(dsdto.type);
                    DatasetDTOService.mapSyncFields(dsdto, simpleDataset);
                    return simpleDataset;
                });
            }
            result.push(entity);
        }
        return Promise.all([
            this.acqEqService.getAll(),
            this.studyService.getStudiesNames()
        ]).then(([acqs, studies]) => {
            for (let entity of result) {
                if (entity.acquisitionEquipment) entity.acquisitionEquipment = acqs.find(acq => acq.id == entity.acquisitionEquipment.id);
                if (entity.examination && entity.examination.study) entity.examination.study = studies.find(study => study.id == entity.examination.study.id);
            }
            return result;
        })
    }

    static mapSyncFields(dto: DatasetAcquisitionDTO, entity: DatasetAcquisition): DatasetAcquisition {
        entity.id = dto.id;
        if (dto.studyCard) {
            entity.studyCard = new StudyCard();
            StudyCardDTOService.mapSyncFields(dto.studyCard, entity.studyCard)
        }
        entity.rank = dto.rank;
        entity.softwareRelease = dto.softwareRelease;
        if (dto.acquisitionStartTime) entity.acquisitionStartTime = new Date(dto.acquisitionStartTime);
        entity.sortingIndex = dto.sortingIndex;
        entity.type = dto.type;
        entity.source = dto.source;
        entity.copies = dto.copies;
        entity.importDate = dto.importDate;
        entity.username = dto.username;
        if (dto.acquisitionEquipmentId) {
            entity.acquisitionEquipment = new AcquisitionEquipment();
            entity.acquisitionEquipment.id = dto.acquisitionEquipmentId;
        }
        if (dto.examination) {
            entity.examination = new Examination();
            ExaminationDTOService.mapSyncFields(dto.examination, entity.examination);
        }
        switch(entity.type) {
            case 'Mr': {
                (entity as MrDatasetAcquisition).protocol = Object.assign(new MrProtocol(), (dto as MrDatasetAcquisitionDTO).protocol);
                break;
            }
            case 'Pet': {
                (entity as PetDatasetAcquisition).protocol = Object.assign(new PetProtocol(), (dto as PetDatasetAcquisitionDTO).protocol);
                break;
            }
            case 'Ct': {
                (entity as CtDatasetAcquisition).protocol = Object.assign(new CtProtocol(), (dto as CtDatasetAcquisitionDTO).protocol);
                break;
            }
            case 'Xa': {
                (entity as XaDatasetAcquisition).protocol = Object.assign(new XaProtocol(), (dto as XaDatasetAcquisitionDTO).protocol);
                break;
            }

        }
        return entity;
    }

}


export class DatasetAcquisitionDTO {

    constructor(dsAcq: DatasetAcquisition) {
        this.id = dsAcq.id;
        if (dsAcq.studyCard) {
            this.studyCard = new StudyCardDTO();
            this.studyCard.id = dsAcq.studyCard.id;
        }
        this.acquisitionEquipmentId = dsAcq.acquisitionEquipment ? dsAcq.acquisitionEquipment.id : null;
        if (dsAcq.examination) {
            this.examination = new ExaminationDTO();
            this.examination.id = dsAcq.examination.id;
        }
        this.rank = dsAcq.rank;
        this.softwareRelease = dsAcq.softwareRelease;
        this.sortingIndex = dsAcq.sortingIndex;
        this.type = dsAcq.type;
        this.importDate = dsAcq.importDate;
        this.username = dsAcq.username;
        this.source = dsAcq.source;
        this.copies = dsAcq.copies;
    }

    id: number;
    studyCard: StudyCardDTO;
    acquisitionEquipmentId: number;
    examination: ExaminationDTO;
    rank: number;
    softwareRelease: string;
    acquisitionStartTime: Date;
    sortingIndex: number;
    importDate: Date;
    type: 'Mr' | 'Pet' | 'Ct' | 'Eeg' | 'Xa' | 'Generic' | 'Processed' | 'BIDS';
    username: string;
    copies: number[];
    source: number;
}

export class MrDatasetAcquisitionDTO extends DatasetAcquisitionDTO {
    protocol: any;
}

export class PetDatasetAcquisitionDTO extends DatasetAcquisitionDTO {
    protocol: any;
}

export class CtDatasetAcquisitionDTO extends DatasetAcquisitionDTO {
    protocol: any;
}

export class XaDatasetAcquisitionDTO extends DatasetAcquisitionDTO {
    protocol: any;
}

export class ProcessedDatasetAcquisitionDTO extends DatasetAcquisitionDTO {
   	parentAcquisitions: any[];
}

export class ExaminationDatasetAcquisitionDTO {
    id: number;
    name: string;
    type: 'Mr' | 'Pet' | 'Ct' | 'Eeg' | 'Xa' | 'Generic' | 'Processed' | 'BIDS';
    datasets: any;
}

export class DatasetAcquisitionDatasetsDTO extends DatasetAcquisitionDTO {
    datasets: DatasetDTO[];
}
