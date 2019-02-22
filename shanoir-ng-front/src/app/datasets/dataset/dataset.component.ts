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
import { DatepickerComponent } from '../../shared/date/date.component';

import { DicomArchiveService } from '../../import/dicom-archive.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Dataset, DatasetMetadata } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';

@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent extends EntityComponent<Dataset> {

    private papayaParams: any;
    private blob: Blob;
    private filename: string;
    
    constructor(
            private datasetService: DatasetService,
            private route: ActivatedRoute,
            private dicomArchiveService: DicomArchiveService) {

        super(route, 'dataset');
    }

    get dataset(): Dataset { return this.entity; }
    set dataset(dataset: Dataset) { this.entity = dataset; }

    ngOnInit(): void {
        super.ngOnInit();
        this.loadDicomInMemory();
    }

    initView(): Promise<void> {
        return this.fetchDataset().then(() => null);
    }

    initEdit(): Promise<void> {
        return this.fetchDataset().then(() => null);
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
                this.dataset = dataset;
                return dataset;
            });
        }
    }
    
    private download(format: string) {
        this.datasetService.download(this.dataset, format);
    }

    private loadDicomInMemory() {
        this.datasetService.downloadToBlob(this.id, 'dcm').subscribe(blobReponse => {
            this.dicomArchiveService.clearFileInMemory();
            this.dicomArchiveService.importFromZip(blobReponse.body)
                .subscribe(response => {
                    this.dicomArchiveService.extractFileDirectoryStructure()
                    .subscribe(response => {
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

}