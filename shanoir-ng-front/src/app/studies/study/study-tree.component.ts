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
import {DatasetAcquisitionNode, DatasetNode, ExaminationNode, StudyNode} from 'src/app/tree/tree.model';
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
    selectedAcquisitionNodes: number[] = [];
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
        let studies = "";
        let series = "";
        if (this.selectedExaminationNodes?.length > 0) {
            studies = this.selectedExaminationNodes.map(id => `1.4.9.12.34.1.8527.${id}`).join(',');

        }
        if (this.selectedAcquisitionNodes?.length > 0) {
            series = this.selectedAcquisitionNodes.map(id => `1.4.9.12.34.1.8527.${id}`).join(',');
        }
        console.log("studies : " + studies);
        console.log("series : " + series);
        if (series.length == 0)
            window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + studies, '_blank');
        if (studies.length == 0)
            window.open(environment.viewerUrl + '/viewer?SeriesInstanceUIDs=' + series, '_blank');
        if (series.length > 0 && studies.length > 0) {
            window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + studies + '&SeriesInstanceUIDs=' + series, '_blank');
        }
    }

    onSelectedChange(study: StudyNode) {
        let dsNodes: DatasetNode[] = [];
        if (study.subjectsNode.subjects && study.subjectsNode.subjects != 'UNLOADED') {
            study.subjectsNode.subjects.forEach(subj => {
                if (subj.examinations && subj.examinations != 'UNLOADED') {
                    subj.examinations.forEach(exam => {
                        this.checkSelectedExams(exam);
                        if (exam.datasetAcquisitions && exam.datasetAcquisitions != 'UNLOADED') {
                            exam.datasetAcquisitions.forEach(dsAcq => {
                                this.checkSelectedAcquisition(dsAcq);
                                dsNodes = dsNodes.concat(this.searchSelectedInDatasetNodes(dsAcq.datasets));
                            });
                        }
                    });
                }
            });
        }
        this.selectedDatasetNodes = dsNodes;
    }

    checkSelectedExams(exam : ExaminationNode) {
        // Exam selected
        if (exam.selected && this.selectedExaminationNodes.length <= 10 && !this.selectedExaminationNodes.includes(exam.id))
            this.selectedExaminationNodes.push(exam.id);
        // Exam unselected
        if (!exam.selected && this.selectedExaminationNodes.includes(exam.id))
            this.selectedExaminationNodes.splice(this.selectedExaminationNodes.indexOf(exam.id), 1);
        // More than 10 exam selected
        if (this.selectedExaminationNodes.length > 10)
            this.consoleService.log('warn', 'For performance reasons, you cannot open more than 10 examinations in the viewer at the same time.')
    }

    checkSelectedAcquisition(acq: DatasetAcquisitionNode) {
        // Exam selected
        if (acq.selected && this.selectedAcquisitionNodes.length <= 10 && !this.selectedAcquisitionNodes.includes(acq.id))
            this.selectedAcquisitionNodes.push(acq.id);
        // Exam unselected
        if (!acq.selected && this.selectedAcquisitionNodes.includes(acq.id))
            this.selectedAcquisitionNodes.splice(this.selectedAcquisitionNodes.indexOf(acq.id), 1);
        // More than 10 exam selected
        if (this.selectedAcquisitionNodes.length > 10)
            this.consoleService.log('warn', 'For performance reasons, you cannot open more than 10 acquisition in the viewer at the same time.')
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


