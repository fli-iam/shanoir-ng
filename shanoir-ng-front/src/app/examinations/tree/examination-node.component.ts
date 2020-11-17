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
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';

import { DatasetAcquisitionNode, DatasetNode, ExaminationNode, ProcessingNode, UNLOADED } from '../../tree/tree.model';
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
    menuOpened: boolean = false;
    @Input() hasBox: boolean = false;

    constructor(
        private router: Router,
        private examinationService: ExaminationService,
        private datasetAcquisitionService: DatasetAcquisitionService,
        private examPipe: ExaminationPipe) {
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof ExaminationNode) {
                this.node = this.input;
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
        if (this.node.datasetAcquisitions == 'UNLOADED') this.loadDatasetAcquisitions();
    }

    loadDatasetAcquisitions() {
        this.loading = true;
        this.datasetAcquisitionService.getAllForExamination(this.node.id).then(dsAcqs => {
            if (dsAcqs) {
                this.node.datasetAcquisitions = dsAcqs.map(dsAcq => this.mapAcquisitionNode(dsAcq));
            }
            this.loading = false;
            this.node.open = true;
        }).catch(() => this.loading = false);
    }

    private mapAcquisitionNode(dsAcq: any): DatasetAcquisitionNode {
        return new DatasetAcquisitionNode(
            dsAcq.id,
            dsAcq.name,
            dsAcq.datasets ? dsAcq.datasets.map(ds => this.mapDatasetNode(ds)) : []
        );
    }
    
    private mapDatasetNode(dataset: Dataset): DatasetNode {
        return new DatasetNode(
            dataset.id,
            dataset.name,
            dataset.type,
            dataset.processings ? dataset.processings.map(proc => this.mapProcessingNode(proc)) : []
        );
    }
    
    private mapProcessingNode(processing: DatasetProcessing): ProcessingNode {
        return new ProcessingNode(
            processing.id,
            DatasetProcessingType.getLabel(processing.datasetProcessingType),
            processing.outputDatasets ? processing.outputDatasets.map(ds => this.mapDatasetNode(ds)) : []
        );
    }
}