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
import { ModalComponent } from '../../shared/components/modal/modal.component';


@Component({
    selector: 'dataset-download',
    templateUrl: 'dataset-download.component.html',
})

/** Button to download datasets. 
    Opens a popup stating:
    - Type of download
    - Use bids format
    - nii.gz or .zip */
export class DatasetDownaloadComponent {
    
    constructor(private datasetService: DatasetService) {
        
    }

    @Input() datasets: Dataset[];
    useBids: boolean = false;
    type: string;
    @ViewChild('downloadDialog') downloadDialog: ModalComponent;

    private dataType: string;

    /** Click on first button */
    prepareDownload() {
        if (!this.datasets || this.datasets.length == 0) {
            // TODO: display an error in the message box
        }
        // Display the messageBox with options
        this.downloadDialog.show();
    }

    /** Download the data */
    public download() {
        if (!this.datasets || this.datasets.length == 0) {
            // TODO: return an error
        }
        // Call service method to download datasets
        this.datasetService.downloadDatasets(this.datasets.map(dataset => dataset.id), this.dataType);
    }

}