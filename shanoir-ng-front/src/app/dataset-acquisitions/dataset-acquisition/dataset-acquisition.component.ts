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
import { Component } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../../acquisition-equipments/shared/acquisition-equipment.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { StudyCardService } from '../../study-cards/shared/study-card.service';
import { DatasetAcquisition } from '../shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from '../shared/dataset-acquisition.service';
import { MrDatasetAcquisition } from '../modality/mr/mr-dataset-acquisition.model';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';


@Component({
    selector: 'dataset-acquisition',
    templateUrl: 'dataset-acquisition.component.html',
    styleUrls: ['dataset-acquisition.component.css']
})
export class DatasetAcquisitionComponent extends EntityComponent<DatasetAcquisition> {

    private studyCards: StudyCard[];
    private acquisitionEquipments: AcquisitionEquipment[];
    
    
    constructor(
            private route: ActivatedRoute,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private studyCardService: StudyCardService,
            private acqEqService: AcquisitionEquipmentService,
            private acqEqPipe: AcquisitionEquipmentPipe) {
        super(route, 'dataset-acquisition');
    }
    
    get datasetAcquisition(): DatasetAcquisition { return this.entity; }
    set datasetAcquisition(datasetAcquisition: DatasetAcquisition) { this.entity = datasetAcquisition; }

    initView(): Promise<void> {
        return this.datasetAcquisitionService.get(this.id).then(dsAcq => {
            this.datasetAcquisition = dsAcq;
        });
    }
    
    initEdit(): Promise<void> {
        this.studyCardService.getAll().then(scs => this.studyCards = scs);
        this.acqEqService.getAll().then(aes => this.acquisitionEquipments = aes);
        return this.datasetAcquisitionService.get(this.id).then(dsAcq => {
            this.datasetAcquisition = dsAcq;
        });
    }

    initCreate(): Promise<void> {
        this.studyCardService.getAll().then(scs => this.studyCards = scs);
        this.acqEqService.getAll().then(aes => this.acquisitionEquipments = aes);
        this.datasetAcquisition = new MrDatasetAcquisition();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'type': [this.datasetAcquisition.type],
            'study-card': [this.datasetAcquisition.studyCard],
            'acq-eq': [this.datasetAcquisition.acquisitionEquipment, [Validators.required]],
            //'examination': [this.datasetAcquisition.examination, [Validators.required]],
            'rank': [this.datasetAcquisition.rank],
            'software-release': [this.datasetAcquisition.softwareRelease],
            'sorting-index': [this.datasetAcquisition.sortingIndex],
            'protocol': [this.datasetAcquisition.protocol] 
        });
    }

    public hasEditRight(): boolean {
        return this.keycloakService.isUserAdminOrExpert(); // TODO
    }
}