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
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TaskState } from 'src/app/async-tasks/task.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { Selection } from 'src/app/studies/study/tree.service';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipmentService } from '../../acquisition-equipments/shared/acquisition-equipment.service';
import { DatasetService } from "../../datasets/shared/dataset.service";
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { StudyRightsService } from "../../studies/shared/study-rights.service";
import { StudyUserRight } from "../../studies/shared/study-user-right.enum";
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { StudyCardService } from '../../study-cards/shared/study-card.service';
import { MrDatasetAcquisition } from '../modality/mr/mr-dataset-acquisition.model';
import { DatasetAcquisition } from '../shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from '../shared/dataset-acquisition.service';

import { FormFooterComponent } from '../../shared/components/form-footer/form-footer.component';
import { SelectBoxComponent } from '../../shared/select/select.component';
import { MrProtocolComponent } from '../modality/mr/mr-protocol.component';
import { CtProtocolComponent } from '../modality/ct/ct-protocol.component';
import { PetProtocolComponent } from '../modality/pet/pet-protocol.component';
import { XaProtocolComponent } from '../modality/xa/xa-protocol.component';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { LocalDateFormatPipe } from '../../shared/localLanguage/localDateFormat.pipe';

@Component({
    selector: 'dataset-acquisition-detail',
    templateUrl: 'dataset-acquisition.component.html',
    styleUrls: ['dataset-acquisition.component.css'],
    imports: [FormsModule, ReactiveFormsModule, FormFooterComponent, SelectBoxComponent, RouterLink, MrProtocolComponent, CtProtocolComponent, PetProtocolComponent, XaProtocolComponent, LoadingBarComponent, LocalDateFormatPipe, AcquisitionEquipmentPipe]
})
export class DatasetAcquisitionComponent extends EntityComponent<DatasetAcquisition> {

    public studyCards: StudyCard[];
    public acquisitionEquipments: AcquisitionEquipment[];
    hasDownloadRight: boolean = false;
    noDatasets: boolean = false;
    hasDicom: boolean = false;
    protected downloadState: TaskState = new TaskState();

    constructor(
            private route: ActivatedRoute,
            private datasetService: DatasetService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private studyCardService: StudyCardService,
            private acqEqService: AcquisitionEquipmentService,
            private studyRightsService: StudyRightsService,
            public acqEqPipe: AcquisitionEquipmentPipe,
            private downloadService: MassDownloadService) {
        super(route, 'dataset-acquisition');
    }

    getService(): EntityService<DatasetAcquisition> {
        return this.datasetAcquisitionService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromAcquisition(this.datasetAcquisition);
    }

    get datasetAcquisition(): DatasetAcquisition { return this.entity; }
    set datasetAcquisition(datasetAcquisition: DatasetAcquisition) {
        this.entity = datasetAcquisition;
    }

    initView(): Promise<void> {
        this.datasetService.getByAcquisitionId(this.datasetAcquisition.id).then(datasets => {
            this.datasetAcquisition.datasets = datasets;
            this.datasetAcquisition.datasets?.forEach(ds => {
                this.noDatasets = false;
                if (ds.type != 'Eeg' && ds.type != 'BIDS') {
                    this.hasDicom = true;
                }
            });
        })
        if (this.keycloakService.isUserAdmin()) {
            this.hasDownloadRight = true;
            return Promise.resolve();
        } else {
            return this.studyRightsService.getMyRightsForStudy(this.datasetAcquisition.examination.study.id).then(rights => {
                this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
            });
        }
    }

    initEdit(): Promise<void> {
        this.studyCardService.getAll().then(scs => this.studyCards = scs);
        this.acqEqService.getAll().then(aes => this.acquisitionEquipments = aes);
        return Promise.resolve();
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
            'studyCard': [this.datasetAcquisition.studyCard],
            'acquisitionEquipment': [this.datasetAcquisition.acquisitionEquipment, [Validators.required]],
            'rank': [this.datasetAcquisition.rank],
            'acquisitionStartTime': [this.datasetAcquisition.acquisitionStartTime],
            'softwareRelease': [this.datasetAcquisition.softwareRelease],
            'sortingIndex': [this.datasetAcquisition.sortingIndex],
            'protocol': [this.datasetAcquisition.protocol]
        });
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert(); // TODO
    }

    downloadAll() {
        this.downloadService.downloadAllByAcquisitionId(this.datasetAcquisition?.id, this.downloadState);
    }
}
