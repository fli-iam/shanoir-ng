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

import { HttpEventType, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { TaskState } from 'src/app/async-tasks/task.model';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { EegImportJob } from '../shared/eeg-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { UploaderComponent } from '../../shared/components/uploader/uploader.component';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';



type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'eeg-upload',
    templateUrl: 'eeg-upload.component.html',
    styleUrls: ['eeg-upload.component.css', '../shared/import.step.css'],
    animations: [slideDown],
    imports: [UploaderComponent, LoadingBarComponent]
})
export class EegUploadComponent implements OnDestroy {

    public archiveStatus: Status = 'none';
    protected extensionError: boolean;
    private modality: string;
    public errorMessage: string;
    uploadState: TaskState = new TaskState();
    subscriptions: Subscription[] = [];

    constructor(
            private importService: ImportService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Upload';
            breadcrumbsService.currentStep.importStart = true;
            breadcrumbsService.currentStep.importMode = 'EEG';
        });
    }

    ngOnDestroy(): void {
        this.subscriptions?.forEach(sub => sub.unsubscribe());
    }

    public uploadArchive(fileEvent: any): void {
        if (fileEvent.target.files.length > 0) {
            this.setArchiveStatus('uploading');
            this.uploadToServer(fileEvent.target.files);
        } else {
            this.setArchiveStatus('none');
            this.modality = null;
        }
    }

    private uploadToServer(file: any) {
        this.extensionError = file[0].name.substring(file[0].name.lastIndexOf("."), file[0].name.length) != '.zip';

        this.modality = null;
        const formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.subscriptions.push(
            this.importService.uploadEegFile(formData)
                .subscribe(
                    event => {
                    if (event.type === HttpEventType.Sent) {
                        this.uploadState.progress = 0;
                    } else if (event.type === HttpEventType.UploadProgress) {
                        this.uploadState.progress = (event.loaded / (event.total + 0.05));
                    } else if (event instanceof HttpResponse) {
                        this.importDataService.eegImportJob =  event.body;
                        this.errorMessage = "";
                        this.importService.analyseEegFile(this.importDataService.eegImportJob)
                            .then((importJobAnalysed: EegImportJob) => {
                                this.importDataService.eegImportJob = importJobAnalysed;
                                this.setArchiveStatus('uploaded');
                                this.uploadState.progress = 1;
                                this.errorMessage = "";
                            }).catch(error => {
                                this.setArchiveStatus('error');
                                this.uploadState.progress = 0;
                                if (error && error.error && error.error.message) {
                                    this.errorMessage = error.error.message;
                                }
                            });
                    }
                }, error => {
                    this.setArchiveStatus('error');
                    this.uploadState.progress = 0;
                    if (error && error.error && error.error.message) {
                        this.errorMessage = error.error.message;
                    }
                })
        );
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    public next() {
        this.router.navigate(['imports/eegseries']);
    }

}
