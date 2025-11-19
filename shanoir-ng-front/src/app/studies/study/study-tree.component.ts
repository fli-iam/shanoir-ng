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
import { Component, ElementRef, HostListener, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { TaskState } from 'src/app/async-tasks/task.model';
import { ConfirmDialogService } from 'src/app/shared/components/confirm-dialog/confirm-dialog.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { SubjectNodeComponent } from 'src/app/subjects/tree/subject-node.component';
import { DatasetAcquisitionNode, DatasetNode, ExaminationNode, ShanoirNode, StudyNode } from 'src/app/tree/tree.model';
import { ExecutionDataService } from 'src/app/vip/execution.data-service';

import { environment } from "../../../environments/environment";

import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { RightsError } from 'src/app/shared/models/error.model';
import { TreeService } from './tree.service';


@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
    standalone: false
})

export class StudyTreeComponent implements OnDestroy {

    _selectedDatasetNodes: DatasetNode[] = [];
    selectedExaminationNodes: ExaminationNode[] = [];
    selectedAcquisitionNodes: DatasetAcquisitionNode[] = [];
    protected downloadState: TaskState;
    canOpenDicomSingleExam: boolean = false;
    canOpenDicomMultiExam: boolean = false;
    protected loaded: boolean = false;
    private subscriptions: Subscription[] = [];
    subjectNodes: SubjectNodeComponent[] = [];
    @ViewChild('tree') treeContainer: ElementRef;

    constructor(
            protected treeService: TreeService,
            private processingService: ExecutionDataService,
            private router: Router,
            private downloadService: MassDownloadService,
            private dialogService: ConfirmDialogService,
            private datasetService: DatasetService) {

        treeService.studyNodeOpenPromise.then(() => this.loaded = true);

        this.subscriptions.push(
            treeService.onScrollToSelected.subscribe(node => {
                setTimeout(() => {
                    this.autoScrollTo(node);
                });
            })
        );
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(s => s.unsubscribe());
    }

    private autoScrollTo(node: ShanoirNode) {
        const nodeTop: number = node?.getTop?.();
        const containerHeight: number = this.treeContainer.nativeElement.offsetHeight;
        const currentScroll: number = this.treeContainer.nativeElement.scrollTop;
        const diff: number = nodeTop - containerHeight - currentScroll;
        if (diff > 0 || nodeTop - currentScroll < 0) {
            this.treeContainer.nativeElement.scrollTop = diff + currentScroll + (containerHeight/2);
        }

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
        this.getSelectedDatasetIdsIncludingExamAndAcq().then(allSelectedIds => {
            this.processingService.setDatasets(allSelectedIds);
            this.router.navigate(['pipelines']);
        }).catch(e => {
            if (e instanceof RightsError) {
                this.dialogService.error('error', 'Sorry, you don\'t have the right to download all the datasets you have selected');
            }
        });
    }

    downloadSelected() {
        this.getSelectedDatasetIdsIncludingExamAndAcq().then(allSelectedIds => {
            this.downloadService.downloadByIds(Array.from(allSelectedIds), this.downloadState);
        }).catch(e => {
            if (e instanceof RightsError) {
                this.dialogService.error('error', 'Sorry, you don\'t have the right to download all the datasets you have selected');
            }
        });
    }

    getSelectedDatasetIdsIncludingExamAndAcq(): Promise<Set<number>> /* throws RightsError */ {        
        // Check directly selected datasets for download rights
        if (this.selectedDatasetNodes.find(dsNode => !dsNode.canDownload)) {
            return Promise.reject(new RightsError());
        }
        else if (this.selectedAcquisitionNodes?.length > 0 || this.selectedExaminationNodes?.length > 0) {
            return this.datasetService.getDownloadData(
                this.selectedAcquisitionNodes.map(acqNode => acqNode.id),
                this.selectedExaminationNodes.map(examNode => examNode.id)
            ).then(results => {
                const denied = results.filter(res => !res.canDownload);
                if (denied.length > 0) {
                    throw new RightsError();
                } else {
                    const allIds: Set<number> = new Set();
                    this.selectedDatasetNodes.forEach(dsNode => allIds.add(dsNode.id));
                    results.forEach(res => allIds.add(res.id));
                    return allIds;
                }
            });
        } else {
            return Promise.resolve(new Set(this.selectedDatasetNodes.map(dsNode => dsNode.id)));
        }

    openInViewer() {
        const studies: Set<string> = new Set();
        const series: Set<string> = new Set();
        if (this.selectedExaminationNodes?.length > 0) {
            this.selectedExaminationNodes.forEach(exam => studies.add('1.4.9.12.34.1.8527.' + exam.id));
        }
        if (this.selectedAcquisitionNodes?.length > 0) {
            this.selectedAcquisitionNodes.forEach(acq => series.add('1.4.9.12.34.1.8527.' + acq.id));
            this.selectedAcquisitionNodes.forEach(acq => studies.add('1.4.9.12.34.1.8527.' + acq.parent.id));
        }
        if ((series.size == 0 && studies.size > 0) || studies.size > 1) {
            window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + Array.from(studies).join(','), '_blank');
        } else if (series.size > 0 && studies.size == 1) {
            window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=' + Array.from(studies).join(',') + '&SeriesInstanceUIDs=' + Array.from(series).join(','), '_blank');
        }
    }

    onSelectedChange(study: StudyNode) {
        let dsNodes: DatasetNode[] = [];
        const acqNodes: DatasetAcquisitionNode[] = [];
        const examNodes: ExaminationNode[] = [];
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
                    const foundInProc: DatasetNode[] = ds.processings
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

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            console.log('tree', this.treeService.studyNode);
        }
    }

}


