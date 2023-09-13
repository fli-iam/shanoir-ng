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
import {Component, ViewChild} from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
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
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import {DatasetAcquisitionNode, ExaminationNode} from '../../tree/tree.model';
import {DatasetService} from "../../datasets/shared/dataset.service";
import {LoadingBarComponent} from "../../shared/components/loading-bar/loading-bar.component";
import {StudyUserRight} from "../../studies/shared/study-user-right.enum";
import {StudyRightsService} from "../../studies/shared/study-rights.service";


@Component({
    selector: 'dataset-acquisition',
    templateUrl: 'dataset-acquisition.component.html',
    styleUrls: ['dataset-acquisition.component.css']
})
export class DatasetAcquisitionComponent extends EntityComponent<DatasetAcquisition> {

    @ViewChild('progressBar') progressBar: LoadingBarComponent;

    public studyCards: StudyCard[];
    public acquisitionEquipments: AcquisitionEquipment[];
    acquisitionNode: DatasetAcquisition | DatasetAcquisitionNode;
    hasDownloadRight: boolean = false;
    datasetIdsLoaded: boolean = false;
    noDatasets: boolean = false;
    hasEEG: boolean = false;
    hasDicom: boolean = false;
    hasBids: boolean = false;

    constructor(
            private route: ActivatedRoute,
            private datasetService: DatasetService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private studyCardService: StudyCardService,
            private acqEqService: AcquisitionEquipmentService,
            private studyRightsService: StudyRightsService,
            public acqEqPipe: AcquisitionEquipmentPipe) {
        super(route, 'dataset-acquisition');
    }

    getService(): EntityService<DatasetAcquisition> {
        return this.datasetAcquisitionService;
    }

    get datasetAcquisition(): DatasetAcquisition { return this.entity; }
    set datasetAcquisition(datasetAcquisition: DatasetAcquisition) {
        this.acquisitionNode = this.breadcrumbsService.currentStep.data.datasetAcquisitionNode ? this.breadcrumbsService.currentStep.data.datasetAcquisitionNode : datasetAcquisition;
        this.entity = datasetAcquisition;
    }

    initView(): Promise<void> {
        return this.datasetAcquisitionService.get(this.id).then(dsAcq => {
            this.datasetAcquisition = dsAcq;
            this.fetchDatasetIdsFromTree() ;

            if (this.keycloakService.isUserAdmin()) {
                this.hasDownloadRight = true;
                return;
            } else {
                return this.studyRightsService.getMyRightsForStudy(dsAcq.examination.study.id).then(rights => {
                    this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
                });
            }

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

    buildForm(): UntypedFormGroup {
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

    download(format: string) {
        this.datasetService.downloadDatasetsByExamination(this.id, format, this.progressBar);
    }


    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert(); // TODO
    }

    onNodeInit(node: DatasetAcquisitionNode) {
        node.open = true;
        this.breadcrumbsService.currentStep.data.datasetAcquisitionNode = node;
    }

    fetchDatasetIdsFromTree() {
        if (!this.datasetIdsLoaded) {
            let node: DatasetAcquisitionNode = this.breadcrumbsService.currentStep.data.acquisitionNode;
            let found: boolean = false;
            if (node && node.datasets != 'UNLOADED') {
                found = true;
                node.datasets.forEach(ds => {
                    console.log("FOUND !")
                    this.noDatasets = false;
                    if (ds.type == 'Eeg') {
                        this.hasEEG = true;
                    } else if (ds.type == 'BIDS') {
                        this.hasBids = true;
                    } else {
                        this.hasDicom = true;
                    }
                });
            }
            if (found) {
                console.log("FOUND !")
                this.datasetIdsLoaded = true;
            }
        }
    }
}
