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
import { Component, HostListener, ViewChild} from '@angular/core';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { Router } from '../../breadcrumbs/router';
import { slideDown } from '../../shared/animations/animations';
import * as AppUtils from '../../utils/app.utils';
import { EegDataset } from '../../datasets/dataset/eeg/dataset.eeg.model';
import { EegDatasetDTO } from '../../datasets/shared/dataset.dto';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';

@Component({
    selector: 'eeg-select-series',
    templateUrl: 'eeg-select-series.component.html',
    styleUrls: ['eeg-select-series.component.css', '../shared/import.step.css'],
    animations: [slideDown]
})
export class EegSelectSeriesComponent {

    @ViewChild('selectAll') tree: TreeNodeComponent;
    public datasets: EegDatasetDTO[];
    protected selectedDatasets: EegDatasetDTO[] = [];
    public datasetDetail: EegDatasetDTO;

    constructor(
            private importService: ImportService,
            private breadcrumbsService: BreadcrumbsService,
            private router: Router,
            protected importDataService: ImportDataService) {

        // If no datasets to import, go back to import
        if (!this.importDataService.eegImportJob || !this.importDataService.eegImportJob.datasets) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        // Initialize breadcrumbs and datasets
        breadcrumbsService.nameStep('2. Series');
        this.datasets = this.importDataService.eegImportJob.datasets;
        this.selectedDatasets = [];
    }

    changeDataset(datasetToMove: EegDatasetDTO) {
      let index = this.selectedDatasets.indexOf(datasetToMove);
        if (index != -1) {
            this.selectedDatasets.splice(index, 1);
            datasetToMove.selected = false;
        } else {
            this.selectedDatasets.push(datasetToMove);
            datasetToMove.selected = true;
        }
    }

    changeDatasets() {
        // Check all or none
        this.selectedDatasets = [];
        if (this.tree.checked) {
            this.importDataService.eegImportJob.datasets.map(dts => {
               this.selectedDatasets.push(dts);
               dts.selected = true;
            });
        } else {
            this.importDataService.eegImportJob.datasets.map(dts => {
               dts.selected = false;
            });
        }
    }

    public showDatasetDetail(datasetToDetail: EegDatasetDTO): void {
        this.datasetDetail = datasetToDetail
    }

    get valid(): boolean {
        return (this.selectedDatasets && this.selectedDatasets.length > 0);
    }

    public next() {
        this.importDataService.eegImportJob.datasets = this.selectedDatasets;
        this.router.navigate(['imports/eegcontext']);
    }

}
