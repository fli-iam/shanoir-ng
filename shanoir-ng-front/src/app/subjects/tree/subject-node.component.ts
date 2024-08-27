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

import { TaskState } from 'src/app/async-tasks/task.model';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { TreeService } from 'src/app/studies/study/tree.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { DatasetAcquisition } from '../../dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { ExaminationPipe } from '../../examinations/shared/examination.pipe';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import {
    ClinicalSubjectNode,
    DatasetAcquisitionNode,
    DatasetNode,
    ExaminationNode,
    PreclinicalSubjectNode,
    ProcessingNode,
    ShanoirNode,
    SubjectNode,
    UNLOADED,
} from '../../tree/tree.model';
import { Subject } from '../shared/subject.model';
import { ConsoleService } from 'src/app/shared/console/console.service';
import { SubjectService } from '../shared/subject.service';
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';


@Component({
    selector: 'subject-node',
    templateUrl: 'subject-node.component.html'
})

export class SubjectNodeComponent implements OnChanges {

    @Input() input: SubjectNode | {subject: Subject, parentNode: ShanoirNode};
    @Input() rights: StudyUserRight[];
    @Input() studyId: number;
    @Output() nodeInit: EventEmitter<SubjectNode> = new EventEmitter();
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() onNodeSelect: EventEmitter<number> = new EventEmitter();
    node: SubjectNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    showDetails: boolean;
    @Input() hasBox: boolean = false;
    detailsPath: string = "";
    @Input() withMenu: boolean = true;
    protected contentLoaded: SuperPromise<void> = new SuperPromise();
    public downloadState: TaskState = new TaskState();

    constructor(
            private examinationService: ExaminationService,
            private router: Router,
            private examPipe: ExaminationPipe,
            private downloadService: MassDownloadService,
            protected treeService: TreeService,
            private consoleService: ConsoleService,
            private subjectService: SubjectService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof SubjectNode) {
                this.node = this.input;
            } else if (this.input.subject.preclinical) {
                this.node = new PreclinicalSubjectNode(
                    this.node,
                    this.input.subject.id,
                    this.input.subject.name,
                    [],
                    UNLOADED,
                    null,
                    this.rights.includes(StudyUserRight.CAN_ADMINISTRATE),
                    this.rights.includes(StudyUserRight.CAN_DOWNLOAD),
                );
            } else {
                this.node = new ClinicalSubjectNode(
                    this.node,
                    this.input.subject.id,
                    this.input.subject.name,
                    [],
                    UNLOADED,
                    null,
                    this.rights.includes(StudyUserRight.CAN_ADMINISTRATE),
                    this.rights.includes(StudyUserRight.CAN_DOWNLOAD)
                );
            }
            this.node.registerOpenPromise(this.contentLoaded);
            this.nodeInit.emit(this.node);
            this.detailsPath = '/' + this.node.title + '/details/' + this.node.id;
            this.showDetails = this.router.url != this.detailsPath;
        }
    }

    loadExaminations(): Promise<void> {
        if (this.node.examinations == UNLOADED) {
            setTimeout(() => this.loading = true);
            return this.examinationService.findExaminationsBySubjectAndStudy(this.node.id, this.studyId)
                .then(examinations => {
                    this.node.examinations = [];
                    if (examinations) {
                        let sortedExaminations = examinations.sort((a: SubjectExamination, b: SubjectExamination) => {
                            return (new Date(a.examinationDate)).getTime() - (new Date(b.examinationDate)).getTime();
                        })
                        if (sortedExaminations) {
                            sortedExaminations.forEach(exam => {
                                (this.node.examinations as ExaminationNode[]).push(ExaminationNode.fromExam(exam, this.node, this.node.canDeleteChildren, this.node.canDownload));
                            });
                        }
                    }
                    this.loading = false;
                    this.node.open();
                }).catch(error => {
                    this.loading = false;
                    this.consoleService.log('error', error.toString());
                });
        } else {
            return Promise.resolve();
        }
    }

    onFirstOpen() {
        this.loadExaminations().then(() => {
            this.contentLoaded.resolve();
        });
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.examinations) return false;
        else if (this.node.examinations == UNLOADED) return 'unknown';
        else return this.node.examinations.length > 0;
    }

    showSubjectDetails() {
        this.router.navigate(['/' + this.node.title + '/details/' + this.node.id]);
    }

    collapseAll() {
    }

    onExaminationDelete(index: number) {
        (this.node.examinations as ExaminationNode[]).splice(index, 1) ;
    }

    download() {
        this.loading = true;
        this.downloadService.downloadAllByStudyIdAndSubjectId(this.studyId, this.node.id, this.downloadState)
            .finally(() => this.loading = false);
    }
}
