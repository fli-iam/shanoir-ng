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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetService } from '../../datasets/shared/dataset.service';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';

import { Subscription } from 'rxjs';
import { TaskState } from 'src/app/async-tasks/task.model';
import { ConsoleService } from "../../shared/console/console.service";
import { DatasetAcquisitionNode, DatasetNode, ProcessingNode, UNLOADED } from '../../tree/tree.model';
import { DatasetAcquisition } from '../shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from "../shared/dataset-acquisition.service";
import {MassDownloadService} from "../../shared/mass-download/mass-download.service";




@Component({
    selector: 'dataset-acquisition-node',
    templateUrl: 'dataset-acquisition-node.component.html'
})

export class DatasetAcquisitionNodeComponent implements OnChanges, OnDestroy {

    progressStatus: TaskState;
    @Input() input: DatasetAcquisitionNode | DatasetAcquisition;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    node: DatasetAcquisitionNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    @Input() hasBox: boolean = false;
    detailsPath: string = '/dataset-acquisition/details/';
    @Output() onAcquisitionDelete: EventEmitter<void> = new EventEmitter();
    datasetIds: number[] = [];
    hasEEG: boolean = false;
    hasDicom: boolean = false;
    downloading = false;
    hasBids: boolean = false;
    protected subscriptions: Subscription[] = [];
    protected downloadState: TaskState = new TaskState();

    constructor(
        private router: Router,
        private datasetService: DatasetService,
        private datasetAcquisitionService: DatasetAcquisitionService,
        private consoleService: ConsoleService,
        private massDownloadService: MassDownloadService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof DatasetAcquisitionNode) {
                this.node = this.input;
                if(this.node.datasets != "UNLOADED"){
                    this.setDatasetIds(this.node.datasets);
                }

            } else {
                let label: string = 'Dataset Acquisition n°' + this.input.id;
                this.node = new DatasetAcquisitionNode(this.input.id, label, UNLOADED,false);
                this.loadDatasets();
            }
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.datasets) return false;
        else if (this.node.datasets == 'UNLOADED') return 'unknown';
        else return this.node.datasets.length > 0;
    }
    loadDatasets() {
        if (this.node.datasets == UNLOADED) {
            this.datasetService.getByAcquisitionId(this.node.id).then(datasets => {
                this.node.datasets = datasets.map(ds => this.mapDatasetNode(ds, false)).sort();
                this.setDatasetIds(this.node.datasets);
            });
        }
    }

    setDatasetIds(nodes: DatasetNode[]){
        if(!nodes){
            return;
        }
        nodes.forEach(node => {
            this.datasetIds.push(node.id);
            if (node.type == 'Eeg') {
                this.hasEEG = true;
            } else if (node.type == 'BIDS') {
                this.hasBids = true;
            } else {
                this.hasDicom = true;
            }
        });
    }

    private mapDatasetNode(dataset: Dataset, processed: boolean): DatasetNode {
        return new DatasetNode(
            dataset.id,
            dataset.name,
            dataset.tags,
            dataset.type,
            dataset.processings ? dataset.processings.map(proc => this.mapProcessingNode(proc)) : [],
            processed,
            this.node.canDelete
        );
    }

    private mapProcessingNode(processing: DatasetProcessing): ProcessingNode {
        return new ProcessingNode(
            processing.id,
            processing.comment ? processing.comment : DatasetProcessingType.getLabel(processing.datasetProcessingType),
            processing.outputDatasets ? processing.outputDatasets.map(ds => this.mapDatasetNode(ds, true)) : [],
            this.node.canDelete
        );
    }

    deleteAcquisition() {
        this.datasetAcquisitionService.get(this.node.id).then(entity => {
            this.datasetAcquisitionService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.onAcquisitionDelete.emit();
                }
            });
        })
    }

    onDatasetDelete(index: number) {
        (this.node.datasets as DatasetNode[]).splice(index, 1) ;
    }

    download() {
        if (this.downloading) {
            return;
        }
        this.downloading = true;
        if (this.datasetIds && this.datasetIds.length == 0) return;
        let datasetIdsReady: Promise<void>;

        if (!this.datasetIds || this.datasetIds.length == 0) {
            this.consoleService.log('warn', 'Sorry, no dataset for acquisition n°' + this.node?.id);
            this.downloading = false;
            return;
        } else {
            datasetIdsReady = Promise.resolve();
        }

        datasetIdsReady.then(() => {
            this.massDownloadService.downloadAllByAcquisitionId(this.node.id, this.downloadState);
        });
    }

    ngOnDestroy() {
        for (let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }
}
