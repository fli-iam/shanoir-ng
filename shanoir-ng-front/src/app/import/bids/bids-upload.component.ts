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
import { StudyService } from '../../studies/shared/study.service';

type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'bids-upload',
    templateUrl: 'bids-upload.component.html',
    styleUrls: ['bids-upload.component.css'],
    animations: [slideDown]
})
export class BidsUploadComponent {
    
    public archiveStatus: Status = 'none';
    protected extensionError: boolean;
    private modality: string;
    public errorMessage: string;

    constructor(
            private importService: ImportService, 
            private bidsArchiveService: DicomArchiveService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService,
            private studyService: StudyService) {
        
        breadcrumbsService.nameStep('1. Upload');
        breadcrumbsService.markMilestone();
    }

    public uploadArchive(fileEvent: any): void {
        this.setArchiveStatus('uploading');
        this.loadInMemory(fileEvent);   
        this.uploadToServer(fileEvent.target.files);
    }

    private loadInMemory(fileEvent: any) {
    	this.bidsArchiveService.clearFileInMemory();
    	this.bidsArchiveService.importFromZip((fileEvent.target).files[0])
            .subscribe(_ => {
                this.bidsArchiveService.extractFileDirectoryStructure()
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
        this.importService.uploadBidsFile(formData)
            .then((importJob: ImportJob) => {
                this.setArchiveStatus('uploaded');
                this.errorMessage = "";
            }).catch(error => {
                this.setArchiveStatus('error');
                if (error && error.error && error.error.message) {
                        this.errorMessage = error.error.message;
                    }
            });
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    data = {'frontStudyId': 10,'studyCardId': 68,'frontAcquisitionEquipmentId': '1','centerId': '1','patients': [{'patientID':'BidsCreated','studies' : [ {'series': [{'images': [{'path':'pathToDicomImage'}]}]}]}]};
}
    