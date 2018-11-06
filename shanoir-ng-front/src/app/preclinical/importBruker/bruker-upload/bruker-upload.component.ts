import { Component, Output, EventEmitter, Input } from '@angular/core';
import { ImportJob } from '../../../import/dicom-data.model';
import { DicomArchiveService } from '../../../import/dicom-archive.service';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';
import { slideDown } from '../../../shared/animations/animations';
import { ImportService } from '../../../import/import.service';
import { Router } from '../../../breadcrumbs/router';
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { ImportDataService } from '../../../import/import.data-service';
import { ImportBrukerService } from '../importBruker.service';


type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'bruker-upload',
    templateUrl: 'bruker-upload.component.html',
    styleUrls: ['bruker-upload.component.css', '../../..//import/import.step.css'],
    animations: [slideDown]
})
export class BrukerUploadComponent {

    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private dicomDirMissingError: boolean;
    private modality: string;
    
    private readonly ImagesUrlUtil = ImagesUrlUtil;
    
    public archive: string;
    fileToUpload: File = null;
    uploadProgress: number = 0;


    constructor(
            private importService: ImportService, 
            private dicomArchiveService: DicomArchiveService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService, 
            private importBrukerService: ImportBrukerService) {
        
        breadcrumbsService.nameStep('1. Upload');
        breadcrumbsService.markMilestone();
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
    			this.importBrukerService.importDicomFile(res)
            		.subscribe((patientDicomList: ImportJob) => {
                		this.modality = patientDicomList.patients[0].studies[0].series[0].modality.toString();
                        this.importDataService.archiveUploaded = patientDicomList;
                        this.setArchiveStatus('uploaded');
                		this.uploadProgress = 5;
            		}, (err: String) => {
            			console.log("error in dicom import"+JSON.stringify(err));
                        this.dicomDirMissingError = (JSON.stringify(err)).indexOf("DICOMDIR is missing") != -1
                        this.uploadProgress = 4;
                        this.setArchiveStatus('error');
            	});
            
                }, 
                (err: String) => {
                	console.log('error in posting File ');
                	console.log(JSON.stringify(err));
                    this.archive = '';
                    this.uploadProgress = 2;
                	this.setArchiveStatus('error');
                }
            );
    }

     private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
        //this.updateValidity();
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    private next() {
        this.router.navigate(['importsBruker/series']);
    }

}