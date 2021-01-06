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

import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { Router } from '../../../breadcrumbs/router';
import { ImportJob } from '../../../import/shared/dicom-data.model';
import { ImportDataService } from '../../../import/shared/import.data-service';
import { slideDown } from '../../../shared/animations/animations';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';
import { ImportBrukerService } from '../importBruker.service';



type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'bruker-upload',
    templateUrl: 'bruker-upload.component.html',
    styleUrls: ['bruker-upload.component.css', '../../..//import/shared/import.step.css'],
    animations: [slideDown]
})
export class BrukerUploadComponent {
    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private dicomDirMissingError: boolean;
    private modality: string;
    
    private readonly ImagesUrlUtil = ImagesUrlUtil;
    
    protected archive: string;
    protected archiveFolder: string;
    fileToUpload: File = null;
    uploadProgress: number = 0;


    constructor(
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService, 
            private importBrukerService: ImportBrukerService) {
        
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Upload';
        });
        breadcrumbsService.currentStep.importStart = true;
        breadcrumbsService.currentStep.importMode = 'BRUKER';
    }
    
    
    private uploadArchive(fileEvent: any): void {
        this.setArchiveStatus('uploading');
        this.uploadBruker(fileEvent);   
    }

    
    
    private uploadBruker(fileEvent: any): void {
        this.dicomDirMissingError = false;
        this.uploadProgress = 0;
    	// checkExtension
    	this.extensionError = false;
    	let file:any = fileEvent.target.files;
        let index:any = file[0].name.lastIndexOf(".");
        let strsubstring: any = file[0].name.substring(index, file[0].name.length);
        if (strsubstring != '.zip') {
            this.extensionError = true;
            return;
        } 
        this.fileToUpload = file.item(0);
    	this.uploadProgress = 1;
    	this.importBrukerService.postFile(this.fileToUpload)
        	.subscribe(res => {
    			this.archive = this.fileToUpload.name;
    			this.uploadProgress = 3;
                        this.archiveFolder = res.substring(res.indexOf(".") + 1, res.indexOf(".converted.zip"));
    			this.importBrukerService.importDicomFile(res)
            		.subscribe((patientDicomList: ImportJob) => {
                		this.modality = patientDicomList.patients[0].studies[0].series[0].modality.toString();
                        this.importDataService.archiveUploaded = patientDicomList;
                        this.importDataService.patientList = patientDicomList;
                        this.setArchiveStatus('uploaded');
                		this.uploadProgress = 5;
            		}, (err: String) => {
                        this.dicomDirMissingError = (JSON.stringify(err)).indexOf("DICOMDIR is missing") != -1
                        this.uploadProgress = 4;
                        this.setArchiveStatus('error');
            	});
            
                }, 
                (err: String) => {
                    this.archive = '';
                    this.uploadProgress = 2;
                	this.setArchiveStatus('error');
                }
            );
    }

    protected storeArchiveChanged(event: boolean) {
        // Get the name of the file to get
        if (event) {
            let archiveFileName = this.archive.substr(0, this.archive.lastIndexOf('.'));
            let archiveName  = '/tmp/bruker/convert/' + archiveFileName + '/' + this.archiveFolder + '/' + this.archive;
            this.importDataService.archiveUploaded.archive = archiveName;
        } else {
            this.importDataService.archiveUploaded.archive = undefined;
        }
    }

     private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    private next() {
        this.router.navigate(['imports/brukerseries']);
    }

}