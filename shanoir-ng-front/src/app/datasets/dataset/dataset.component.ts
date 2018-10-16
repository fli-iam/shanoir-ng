import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DicomArchiveService } from '../../import/dicom-archive.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Dataset, DatasetMetadata } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';

@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent implements OnInit {

    private mode: 'create' | 'edit' | 'view';
    private id: number;
    private dataset: Dataset;
    private canModify: Boolean = false;
    private papayaParams: any;
    private blob: Blob;
    private filename: string;
    
    constructor(
            private datasetService: DatasetService,
            private route: ActivatedRoute,
            private location: Location,
            private keycloakService: KeycloakService,
            private router: Router,
            private msgService: MsgBoxService,
            private dicomArchiveService: DicomArchiveService) {

        this.mode = this.route.snapshot.data['mode'];
        this.id = +this.route.snapshot.params['id'];        
    }

    ngOnInit(): void {
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
        this.fetchDataset();
        this.loadDicomInMemory();
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
    
    private back(): void {
        this.location.back();
    }

    private edit(): void {
        this.router.navigate(['/dataset/edit/' + this.dataset.id])
    }

    private update(): void {
        this.datasetService.update(this.dataset).subscribe((dataset) => {
            this.router.navigate(['/dataset/details/' + dataset.id])
                .then(() => {
                    this.msgService.log('info', 'Dataset n°' + this.dataset.id + ' has been updated');
                });
        });
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