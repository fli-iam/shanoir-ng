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
import { EegImportJob } from '../shared/eeg-data.model';

type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'eeg-upload',
    templateUrl: 'eeg-upload.component.html',
    styleUrls: ['eeg-upload.component.css', '../shared/import.step.css'],
    animations: [slideDown]
})
export class EegUploadComponent {
    
    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private modality: string;


    constructor(
            private importService: ImportService, 
            private eegArchiveService: DicomArchiveService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
        
        breadcrumbsService.nameStep('1. Upload');
        breadcrumbsService.markMilestone();
    }

    private uploadArchive(fileEvent: any): void {
        this.setArchiveStatus('uploading');
        this.loadInMemory(fileEvent);   
        this.uploadToServer(fileEvent.target.files);
    }

    private loadInMemory(fileEvent: any) {
    	this.eegArchiveService.clearFileInMemory();
    	this.eegArchiveService.importFromZip((fileEvent.target).files[0])
            .subscribe(_ => {
                this.eegArchiveService.extractFileDirectoryStructure()
                .subscribe(response => {
                    this.importDataService.inMemoryExtracted = response;
                });
            });
    }

    private uploadToServer(file: any) {
        this.extensionError = file[0].name.substring(file[0].name.lastIndexOf("."), file[0].name.length) != '.zip';

        this.modality = null;
        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.importService.uploadEegFile(formData)
            .then((importJob: EegImportJob) => {
                this.importDataService.eegImportJob = importJob;
                this.setArchiveStatus('uploaded');
            }).catch(error => {
                this.setArchiveStatus('error');
            });
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    private next() {
        this.router.navigate(['imports/eegcontext']);
    }

}