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
    
    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private dicomDirMissingError: boolean;
    private modality: string;


    constructor(
            private importService: ImportService, 
            private dicomArchiveService: DicomArchiveService,
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
                this.importDataService.archiveUploaded = patientDicomList;
                this.setArchiveStatus('uploaded');
            }).catch(error => {
                this.setArchiveStatus('error');
                if (error && error.error && error.error.message) 
                    this.dicomDirMissingError = error.error.message.indexOf("DICOMDIR is missing") != -1
            });
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
        //this.updateValidity();
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    private next() {
        this.router.navigate(['imports/series']);
    }

}