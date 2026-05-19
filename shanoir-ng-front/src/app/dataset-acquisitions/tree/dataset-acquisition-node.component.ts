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
import { Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

import { TaskState } from 'src/app/async-tasks/task.model';
import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';
import { TreeService } from 'src/app/studies/study/tree.service';

import { DatasetService } from '../../datasets/shared/dataset.service';
import { ConsoleService } from "../../shared/console/console.service";
import { MassDownloadService } from "../../shared/mass-download/mass-download.service";
import { DatasetAcquisitionNode, DatasetNode, ShanoirNode, UNLOADED } from '../../tree/tree.model';
import { DatasetAcquisition } from '../shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from "../shared/dataset-acquisition.service";


@Component({
    selector: 'dataset-acquisition-node',
    templateUrl: 'dataset-acquisition-node.component.html',
    standalone: false
})

export class DatasetAcquisitionNodeComponent extends TreeNodeAbstractComponent<DatasetAcquisitionNode> implements OnChanges {

    progressStatus: TaskState;
    @Input() input: DatasetAcquisitionNode | {datasetAcquisition: DatasetAcquisition, parentNode: ShanoirNode, studyRights: StudyUserRight[]} ;
    @Input() hasBox: boolean = false;
    detailsPath: string = '/dataset-acquisition/details/';
    @Output() acquisitionDelete: EventEmitter<void> = new EventEmitter();
    datasetIds: number[] = [];
    hasEEG: boolean = false;
    hasDicom: boolean = false;
    downloading = false;
    hasBids: boolean = false;

    constructor(
            private datasetService: DatasetService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private consoleService: ConsoleService,
            private massDownloadService: MassDownloadService,
            protected treeService: TreeService,
            elementRef: ElementRef) {
        super(elementRef);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof DatasetAcquisitionNode) {
                this.node = this.input;
                if(this.node.datasets != "UNLOADED"){
                    this.setDatasetIds(this.node.datasets);
                }
            } else {
                const label: string = 'Dataset Acquisition n°' + this.input.datasetAcquisition.id;
                this.node = new DatasetAcquisitionNode(
                    this.input.parentNode,
                    this.input.datasetAcquisition.id,
                    label,
                    UNLOADED,
                    this.input.studyRights.includes(StudyUserRight.CAN_ADMINISTRATE),
                    this.input.studyRights.includes(StudyUserRight.CAN_DOWNLOAD)
                );
            }
            this.node.registerOpenPromise(this.contentLoaded);
            this.nodeInit.emit(this.node);
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.datasets) return false;
        else if (this.node.datasets == 'UNLOADED') return 'unknown';
        else return this.node.datasets.length > 0;
    }
    
    loadDatasets() {
        if (this.node.datasets == UNLOADED) {
            this.loading = true;
            this.datasetService.getByAcquisitionId(this.node.id).then(datasets => {
                this.node.datasets = datasets.map(ds => DatasetNode.fromDataset(ds, false, this.node, this.node.canDelete, this.node.canDownload)).sort();
                this.setDatasetIds(this.node.datasets);
            }).finally(() => {
                this.loading = false;
                this.contentLoaded.resolve();
            });
        } else {
            this.contentLoaded.resolve();
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
                    this.acquisitionDelete.emit();
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
}
