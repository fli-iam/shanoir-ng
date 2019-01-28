import { Component } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DatepickerComponent } from '../../shared/date/date.component';

import { DicomArchiveService } from '../../import/shared/dicom-archive.service';
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