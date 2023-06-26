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
import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {Router} from '@angular/router';

import {DatasetAcquisition} from '../../dataset-acquisitions/shared/dataset-acquisition.model';
import {DatasetProcessing} from '../../datasets/shared/dataset-processing.model';
import {Dataset} from '../../datasets/shared/dataset.model';
import {DatasetProcessingType} from '../../enum/dataset-processing-type.enum';
import {ExaminationPipe} from '../../examinations/shared/examination.pipe';
import {ExaminationService} from '../../examinations/shared/examination.service';
import {SubjectExamination} from '../../examinations/shared/subject-examination.model';
import {
    DatasetAcquisitionNode,
    DatasetNode,
    ExaminationNode,
    ProcessingNode,
    ClinicalSubjectNode,
    UNLOADED, SubjectNode, PreclinicalSubjectNode,
} from '../../tree/tree.model';
import {Subject} from '../shared/subject.model';
import {SubjectService} from "../shared/subject.service";


@Component({
    selector: 'subject-node',
    templateUrl: 'subject-node.component.html'
})

export class SubjectNodeComponent implements OnChanges {

    @Input() input: Subject | SubjectNode;
    @Input() studyId: number;
    @Output() nodeInit: EventEmitter<SubjectNode> = new EventEmitter();
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    node: SubjectNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    showDetails: boolean;
    @Input() hasBox: boolean = false;
    detailsPath: string = "/subject/details/";

    constructor(
        private examinationService: ExaminationService,
        private subjectService: SubjectService,
        private router: Router,
        private examPipe: ExaminationPipe) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof SubjectNode) {
                this.node = this.input;
            } else if (this.input.preclinical){
                this.node = new PreclinicalSubjectNode(
                    this.input.id,
                    this.input.name,
                    [],
                    UNLOADED,
                    null,
                    false);
            } else {
                this.node = new ClinicalSubjectNode(
                    this.input.id,
                    this.input.name,
                    [],
                    UNLOADED,
                    null,
                    false);
            }
            this.nodeInit.emit(this.node);
            this.showDetails = this.router.url != '/' + this.node.title + '/details/' + this.node.id;
        }
    }

    loadExaminations() {
        if (this.node.examinations == UNLOADED) {
            this.loading = true;
            this.examinationService.findExaminationsBySubjectAndStudy(this.node.id, this.studyId)
                .then(examinations => {
                    let sortedExaminations = examinations.sort((a: SubjectExamination, b: SubjectExamination) => {
                        return (new Date(a.examinationDate)).getTime() - (new Date(b.examinationDate)).getTime();
                    })
                    this.node.examinations = [];
                    if (sortedExaminations) {
                        sortedExaminations.forEach(exam => {
                            (this.node.examinations as ExaminationNode[]).push(this.mapExamNode(exam));
                        });
                    }
                    this.loading = false;
                    this.node.open = true;
                }).catch(() => {
                this.loading = false;
            });
        }
    }

    private mapExamNode(exam: SubjectExamination): ExaminationNode {
        return new ExaminationNode(
            exam.id,
            this.examPipe.transform(exam),
            exam.datasetAcquisitions ? exam.datasetAcquisitions.map(dsAcq => this.mapAcquisitionNode(dsAcq)) : [],
            exam.extraDataFilePathList,
            this.node.canDeleteChildren
        );
    }

    private mapAcquisitionNode(dsAcq: DatasetAcquisition): DatasetAcquisitionNode {
        return new DatasetAcquisitionNode(
            dsAcq.id,
            dsAcq.name,
            dsAcq.datasets ? dsAcq.datasets.map(ds => this.mapDatasetNode(ds, false)) : [],
            this.node.canDeleteChildren
        );
    }

    private mapDatasetNode(dataset: Dataset, processed: boolean): DatasetNode {
        return new DatasetNode(
            dataset.id,
            dataset.name,
            dataset.type,
            dataset.processings ? dataset.processings.map(proc => this.mapProcessingNode(proc)) : [],
            processed,
            this.node.canDeleteChildren
        );
    }

    private mapProcessingNode(processing: DatasetProcessing): ProcessingNode {
        return new ProcessingNode(
            processing.id,
            DatasetProcessingType.getLabel(processing.datasetProcessingType),
            processing.outputDatasets ? processing.outputDatasets.map(ds => this.mapDatasetNode(ds, true)) : [],
            this.node.canDeleteChildren
        );
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.examinations) return false;
        else if (this.node.examinations == (UNLOADED as any)) return 'unknown';
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
}
