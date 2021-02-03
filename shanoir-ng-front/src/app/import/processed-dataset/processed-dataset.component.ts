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
import { ImportJob } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { ProcessedDatasetImportJob } from '../shared/processed-dataset-data.model';

type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'processed-dataset',
    templateUrl: 'processed-dataset.component.html',
    styleUrls: ['processed-dataset.component.css', '../shared/import.step.css'],
    animations: [slideDown]
})
export class ProcessedDatasetComponent {
    
    archiveStatus: Status = 'none';
    extensionError: boolean;
    public errorMessage: string;

    constructor(
            private importService: ImportService, 
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
        
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Upload';
        });
        breadcrumbsService.currentStep.importStart = true;
    }
    
    public uploadArchive(fileEvent: any): void {
        if (fileEvent.target.files.length > 0) {
            this.setArchiveStatus('uploading');
            this.uploadToServer(fileEvent.target.files);
        } else {
            this.setArchiveStatus('none');
        }
    }

    private uploadToServer(files: any) {
        // Blindly initialize the image and header files, we will fix that when checking extensions
        let image = files[0];
        let header = files[1];

        // Extract extensions of selected files to check if they are valid
        let extensions = [];
        for(let file of files) {
            extensions.push(file.name.substring(file.name.lastIndexOf("."), file.name.length));
        }
        if(files.length == 1) {                     // Nifti files
            this.extensionError = extensions[0] != '.nii' || extensions[0] != '.nii.gz';
        }
        else if(files.length == 2) {                // Analyze files: .hdr + .img
            if(extensions.indexOf('.hdr') < 0 || extensions.indexOf('.img') < 0) {
                this.extensionError = true;
                return;
            }
            // Reorder files so that the header is after the image
            if(extensions.indexOf('.hdr') == 0) {
                image = files[1];
                header = files[0];
            }
        } else {
            this.extensionError = true;
            return;
        }

        let formData: FormData = new FormData();
        
        formData.append('image', image, image.name);
        formData.append('header', header, header.name);
        this.importService.uploadProcessedDataset(formData)
            .then((filePath: string) => {
                let importJob = new ProcessedDatasetImportJob()
                importJob.processedDatasetFilePath = filePath;
                this.importDataService.processedDatasetImportJob = importJob;
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

    next() {
        this.router.navigate(['imports/processed-dataset-context']);
    }

}