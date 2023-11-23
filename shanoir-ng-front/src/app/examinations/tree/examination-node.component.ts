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
import {Component, EventEmitter, ViewChild, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {DatasetAcquisitionService} from '../../dataset-acquisitions/shared/dataset-acquisition.service';
import {DatasetProcessing} from '../../datasets/shared/dataset-processing.model';
import {Dataset} from '../../datasets/shared/dataset.model';
import {DatasetProcessingType} from '../../enum/dataset-processing-type.enum';
import {ConsoleService} from '../../shared/console/console.service';

import {DatasetAcquisitionNode, DatasetNode, ExaminationNode, ProcessingNode} from '../../tree/tree.model';
import {Examination} from '../shared/examination.model';
import {ExaminationPipe} from '../shared/examination.pipe';
import {ExaminationService} from '../shared/examination.service';
import {environment} from '../../../environments/environment';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { TaskState } from 'src/app/async-tasks/task.model';

@Component({
    selector: 'examination-node',
    templateUrl: 'examination-node.component.html'
})

export class ExaminationNodeComponent implements OnChanges {

    @Input() input: ExaminationNode | Examination;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() nodeInit: EventEmitter<ExaminationNode> = new EventEmitter();
    @Output() onExaminationDelete: EventEmitter<void> = new EventEmitter();

    protected downloadState: TaskState = new TaskState();
    node: ExaminationNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    @Input() hasBox: boolean = false;
    datasetIds: number[];
    hasDicom: boolean = false;
    downloading = false;
    detailsPath: string = '/examination/details/';

    constructor(
        private examinationService: ExaminationService,
        private datasetAcquisitionService: DatasetAcquisitionService,
        private examPipe: ExaminationPipe,
        private massDownloadService : MassDownloadService,
        private consoleService: ConsoleService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof ExaminationNode) {
                this.node = this.input;
                if (this.input.datasetAcquisitions != 'UNLOADED') {
                    this.fetchDatasetIds(this.input.datasetAcquisitions);
                }
            } else {
                this.node = new ExaminationNode(
                    this.input.id,
                    this.examPipe.transform(this.input),
                    'UNLOADED',
                    this.input.extraDataFilePathList,
                    false);
            }
            this.nodeInit.emit(this.node);
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.datasetAcquisitions && !this.node.extraDataFilePathList) return false;
        else if (this.node.datasetAcquisitions == 'UNLOADED' || this.node.extraDataFilePathList == 'UNLOADED') return 'unknown';
        else return (this.node.datasetAcquisitions && this.node.datasetAcquisitions.length > 0)
                || (this.node.extraDataFilePathList && this.node.extraDataFilePathList.length > 0);
    }
    viewExaminationDicoms() {
        window.open(environment.viewerUrl + '/viewer/1.4.9.12.34.1.8527.' + this.node.id, '_blank');
    }

    downloadFile(file) {
        this.examinationService.downloadFile(file, this.node.id, this.downloadState);
    }

    firstOpen() {
        if (this.node.datasetAcquisitions == 'UNLOADED') this.loadDatasetAcquisitions().then(() => this.node.open = true);
    }

    loadDatasetAcquisitions(): Promise<void> {
        this.loading = true;

        return this.datasetAcquisitionService.getAllForExamination(this.node.id).then(dsAcqs => {
            if (!dsAcqs) dsAcqs = [];
            dsAcqs = dsAcqs.filter(acq => acq.type !== 'Processed');
            this.node.datasetAcquisitions = dsAcqs.map(dsAcq => this.mapAcquisitionNode(dsAcq));
            this.fetchDatasetIds(this.node.datasetAcquisitions);
            this.nodeInit.emit(this.node);
            this.loading = false;
        }).catch((reason) => {
            this.loading = false;
        });
    }

    fetchDatasetIds(datasetAcquisitions: DatasetAcquisitionNode[]) {
        if (!datasetAcquisitions) {
            return;
        }
        let datasetIds: number[] = [];
        datasetAcquisitions.forEach(dsAcq => {
            if (dsAcq.datasets == 'UNLOADED') {
                datasetIds = undefined; // abort
                return;
            } else {
                dsAcq.datasets.forEach(ds => {
                    datasetIds.push(ds.id);
                    if (ds.type != 'Eeg' && ds.type != 'BIDS') {
                        this.hasDicom = true;
                    }
                });
            }
        });
        this.datasetIds = datasetIds;
    }

    download() {
        if (this.downloading) {
            return;
        }
        this.downloading = true;
        this.massDownloadService.downloadAllByExaminationId(this.node.id, this.downloadState)
            .then(() => this.downloading = false);

    }

    mapAcquisitionNode(dsAcq: any): DatasetAcquisitionNode {
        return new DatasetAcquisitionNode(
            dsAcq.id,
            dsAcq.name,
            dsAcq.datasets ? dsAcq.datasets.map(ds => this.mapDatasetNode(ds, false)) : [],
            this.node.canDelete
        );
    }

    mapDatasetNode(dataset: Dataset, processed: boolean): DatasetNode {
        return new DatasetNode(
            dataset.id,
            dataset.name,
            dataset.type,
            dataset.processings ? dataset.processings.map(proc => this.mapProcessingNode(proc)) : [],
            processed,
            this.node.canDelete
        );
    }

    mapProcessingNode(processing: DatasetProcessing): ProcessingNode {
        return new ProcessingNode(
            processing.id,
            DatasetProcessingType.getLabel(processing.datasetProcessingType),
            processing.outputDatasets ? processing.outputDatasets.map(ds => this.mapDatasetNode(ds, true)) : [],
            this.node.canDelete
        );
    }

    deleteExamination() {
        this.examinationService.get(this.node.id).then(entity => {
            this.examinationService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.onExaminationDelete.emit();
                }
            });
        })
    }

    onAcquisitionDelete(index: number) {
        (this.node.datasetAcquisitions as DatasetAcquisitionNode[]).splice(index, 1) ;
    }
}
