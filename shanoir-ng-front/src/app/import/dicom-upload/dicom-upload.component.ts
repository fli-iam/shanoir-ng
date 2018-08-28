import { Component, Output, EventEmitter, Input } from '@angular/core';
import { ImportJob } from '../dicom-data.model';
import { ImportService } from '../import.service';
import { DicomArchiveService } from '../dicom-archive.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { AbstractImportStepComponent } from '../import-step.abstract';
import { slideDown } from '../../shared/animations/animations';


type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'dicom-upload',
    templateUrl: 'dicom-upload.component.html',
    styleUrls: ['dicom-upload.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class DicomUploadComponent extends AbstractImportStepComponent {

    @Output() inMemoryExtracted = new EventEmitter<any>();
    @Output() archiveUploaded = new EventEmitter<ImportJob>();
    
    private archiveStatus: Status = 'none';
    private extensionError: boolean;
    private dicomDirMissingError: boolean;
    private modality: string;
    private readonly ImagesUrlUtil = ImagesUrlUtil;


    constructor(private importService: ImportService, private dicomArchiveService: DicomArchiveService) {
        super();
    }
    
    private uploadArchive(fileEvent: any): void {
        this.setArchiveStatus('uploading');
        this.loadInMemory(fileEvent);   
        this.uploadToServer(fileEvent.target.files);
    }

    private loadInMemory(fileEvent: any) {
    	this.dicomArchiveService.clearFileInMemory();
    	this.dicomArchiveService.importFromZip((fileEvent.target).files[0])
            .subscribe(response => {
                this.dicomArchiveService.extractFileDirectoryStructure()
                .subscribe(response => {
                    this.inMemoryExtracted.emit(response);
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
                this.archiveUploaded.emit(patientDicomList);
                this.setArchiveStatus('uploaded');
            }, (err: String) => {
                this.setArchiveStatus('error');
                this.dicomDirMissingError = err.indexOf("DICOMDIR is missing") != -1
            });
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
        this.updateValidity();
    }

    getValidity(): boolean {
        return this.archiveStatus == 'uploaded';
    }

}