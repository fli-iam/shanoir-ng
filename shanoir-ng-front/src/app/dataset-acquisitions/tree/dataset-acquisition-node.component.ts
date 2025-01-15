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
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetService } from '../../datasets/shared/dataset.service';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';

import { Subscription } from 'rxjs';
import { TaskState } from 'src/app/async-tasks/task.model';
import { Selection, TreeService } from 'src/app/studies/study/tree.service';
import { ConsoleService } from "../../shared/console/console.service";
import { MassDownloadService } from "../../shared/mass-download/mass-download.service";
import { DatasetAcquisitionNode, DatasetNode, ProcessingNode, ShanoirNode, UNLOADED } from '../../tree/tree.model';
import { DatasetAcquisition } from '../shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from "../shared/dataset-acquisition.service";
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';

@Component({
    selector: 'dataset-acquisition-node',
    templateUrl: 'dataset-acquisition-node.component.html',
    standalone: false
})

export class DatasetAcquisitionNodeComponent implements OnChanges, OnDestroy {

    progressStatus: TaskState;
    @Input() input: DatasetAcquisitionNode | {datasetAcquisition: DatasetAcquisition, parentNode: ShanoirNode, studyRights: StudyUserRight[]} ;
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
    @Input() withMenu: boolean = true;
    protected downloadState: TaskState = new TaskState();

    constructor(
        private datasetService: DatasetService,
        private datasetAcquisitionService: DatasetAcquisitionService,
        private consoleService: ConsoleService,
        private massDownloadService: MassDownloadService,
        protected treeService: TreeService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof DatasetAcquisitionNode) {
                this.node = this.input;
                if(this.node.datasets != "UNLOADED"){
                    this.setDatasetIds(this.node.datasets);
                }
            } else {
                let label: string = 'Dataset Acquisition n°' + this.input.datasetAcquisition.id;
                this.node = new DatasetAcquisitionNode(
                    this.input.parentNode,
                    this.input.datasetAcquisition.id,
                    label,
                    UNLOADED,
                    this.input.studyRights.includes(StudyUserRight.CAN_ADMINISTRATE),
                    this.input.studyRights.includes(StudyUserRight.CAN_DOWNLOAD)
                );
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
                this.node.datasets = datasets.map(ds => DatasetNode.fromDataset(ds, false, this.node, this.node.canDelete, this.node.canDownload)).sort();
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
