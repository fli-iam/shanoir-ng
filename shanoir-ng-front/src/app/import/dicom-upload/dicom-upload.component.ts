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

import { HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { Subscription } from 'rxjs';
import { OnDestroy } from '@angular/core';


type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'dicom-upload',
    templateUrl: 'dicom-upload.component.html',
    styleUrls: ['dicom-upload.component.css', '../shared/import.step.css'],
    animations: [slideDown]
})
export class DicomUploadComponent implements OnDestroy {
    subscribtions: Subscription[] = [];
    archiveStatus: Status = 'none';
    extensionError: boolean;
    dicomDirMissingError: boolean;
    fileTooBigError: boolean;
    modality: string;
    @ViewChild('progressBar') progressBar: LoadingBarComponent;


    constructor(
            private importService: ImportService, 
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
        
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Upload';
            breadcrumbsService.currentStep.importStart = true;
            breadcrumbsService.currentStep.importMode = 'DICOM';
        });
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
        this.dicomDirMissingError = false;
        this.fileTooBigError = false;
        this.extensionError = file[0].name.substring(file[0].name.lastIndexOf("."), file[0].name.length) != '.zip';

        this.modality = null;
        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.subscribtions.push(
        this.importService.uploadFile(formData)
            .subscribe(
                event => {
                if (event.type === HttpEventType.Sent) {
                    this.progressBar.progress = -1;
                } else if (event.type === HttpEventType.UploadProgress) {
                    this.progressBar.progress = (event.loaded / event.total);
                } else if (event instanceof HttpResponse) {
                    let patientDicomList =  event.body;
                    this.modality = patientDicomList.patients[0].studies[0].series[0].modality.toString();
                    this.importDataService.patientList = patientDicomList;
                    this.setArchiveStatus('uploaded');
                }
            }, error => {
                this.setArchiveStatus('error');
                this.progressBar.progress = 0;
                if (error && error.error && error.error.message)
                    this.dicomDirMissingError = error.error.message.indexOf("DICOMDIR is missing") != -1
                    this.fileTooBigError = error.error.message.indexOf("exceeds the configured maximum") != -1
            })
        );
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }
    
    progressBarFunc(event: HttpEvent<any>, progressBar: LoadingBarComponent): void {
       switch (event.type) {
            case HttpEventType.Sent:
              progressBar.progress = -1;
              break;
            case HttpEventType.UploadProgress:
              break;
            case HttpEventType.Response:
                progressBar.progress = 0;
        }
    }

    next() {
        this.router.navigate(['imports/series']);
    }
    
    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }

}