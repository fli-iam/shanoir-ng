import { Component, Output, EventEmitter, Input } from '@angular/core';
import { ImportJob } from '../../../import/dicom-data.model';
import { ImportBrukerService } from '../importBruker.service';
import { DicomArchiveService } from '../../../import/dicom-archive.service';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';
import { AbstractImportStepComponent } from '../../../import/import-step.abstract';
import { slideDown } from '../../../shared/animations/animations';


type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'bruker-upload',
    templateUrl: 'bruker-upload.component.html',
    styleUrls: ['bruker-upload.component.css', '../../..//import/import.step.css'],
    animations: [slideDown]
})
export class BrukerUploadComponent extends AbstractImportStepComponent {

    @Output() inMemoryExtracted = new EventEmitter<any>();
    @Output() archiveUploaded = new EventEmitter<ImportJob>();
    
    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private dicomDirMissingError: boolean;
    private modality: string;
    private readonly ImagesUrlUtil = ImagesUrlUtil;
    
    public archive: string;
    fileToUpload: File = null;
    uploadProgress: number = 0;


    constructor(private importBrukerService: ImportBrukerService, private dicomArchiveService: DicomArchiveService) {
        super();
    }
    
    private uploadArchive(fileEvent: any): void {
        this.setArchiveStatus('none');
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
                		this.archiveUploaded.emit(patientDicomList);
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
        this.updateValidity();
    }

    getValidity(): boolean {
        return this.archiveStatus == 'uploaded';
    }

}