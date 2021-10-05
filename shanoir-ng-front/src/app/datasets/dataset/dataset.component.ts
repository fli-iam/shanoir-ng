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
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DicomArchiveService } from '../../import/shared/dicom-archive.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Dataset, DatasetMetadata } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { NiftiConverter } from 'src/app/niftiConverters/nifti.converter.model';
import { NiftiConverterService } from 'src/app/niftiConverters/nifti.converter.service';
import { MrDataset, MrDatasetMetadata } from './mr/dataset.mr.model';


@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent extends EntityComponent<Dataset> {

    papayaParams: any;
    private blob: Blob;
    private filename: string;
    hasDownloadRight: boolean = false;
    private hasAdministrateRight: boolean = false;
    public downloading: boolean = false;
    public papayaLoaded: boolean = false;
    public converters: NiftiConverter[];
    public converterId: number;
    public menuOpened = false;
    isMRS: boolean = false; // MR Spectroscopy

    constructor(
        private datasetService: DatasetService,
        private route: ActivatedRoute,
        private dicomArchiveService: DicomArchiveService,
        private studyRightsService: StudyRightsService,
        private niftiConverterService: NiftiConverterService) {
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
        });
    }

    initCreate(): Promise<void> {
        throw new Error('Cannot create Dataset!');
    }

    buildForm(): FormGroup {
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
        this.downloading = true;
        this.datasetService.download(this.dataset, 'nii', id).then(() => this.downloading = false);
    }

    download(format: string) {
        this.downloading = true;
        this.datasetService.download(this.dataset, format).then(() => this.downloading = false);
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
}