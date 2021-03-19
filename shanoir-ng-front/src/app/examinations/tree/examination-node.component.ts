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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { DatasetAcquisitionService } from '../../dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetService } from '../../datasets/shared/dataset.service';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

import { DatasetAcquisitionNode, DatasetNode, ExaminationNode, ProcessingNode } from '../../tree/tree.model';
import { Examination } from '../shared/examination.model';
import { ExaminationPipe } from '../shared/examination.pipe';
import { ExaminationService } from '../shared/examination.service';

@Component({
    selector: 'examination-node',
    templateUrl: 'examination-node.component.html'
})

export class ExaminationNodeComponent implements OnChanges {

    @Input() input: ExaminationNode | Examination;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() nodeInit: EventEmitter<ExaminationNode> = new EventEmitter();
    node: ExaminationNode;
    loading: boolean = false;
    downloading: boolean = false;
    menuOpened: boolean = false;
    @Input() hasBox: boolean = false;
    datasetIds: number[];

    constructor(
        private router: Router,
        private examinationService: ExaminationService,
        private datasetAcquisitionService: DatasetAcquisitionService,
        private examPipe: ExaminationPipe,
        private datasetService: DatasetService,
        private msgService: MsgBoxService) {
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof ExaminationNode) {
                this.node = this.input;
                if (this.input.datasetAcquisitions != 'UNLOADED') this.fetchDatasetIds(this.input.datasetAcquisitions);
            } else {
                this.node = new ExaminationNode(
                        this.input.id, 
                        this.examPipe.transform(this.input),
                        'UNLOADED',
                        this.input.extraDataFilePathList);
            }
            this.nodeInit.emit(this.node);
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.datasetAcquisitions && !this.node.extraDataFilePathList) return false;
        else if (this.node.datasetAcquisitions == 'UNLOADED' || this.node.extraDataFilePathList == 'UNLOADED') return 'unknown';
        else return (this.node.datasetAcquisitions && this.node.datasetAcquisitions.length > 0) 
                ||  (this.node.extraDataFilePathList && this.node.extraDataFilePathList.length > 0) ;
    }

    showExaminationDetails() {
        this.router.navigate(['/examination/details/' + this.node.id]);
    }

    downloadFile(file) {
        this.examinationService.downloadFile(file, this.node.id);
    }

    firstOpen() {
        if (this.node.datasetAcquisitions == 'UNLOADED') this.loadDatasetAcquisitions().then(() => this.node.open = true);
    }

    loadDatasetAcquisitions(): Promise<void> {
        this.loading = true;

        return this.datasetAcquisitionService.getAllForExamination(this.node.id).then(dsAcqs => {
            if (!dsAcqs) dsAcqs = [];
            this.node.datasetAcquisitions = dsAcqs.map(dsAcq => this.mapAcquisitionNode(dsAcq));
            this.fetchDatasetIds(this.node.datasetAcquisitions);
            this.nodeInit.emit(this.node);
            this.loading = false; 
        }).catch((reason) => { this.loading = false; });
    }
    
    fetchDatasetIds(datasetAcquisitions: DatasetAcquisitionNode[]) {
        let datasetIds: number[] = [];
        if (datasetAcquisitions) {
            datasetAcquisitions.forEach(dsAcq => {
                if (dsAcq.datasets == 'UNLOADED') {
                    datasetIds = undefined; // abort
                    return;
                } else {
                    dsAcq.datasets.forEach(ds => {
                        datasetIds.push(ds.id);                    
                    });
                }
            });
        }
        this.datasetIds = datasetIds;
    }

    download(format: string) {
        if (this.datasetIds && this.datasetIds.length == 0) return;
        let datasetIdsReady: Promise<void>;
        if (this.node.datasetAcquisitions == 'UNLOADED') {
            datasetIdsReady = this.loadDatasetAcquisitions();
            if (!this.datasetIds || this.datasetIds.length == 0) {
                this.msgService.log('warn', 'Sorry, no dataset for this examination');
                return;
            }
        } else {
            datasetIdsReady = Promise.resolve();
        }
        datasetIdsReady.then(() => {
            this.downloading = true;
            this.datasetService.downloadDatasets(this.datasetIds, format)
                    .then(() => this.downloading = false).catch(() => this.downloading = false);
        });
    }


    mapAcquisitionNode(dsAcq: any): DatasetAcquisitionNode {
        return new DatasetAcquisitionNode(
            dsAcq.id,
            dsAcq.name,
            dsAcq.datasets ? dsAcq.datasets.map(ds => this.mapDatasetNode(ds)) : []
        );
    }
    
    mapDatasetNode(dataset: Dataset): DatasetNode {
        return new DatasetNode(
            dataset.id,
            dataset.name,
            dataset.type,
            dataset.processings ? dataset.processings.map(proc => this.mapProcessingNode(proc)) : []
        );
    }
    
    mapProcessingNode(processing: DatasetProcessing): ProcessingNode {
        return new ProcessingNode(
            processing.id,
            DatasetProcessingType.getLabel(processing.datasetProcessingType),
            processing.outputDatasets ? processing.outputDatasets.map(ds => this.mapDatasetNode(ds)) : []
        );
    }
}