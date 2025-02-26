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
import { TaskState } from 'src/app/async-tasks/task.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { DicomArchiveService } from '../../import/shared/dicom-archive.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Dataset, DatasetMetadata } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { MrDataset } from './mr/dataset.mr.model';
import { Selection } from 'src/app/studies/study/tree.service';


@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css'],
    standalone: false
})

export class DatasetComponent extends EntityComponent<Dataset> {

    papayaParams: any;
    hasDownloadRight: boolean = false;
    private hasAdministrateRight: boolean = false;
    public downloadState: TaskState = new TaskState();
    public papayaLoaded: boolean = false;
    isMRS: boolean = false; // MR Spectroscopy

    constructor(
            private datasetService: DatasetService,
            route: ActivatedRoute,
            private dicomArchiveService: DicomArchiveService,
            private studyRightsService: StudyRightsService,
            private downloadService: MassDownloadService) {
        super(route, 'dataset');
    }

    get dataset(): Dataset { return this.entity; }
    set dataset(dataset: Dataset) { this.entity = dataset; }

    getService(): EntityService<Dataset> {
        return this.datasetService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromDataset(this.dataset);
    }

    initView(): Promise<void> {
        this.papayaLoaded = false;
        this.dicomArchiveService.clearFileInMemory();
        if (!this.dataset.updatedMetadata) this.dataset.updatedMetadata = new DatasetMetadata();
        this.isMRS = this.isSpectro(this.dataset);
        if (this.keycloakService.isUserAdmin()) {
            this.hasAdministrateRight = true;
            this.hasDownloadRight = true;
            return;
        } else {
            return this.studyRightsService.getMyRightsForStudy(this.dataset.study.id).then(rights => {
                this.hasAdministrateRight = rights.includes(StudyUserRight.CAN_ADMINISTRATE);
                this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
            });
        }
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
        if (!this.dataset.updatedMetadata) this.dataset.updatedMetadata = new DatasetMetadata();
        this.dataset.creationDate = new Date(this.dataset.creationDate);
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        throw new Error('Cannot create Dataset!');
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({});
    }

    downloadAll() {
        this.downloadService.downloadByIds([this.dataset?.id], this.downloadState);
    }

    public loadDicomInMemory() {
        this.papayaLoaded = true;
        this.datasetService.downloadToBlob(this.id, 'dcm').then(blobReponse => {
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
            buffs.push(dataFiles.files[key].async("arraybuffer"));
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
