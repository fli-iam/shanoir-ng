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

import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { TreeService } from 'src/app/studies/study/tree.service';

import { DatasetAcquisitionService } from '../../dataset-acquisitions/shared/dataset-acquisition.service';
import { environment } from '../../../environments/environment';
import { DatasetAcquisitionNode, ExaminationNode, ShanoirNode } from '../../tree/tree.model';
import { Examination } from '../shared/examination.model';
import { ExaminationPipe } from '../shared/examination.pipe';
import { ExaminationService } from '../shared/examination.service';


@Component({
    selector: 'examination-node',
    templateUrl: 'examination-node.component.html',
    standalone: false
})

export class ExaminationNodeComponent extends TreeNodeAbstractComponent<ExaminationNode> implements OnChanges {

    @Input() input: ExaminationNode | {examination: Examination, parentNode: ShanoirNode, hasDeleteRights: boolean, hasDownloadRights: boolean};
    @Output() examinationDelete: EventEmitter<void> = new EventEmitter();

    loading: boolean = false;
    @Input() hasBox: boolean = true;
    datasetIds: number[];
    hasDicom: boolean = false;
    downloading = false;
    detailsPath: string = '/examination/details/';
    preclinical: boolean;

    constructor(
            private examinationService: ExaminationService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private examPipe: ExaminationPipe,
            private massDownloadService : MassDownloadService,
            protected treeService: TreeService,
            elementRef: ElementRef) {
        super(elementRef);
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
                    this.input.parentNode,
                    this.input.examination?.id,
                    this.examPipe.transform(this.input.examination),
                    'UNLOADED',
                    this.input.examination.extraDataFilePathList,
                    this.input.hasDeleteRights,
                    this.input.hasDownloadRights,
                    this.input.examination.preclinical);
            }
            this.node.registerOpenPromise(this.contentLoaded);
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
        window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=1.4.9.12.34.1.8527.' + this.node.id, '_blank');
    }

    downloadFile(file) {
        this.examinationService.downloadFile(file, this.node.id, this.downloadState);
    }

    firstOpen() {
        if (this.node.datasetAcquisitions == 'UNLOADED') {
            this.loadDatasetAcquisitions().then(() => this.contentLoaded.resolve());
        } else {
            this.contentLoaded.resolve();
        }
    }

    loadDatasetAcquisitions(): Promise<void> {
        this.loading = true;
        return this.datasetAcquisitionService.getAllForExamination(this.node.id).then(dsAcqs => {
            if (!dsAcqs) dsAcqs = [];
            dsAcqs = dsAcqs.filter(acq => acq.type !== 'Processed');
            this.node.datasetAcquisitions = dsAcqs.map(dsAcq => DatasetAcquisitionNode.fromAcquisition(dsAcq, this.node, this.node.canDelete, this.node.canDownload));
            this.fetchDatasetIds(this.node.datasetAcquisitions);
            this.nodeInit.emit(this.node);
            this.loading = false;
        }).catch(() => {
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
                    datasetIds?.push(ds.id);
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

    deleteExamination() {
        this.examinationService.get(this.node.id).then(entity => {
            this.examinationService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.examinationDelete.emit();
                }
            });
        })
    }

    onAcquisitionDelete(index: number) {
        (this.node.datasetAcquisitions as DatasetAcquisitionNode[]).splice(index, 1) ;
    }
}
