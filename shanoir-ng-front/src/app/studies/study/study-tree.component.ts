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
import { ConfirmDialogService } from 'src/app/shared/components/confirm-dialog/confirm-dialog.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { DatasetAcquisitionNode, DatasetNode, ExaminationNode, StudyNode } from 'src/app/tree/tree.model';
import { ExecutionDataService } from 'src/app/vip/execution.data-service';
import { environment } from "../../../environments/environment";
import { TreeService } from './tree.service';


@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css']
})

export class StudyTreeComponent {

    _selectedDatasetNodes: DatasetNode[] = [];
    selectedExaminationNodes: ExaminationNode[] = [];
    selectedAcquisitionNodes: DatasetAcquisitionNode[] = [];
    protected downloadState: TaskState;
    canOpenDicomSingleExam: boolean = false;
    canOpenDicomMultiExam: boolean = false;
    protected loaded: boolean = false;

    constructor(
            protected treeService: TreeService,
            private processingService: ExecutionDataService,
            private router: Router,
            private downloadService: MassDownloadService,
            private dialogService: ConfirmDialogService) {

        treeService.studyNodeOpenPromise.then(() => this.loaded = true)
    }

    protected set selectedDatasetNodes(selectedDatasetNodes: DatasetNode[]) {
        this._selectedDatasetNodes = selectedDatasetNodes;
    }

    get selectedDatasetNodes(): DatasetNode[] {
        return this._selectedDatasetNodes;
    }


    get selectionEmpty(): boolean {
        return !this.loaded || (
            !(this.selectedDatasetNodes?.length > 0) 
            && !(this.selectedAcquisitionNodes?.length > 0) 
            && !(this.selectedExaminationNodes?.length > 0)
        );
    }

    goToProcessing() {
        let allSelectedNodes: DatasetNode[] = this.getSelectedDatasetNodesIncludingExamAndAcq();
        this.processingService.setDatasets(new Set(allSelectedNodes?.map(n => n.id)));
        this.router.navigate(['pipelines']);
    }

    downloadSelected() {
        let allSelectedNodes: DatasetNode[] = this.getSelectedDatasetNodesIncludingExamAndAcq();
        if (allSelectedNodes.find(node => !node.canDownload)) {
            this.dialogService.error('error', 'Sorry, you don\'t have the right to download all the datasets you have selected');
        } else {
            this.downloadService.downloadByIds(allSelectedNodes?.map(n => n.id), this.downloadState);
        }
    }

    getSelectedDatasetNodesIncludingExamAndAcq(): DatasetNode[] {
        let allSelectedNodes: DatasetNode[] = []; // selected datasets + datasets in selected exams and acq
        /** Concat all selected */
        allSelectedNodes = allSelectedNodes.concat(this.selectedDatasetNodes);
        this.selectedAcquisitionNodes.forEach(acqNode => {
            if (acqNode.datasets != 'UNLOADED') allSelectedNodes = allSelectedNodes.concat(acqNode.datasets);
        });
        this.selectedExaminationNodes.forEach(examNode => {
            if (examNode.datasetAcquisitions != 'UNLOADED') {
                examNode.datasetAcquisitions.forEach(acqNode => {
                    if (acqNode.datasets != 'UNLOADED') allSelectedNodes = allSelectedNodes.concat(acqNode.datasets);
                });
            }
        });
        return allSelectedNodes;
    }

    openInViewer() {
        console.log('?')
        let studies = "";
        let series = "";
        if (this.selectedExaminationNodes?.length > 0) {
            studies = this.selectedExaminationNodes.map(exam => '1.4.9.12.34.1.8527.' + exam.id).join(',');

        }
        if (this.selectedAcquisitionNodes?.length > 0) {
            studies += this.selectedAcquisitionNodes.map(acq => '1.4.9.12.34.1.8527.' + acq.parent.id).join(',');
            series = this.selectedAcquisitionNodes.map(acq => '1.4.9.12.34.1.8527.' + acq.id).join(',');
        }
        if (series.length == 0) {
            window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + studies, '_blank');
            console.log('###', environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + studies, '_blank');
        } else if (series.length > 0 && studies.length > 0) {
            window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + studies + '&SeriesInstanceUIDs=' + series, '_blank');
            console.log('###', environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + studies + '&SeriesInstanceUIDs=' + series, '_blank');
        }
    }

    onSelectedChange(study: StudyNode) {
        let dsNodes: DatasetNode[] = [];
        let acqNodes: DatasetAcquisitionNode[] = [];
        let examNodes: ExaminationNode[] = [];
        if (study.subjectsNode.subjects && study.subjectsNode.subjects != 'UNLOADED') {
            study.subjectsNode.subjects.forEach(subj => {
                if (subj.examinations && subj.examinations != 'UNLOADED') {
                    subj.examinations.forEach(exam => {
                        if (exam.selected) examNodes.push(exam);
                        if (exam.datasetAcquisitions && exam.datasetAcquisitions != 'UNLOADED') {
                            exam.datasetAcquisitions.forEach(dsAcq => {
                                if (dsAcq.selected) acqNodes.push(dsAcq);
                                dsNodes = dsNodes.concat(this.searchSelectedInDatasetNodes(dsAcq.datasets));
                            });
                        }
                    });
                }
            });
        }
        this.selectedDatasetNodes = dsNodes;
        this.selectedAcquisitionNodes = acqNodes;
        this.selectedExaminationNodes = examNodes;
        this.canOpenDicomMultiExam = this.canOpenDicomSingleExam = false;
 
        if (this.selectedExaminationNodes.length == 0) {
            if (this.selectedAcquisitionNodes.length > 0) {
                this.canOpenDicomSingleExam = (!this.selectedAcquisitionNodes.find(acqNode => acqNode.parent.id != this.selectedAcquisitionNodes[0]?.parent.id));
                this.canOpenDicomMultiExam = !this.canOpenDicomSingleExam;
            } 
        }
        else if (this.selectedExaminationNodes.length == 1) {
            if (this.selectedAcquisitionNodes.length > 0) {
                this.canOpenDicomSingleExam = 
                    (!this.selectedAcquisitionNodes.find(acqNode => acqNode.parent.id != this.selectedAcquisitionNodes[0]?.parent.id || acqNode.parent.id != this.selectedExaminationNodes[0].id));
                    this.canOpenDicomMultiExam = !this.canOpenDicomSingleExam;
            } else {
                this.canOpenDicomSingleExam = true;
            }
        } else {
            this.canOpenDicomSingleExam = false;
            this.canOpenDicomMultiExam = true;
        }
        
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

    resetSelection() {
        this.treeService.unSelectAll();
        this.onSelectedChange(this.treeService.studyNode);
    }

}


