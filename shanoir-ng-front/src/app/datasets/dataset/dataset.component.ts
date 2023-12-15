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
import { UntypedFormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { NiftiConverter } from 'src/app/niftiConverters/nifti.converter.model';
import { NiftiConverterService } from 'src/app/niftiConverters/nifti.converter.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { DownloadSetupOptions } from 'src/app/shared/mass-download/download-setup/download-setup.component';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { DicomArchiveService } from '../../import/shared/dicom-archive.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Dataset, DatasetMetadata } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { MrDataset } from './mr/dataset.mr.model';
import { TaskState, TaskStatus } from 'src/app/async-tasks/task.model';


@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent extends EntityComponent<Dataset> {

    papayaParams: any;
    hasDownloadRight: boolean = false;
    private hasAdministrateRight: boolean = false;
    public downloadState: TaskState = new TaskState();
    public papayaLoaded: boolean = false;
    public converters: NiftiConverter[];
    public converterId: number;
    public menuOpened = false;
    isMRS: boolean = false; // MR Spectroscopy

    constructor(
        private datasetService: DatasetService,
        route: ActivatedRoute,
        private dicomArchiveService: DicomArchiveService,
        private studyRightsService: StudyRightsService,
        niftiConverterService: NiftiConverterService,
        private downloadService: MassDownloadService) {
        super(route, 'dataset');
        niftiConverterService.getAll().then(result => this.converters = result);
    }

    get dataset(): Dataset { return this.entity; }
    set dataset(dataset: Dataset) { this.entity = dataset; }

    getService(): EntityService<Dataset> {
        return this.datasetService;
    }

    initView(): Promise<void> {
        return this.fetchDataset().then(dataset => {
            this.dataset = dataset;
            this.isMRS = this.isSpectro(dataset);
            if (this.keycloakService.isUserAdmin()) {
                this.hasAdministrateRight = true;
                this.hasDownloadRight = true;
                return;
            } else {
                return this.studyRightsService.getMyRightsForStudy(dataset.study.id).then(rights => {
                    this.hasAdministrateRight = rights.includes(StudyUserRight.CAN_ADMINISTRATE);
                    this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
                });
            }
        });
    }

    private isSpectro(dataset: Dataset): boolean {
        if (dataset.type != 'Mr') return false;
        else {
            const mrDataset = dataset as MrDataset;
            if (mrDataset.updatedMrMetadata && mrDataset.updatedMrMetadata.mrDatasetNature) {
                return mrDataset.updatedMrMetadata.mrDatasetNature.includes('SPECTRO');
            } else if (mrDataset.originMrMetadata && mrDataset.originMrMetadata.mrDatasetNature) {
                return mrDataset.originMrMetadata.mrDatasetNature.includes('SPECTRO');
            } else {
                return false;
            }
        }
    }

    initEdit(): Promise<void> {
        return this.fetchDataset().then(dataset => {
            this.dataset = dataset;
            this.dataset.creationDate = new Date(this.dataset.creationDate);
        });
    }

    initCreate(): Promise<void> {
        throw new Error('Cannot create Dataset!');
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({});
    }

    private fetchDataset(): Promise<Dataset> {
        if (this.mode != 'create') {
            return this.datasetService.get(this.id).then((dataset: Dataset) => {
                if (!dataset.updatedMetadata) dataset.updatedMetadata = new DatasetMetadata();
                return dataset;
            });
        }
    }

    toggleMenu() {
        this.menuOpened = !this.menuOpened;
    }

    convertNiftiToggle() {
        this.toggleMenu();
    }

    convertNifti(id: number) {
        this.downloadState.status = TaskStatus.IN_PROGRESS;
        this.datasetService.download(this.dataset, 'nii', id).then(() => this.downloadState.status = TaskStatus.IN_PROGRESS);
    }

    downloadAll() {
        let options: DownloadSetupOptions = new DownloadSetupOptions();
        options.hasBids = this.dataset.type == 'BIDS';
        options.hasDicom = this.dataset.type != 'Eeg' && this.dataset.type != 'BIDS' && !this.dataset.datasetProcessing;
        options.hasNii = !this.isMRS && this.dataset.type != 'Eeg' && this.dataset.type != 'BIDS' && this.dataset.type != 'Measurement' && !this.dataset.datasetProcessing;
        options.hasEeg = this.dataset.type == 'Eeg' && !this.dataset.datasetProcessing;
        this.downloadService.downloadDataset(this.dataset?.id, options, this.downloadState);
    }

    public loadDicomInMemory() {
        this.papayaLoaded = true;
        this.datasetService.downloadToBlob(this.id, 'nii').then(blobReponse => {
            this.dicomArchiveService.clearFileInMemory();
            this.dicomArchiveService.importFromZip(blobReponse.body)
                .then(response => {
                    this.dicomArchiveService.extractFileDirectoryStructure()
                        .then(response => {
                            this.initPapaya(response);
                        });
                });
        });
    }

    private initPapaya(dataFiles: any): void {
        let buffs = [];
        Object.keys(dataFiles.files).forEach((key) => {
            if (key.indexOf(".nii") != -1) {
                buffs.push(dataFiles.files[key].async("arraybuffer"));
            }
        });
        let promiseOfList = Promise.all(buffs);
        promiseOfList.then((values) => {
            let params: object[] = [];
            params['binaryImages'] = [values];
            this.papayaParams = params;
        });
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdmin() || this.hasAdministrateRight;
    }

    public async hasDeleteRight(): Promise<boolean> {
        return this.keycloakService.isUserAdmin() || this.hasAdministrateRight;
    }

    downloadMetadata() {
        this.datasetService.downloadDicomMetadata(this.dataset.id);
    }

    seeDicomMetadata() {
        this.router.navigate(['/dataset/details/dicom/' + this.dataset.id]);
    }

    goToList(): void {
        this.router.navigate(['/solr-search']);
    }

}
