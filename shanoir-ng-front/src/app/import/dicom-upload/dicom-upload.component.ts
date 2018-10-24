import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { DicomArchiveService } from '../dicom-archive.service';
import { ImportJob } from '../dicom-data.model';
import { ImportService } from '../import.service';


type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'dicom-upload',
    templateUrl: 'dicom-upload.component.html',
    styleUrls: ['dicom-upload.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class DicomUploadComponent implements OnInit {
    
    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private dicomDirMissingError: boolean;
    private modality: string;
    private step: Step;
    private readonly ImagesUrlUtil = ImagesUrlUtil;


    constructor(
            private importService: ImportService, 
            private dicomArchiveService: DicomArchiveService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService) {
        
        breadcrumbsService.nameStep('Import : Upload');
    }
    
    ngOnInit() {
        this.step = this.breadcrumbsService.currentStep;
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
                    this.step.data.inMemoryExtracted = response;
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
            .subscribe((patientDicomList: ImportJob) => {
                this.modality = patientDicomList.patients[0].studies[0].series[0].modality.toString();
                this.step.data.archiveUploaded = patientDicomList;
                this.setArchiveStatus('uploaded');
            }, (err: String) => {
                this.setArchiveStatus('error');
                this.dicomDirMissingError = err.indexOf("DICOMDIR is missing") != -1
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