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
 * anumber with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Dataset } from '../../datasets/shared/dataset.model';
import { Examination } from '../../examinations/shared/examination.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { StudyCard } from '../../study-cards/shared/study-card.model';


export abstract class DatasetAcquisition extends Entity {
    
    id: number;
    datasets: Dataset[];
    studyCard: StudyCard;
    acquisitionEquipment: AcquisitionEquipment;
    examination: Examination;
    rank: number;
    softwareRelease: string;
    sortingIndex: number;
    type: 'Mr' | 'Pet' | 'Ct' | 'Eeg' | 'Generic' | 'Processed'; // TODO : other types
    protocol: any;
    name: string; // set in ExaminationDatasetAcquisitionDecorator.java
    creationDate: Date;
}