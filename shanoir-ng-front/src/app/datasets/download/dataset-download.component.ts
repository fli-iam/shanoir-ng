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

import { Component, ViewChild, Input } from '@angular/core';
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { StudyService } from '../../studies/shared/study.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { MatDialogRef } from '@angular/material/dialog';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';


@Component({
    selector: 'dataset-download',
    templateUrl: 'dataset-download.component.html',
    styleUrls: ['dataset-download.component.css']
})

/** Button to download datasets. 
    Opens a popup stating:
    - Type of download
    - Use bids format
    - nii.gz or .zip */
export class DatasetDownloadComponent {
    
    constructor(private datasetService: DatasetService, private studyService: StudyService) {
        
    }

    @Input() datasetIds: number[] = [];
    @Input() studyId: number;
    protected useBids: boolean = false;
    protected type: 'nii' | 'dcm' = 'nii';
    protected inError: boolean = false;
    protected errorMessage: string;
    protected loading: boolean = false;
    protected readonly ImagesUrlUtil = ImagesUrlUtil;
    protected mode: 'all' | 'selected';

    @ViewChild('downloadDialog') downloadDialog: ModalComponent;

    /** Click on first button */
    prepareDownloadAll() {
        if (!this.studyId) {
            this.inError = true;
            this.errorMessage = 'No datasets available for the current selection.';
        }
        // Display the messageBox with options
        this.mode = 'all';
        this.downloadDialog.show();
    }

    prepareDownloadSelected() {
        if (!this.datasetIds || this.datasetIds.length == 0) {
            this.inError = true;
            this.errorMessage = 'No datasets available for the current selection.';
        }
        // Display the messageBox with options
        this.mode = 'selected';
        this.downloadDialog.show();
    }

    /** Download the data */
    public download() {
        // Call service method to download datasets
        this.loading = true;
        if (this.mode == 'selected') {
            this.datasetService.downloadDatasets(this.datasetIds, this.type).then(() => this.loading = false);
        } else if (this.mode == 'all') {
            if (this.useBids) {
                this.studyService.exportBIDSByStudyId(this.studyId).then(() => this.loading = false);
            } else {
                this.datasetService.downloadDatasetsByStudy(this.studyId, this.type).then(() => this.loading = false);
            }
        } 
        this.downloadDialog.hide();
    }

    public cancel() {
        this.downloadDialog.hide();
    }

}