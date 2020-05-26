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
import { Router } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { DicomArchiveService } from '../shared/dicom-archive.service';
import { ImportJob } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';

type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'dicom-upload',
    templateUrl: 'dicom-upload.component.html',
    styleUrls: ['dicom-upload.component.css', '../shared/import.step.css'],
    animations: [slideDown]
})
export class DicomUploadComponent {
    
    archiveStatus: Status = 'none';
    extensionError: boolean;
    dicomDirMissingError: boolean;
    modality: string;


    constructor(
            private importService: ImportService, 
            private dicomArchiveService: DicomArchiveService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
        
        breadcrumbsService.nameStep('1. Upload');
        breadcrumbsService.markMilestone();
    }
    
    uploadArchive(fileEvent: any): void {
        this.setArchiveStatus('uploading');
        this.loadInMemory(fileEvent);   
        this.uploadToServer(fileEvent.target.files);
    }

    private loadInMemory(fileEvent: any) {
    	this.dicomArchiveService.clearFileInMemory();
    	this.dicomArchiveService.importFromZip((fileEvent.target).files[0])
            .subscribe(_ => {
                this.dicomArchiveService.extractFileDirectoryStructure()
                .subscribe(response => {
                    this.importDataService.inMemoryExtracted = response;
                });
            });
    }

    private uploadToServer(file: any) {
        this.dicomDirMissingError = false;
        this.extensionError = file[0].name.substring(file[0].name.lastIndexOf("."), file[0].name.length) != '.zip';

        this.modality = null;
        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.importService.uploadFile(formData)
            .then((patientDicomList: ImportJob) => {
                this.modality = patientDicomList.patients[0].studies[0].series[0].modality.toString();
                this.importDataService.patientList = patientDicomList;
                this.setArchiveStatus('uploaded');
            }).catch(error => {
                this.setArchiveStatus('error');
                if (error && error.error && error.error.message) 
                    this.dicomDirMissingError = error.error.message.indexOf("DICOMDIR is missing") != -1
            });
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    next() {
        this.router.navigate(['imports/series']);
    }

}