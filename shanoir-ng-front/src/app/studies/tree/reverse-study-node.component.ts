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
import { DatasetAcquisition } from '../../dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { ExaminationPipe } from '../../examinations/shared/examination.pipe';

import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { DatasetAcquisitionNode, DatasetNode, ExaminationNode, ProcessingNode, ReverseStudyNode, ShanoirNode, UNLOADED } from '../../tree/tree.model';
import { Study } from '../shared/study.model';
import {KeycloakService} from "../../shared/keycloak/keycloak.service";
import {StudyRightsService} from "../shared/study-rights.service";
import {StudyUserRight} from "../shared/study-user-right.enum";
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import {TaskState} from "../../async-tasks/task.model";
import { SuperPromise } from 'src/app/utils/super-promise';

@Component({
    selector: 'reverse-study-node',
    templateUrl: 'reverse-study-node.component.html',
    standalone: false
})

export class ReverseStudyNodeComponent implements OnChanges {

    @Input() input: ReverseStudyNode | {study: Study, parentNode: ShanoirNode};
    @Input() subjectId: number;
    @Output() nodeInit: EventEmitter<ReverseStudyNode> = new EventEmitter();
    @Output() selectedChange: EventEmitter<ReverseStudyNode> = new EventEmitter();
    node: ReverseStudyNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    studyCardsLoading: boolean = false;
    showDetails: boolean;
    hasDicom: boolean = true;
    @Input() hasBox: boolean = false;
    detailsPath: string = '/study/details/';
    private canAdmin: boolean = false;
    private canDownload: boolean = false;
    public downloadState: TaskState = new TaskState();
    idPromise: SuperPromise<number> = new SuperPromise();

    constructor(
            private router: Router,
            private examinationService: ExaminationService,
            private examPipe: ExaminationPipe,
            private keycloakService: KeycloakService,
            private studyRightsService: StudyRightsService,
            private downloadService: MassDownloadService) {

        this.idPromise.then(id => {
            (this.keycloakService.isUserAdmin
                ? Promise.resolve(StudyUserRight.all())
                : this.studyRightsService.getMyRightsForStudy(id)
            ).then(rights => {
                this.canAdmin = rights.includes(StudyUserRight.CAN_ADMINISTRATE);
                this.canDownload = rights.includes(StudyUserRight.CAN_DOWNLOAD);
            });
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (!changes['input']) {
            return;
        }
        let id: number = this.input instanceof ReverseStudyNode ? this.input.id : this.input.study.id;
        this.idPromise.resolve(id);
        if (this.input instanceof ReverseStudyNode) {
            this.node = this.input;
        } else {
            this.node = new ReverseStudyNode(
                this.input.parentNode,
                this.input.study.id,
                this.input.study.name,
                [],
                UNLOADED);
        }
        this.nodeInit.emit(this.node);
        this.showDetails = this.router.url != '/study/details/' + this.node.id;
    }

    loadExaminations() {
        this.idPromise.then(() => {
            if (this.node.examinations == UNLOADED) {
                this.loading = true;
                this.examinationService.findExaminationsBySubjectAndStudy(this.subjectId, this.node.id)
                .then(examinations => {
                    let sortedExaminations = examinations.sort((a: SubjectExamination, b: SubjectExamination) => {
                        return (new Date(a.examinationDate)).getTime() - (new Date(b.examinationDate)).getTime();
                    })
                    this.node.examinations = [];
                    if (sortedExaminations) {
                        sortedExaminations.forEach(exam => {
                            let examNode = ExaminationNode.fromExam(exam, this.node, this.canAdmin, this.canDownload);
                            (this.node.examinations as ExaminationNode[]).push(examNode);
                        });
                    }
                    this.loading = false;
                    this.node.open();
                }).catch(() => {
                    this.loading = false;
                });
            }
        });
    }

    hasDependency(dependencyArr: any[] | UNLOADED): boolean | 'unknown' {
        if (!dependencyArr) return false;
        else if (dependencyArr == UNLOADED) return 'unknown';
        else return dependencyArr.length > 0;
    }

    download() {
        this.loading = true;
        this.downloadService.downloadAllByStudyIdAndSubjectId(this.node.id, this.subjectId, this.downloadState)
            .finally(() => this.loading = false);
    }
}
