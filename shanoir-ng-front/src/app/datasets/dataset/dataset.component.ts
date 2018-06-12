import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { DatasetService } from '../shared/dataset.service';
import { Dataset } from '../shared/dataset.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { SerieDicom } from '../../import/dicom-data.model';
import { DicomArchiveService } from '../../import/dicom-archive.service';
import { HttpResponse } from '@angular/common/http';

@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent implements OnInit {

    @Input() private mode: 'create' | 'edit' | 'view';
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
            private dicomArchiveService: DicomArchiveService) {}

    ngOnInit(): void {
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
        this.fetchMode().then(() => {
            this.fetchDataset();
        });
        this.loadDicomInMemory();
    }

    private fetchMode(): Promise<void> {
        return new Promise((resolve) => { 
            this.route.queryParams
                .subscribe((queryParams: Params) => {
                    if (this.mode = queryParams['mode']) this.mode = queryParams['mode'];
                    else this.mode = 'create';
                    resolve();
                });
        });
    }

    private fetchDataset(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let id = queryParams['id'];
                if (id) {
                    if (this.mode == 'create') throw Error('Cannot be in create mode and have an id');
                    return this.datasetService.get(id);
                } else {
                    if (this.mode != 'create') throw Error('Cannot be in '+this.mode+' mode without an id');
                    return Observable.of<Dataset>();
                }
            })
            .subscribe((dataset: Dataset) => {
                this.dataset = dataset;
            });
    }
    
    private back(): void {
        this.location.back();
    }

    private edit(): void {
        this.router.navigate([], {
            relativeTo: this.route, 
            queryParams: { id: this.dataset.id, mode: "edit" }
        });
    }

    private update(): void {
        this.datasetService.update(this.dataset).subscribe((dataset) => {
            this.msgService.log('info', 'Dataset updated');
        });
    }

    private download() {
        this.datasetService.download(this.dataset);
    }

    private loadDicomInMemory() {
        this.route.queryParams
            .filter(params => 'id' in params)
            .map(params => params.id)
            .distinctUntilChanged()
            .subscribe((id: number) => {
                this.datasetService.downloadToBlob(id).subscribe(blobReponse => {
                    this.dicomArchiveService.clearFileInMemory();
                    this.dicomArchiveService.importFromZip(blobReponse.body)
                        .subscribe(response => {
                            this.dicomArchiveService.extractFileDirectoryStructure()
                            .subscribe(response => {
                                this.initPapaya(response);
                            });
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

    // getFormValidationErrors() {
    //     Object.keys(this.subjectForm.controls).forEach(key => {
    //         const controlErrors: ValidationErrors = this.subjectForm.get(key).errors;
    //         if (controlErrors != null) {
    //             Object.keys(controlErrors).forEach(keyError => {
    //                 console.log('Key control: ' + key + ', keyError: ' + keyError + ', err value: ', controlErrors[keyError]);
    //             });
    //         }
    //     });
    // }

}