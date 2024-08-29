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
import { Component } from '@angular/core';

import { Router } from '@angular/router';
import { TaskState } from 'src/app/async-tasks/task.model';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { ExecutionDataService } from 'src/app/vip/execution.data-service';
import { TreeService } from './tree.service';
import {DatasetNode, ExaminationNode, StudyNode} from 'src/app/tree/tree.model';
import { MsgBoxService } from 'src/app/shared/msg-box/msg-box.service';
import { ConfirmDialogService } from 'src/app/shared/components/confirm-dialog/confirm-dialog.service';
import {Examination} from "../../examinations/shared/examination.model";
import {environment} from "../../../environments/environment";
import {ConsoleService} from "../../shared/console/console.service";


@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css']
})

export class StudyTreeComponent {

    _selectedDatasetNodes: DatasetNode[] = [];
    selectedExaminationNodes: number[] = [];
    protected downloadState: TaskState;

    constructor(
            protected treeService: TreeService,
            private processingService: ExecutionDataService,
            private router: Router,
            private downloadService: MassDownloadService,
            private dialogService: ConfirmDialogService,
            private consoleService: ConsoleService) {
    }

    protected set selectedDatasetNodes(selectedDatasetNodes: DatasetNode[]) {
        this._selectedDatasetNodes = selectedDatasetNodes;
    }

    get selectedDatasetNodes(): DatasetNode[] {
        return this._selectedDatasetNodes;
    }

    goToProcessing() {
        this.processingService.setDatasets(new Set(this.selectedDatasetNodes?.map(n => n.id)));
        this.router.navigate(['pipelines']);
    }

    downloadSelected() {
        if (this.selectedDatasetNodes.find(node => !node.canDownload)) {
            this.dialogService.error('error', 'Sorry, you don\'t have the right to download all the datasets you have selected');
        } else {
            this.downloadService.downloadByIds(this.selectedDatasetNodes?.map(n => n.id), this.downloadState);
        }

    }

    openInViewer() {
        console.log("selected exam nodes : " + this.selectedExaminationNodes);
        let res = this.selectedExaminationNodes.map(id => `1.4.9.12.34.1.8527.${id}`).join(',');
        window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + res, '_blank');
    }

    onSelectedChange(study: StudyNode) {
        let dsNodes: DatasetNode[] = [];
        if (study.subjectsNode.subjects && study.subjectsNode.subjects != 'UNLOADED') {
            study.subjectsNode.subjects.forEach(subj => {
                if (subj.examinations && subj.examinations != 'UNLOADED') {
                    subj.examinations.forEach(exam => {
                        if (exam.selected && this.selectedExaminationNodes.length <= 10 && !this.selectedExaminationNodes.includes(exam.id)) this.selectedExaminationNodes.push(exam.id);
                        if (!exam.selected && this.selectedExaminationNodes.includes(exam.id)) this.selectedExaminationNodes.splice(this.selectedExaminationNodes.indexOf(exam.id), 1);
                        if (this.selectedExaminationNodes.length > 10) this.consoleService.log('warn', 'For performance reasons, you cannot open more than 10 examinations in the viewer at the same time.')

                        if (exam.datasetAcquisitions && exam.datasetAcquisitions != 'UNLOADED') {
                            exam.datasetAcquisitions.forEach(dsAcq => {
                                dsNodes = dsNodes.concat(this.searchSelectedInDatasetNodes(dsAcq.datasets));
                            });
                        }
                    });
                }
            });
        }
        this.selectedDatasetNodes = dsNodes;
    }

    private searchSelectedInDatasetNodes(dsNodes: DatasetNode[] | 'UNLOADED'): DatasetNode[] {
        if (dsNodes && dsNodes != 'UNLOADED') {
            return dsNodes.map(ds => {
                // get selected dataset from this nodes
                let nodesFound: DatasetNode[] = ds.selected ? [ds] : [];
                // get selected datasets from this node's processings datasets
                if (ds.processings && ds.processings != 'UNLOADED') {
                    let foundInProc: DatasetNode[] = ds.processings
                            .map(proc => this.searchSelectedInDatasetNodes(proc.datasets))
                            .reduce((allFromProc, oneProc) => allFromProc.concat(oneProc), []);
                            nodesFound = nodesFound.concat(foundInProc);
                }
                return nodesFound;
            }).reduce((allFromDs, thisDs) => {
                return allFromDs.concat(thisDs);
            }, []);
        } else return [];
    }

}


