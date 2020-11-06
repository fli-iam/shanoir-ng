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
import { Examination } from '../../examinations/shared/examination.model';
import { ExaminationPipe } from '../../examinations/shared/examination.pipe';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { Study } from '../../studies/shared/study.model';
import {
    DatasetAcquisitionNode,
    DatasetNode,
    ExaminationNode,
    ProcessingNode,
    ReverseStudyNode,
    ReverseSubjectNode,
    UNLOADED,
} from '../../tree/tree.model';
import { Subject } from '../shared/subject.model';


@Component({
    selector: 'reverse-subject-node',
    templateUrl: 'reverse-subject-node.component.html'
})

export class ReverseSubjectNodeComponent implements OnChanges {

    @Input() input: Subject | ReverseSubjectNode;
    @Input() studyId: number;
    @Output() nodeInit: EventEmitter<ReverseSubjectNode> = new EventEmitter();
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    node: ReverseSubjectNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    showDetails: boolean;
    @Input() hasBox: boolean = false;

    constructor(
            private examinationService: ExaminationService,
            private router: Router,
            private examPipe: ExaminationPipe) {
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof ReverseSubjectNode) {
                this.node = this.input;
            } else {
                this.node  = new ReverseSubjectNode(
                    this.input.id,
                    this.input.name,
                    this.input.subjectStudyList.map(subjectStudy => this.mapStudy(subjectStudy.study, subjectStudy.examinations)));
            }
            this.nodeInit.emit(this.node);
            this.showDetails = this.router.url != '/subject/details/' + this.node.id;
        } 
    }

    private mapStudy(study: Study, examinations: SubjectExamination[]): ReverseStudyNode {
        return new ReverseStudyNode(
            study.id,
            study.name,
            UNLOADED
        );
    }
    
    
    hasChildren(): boolean | 'unknown' {
        if (!this.node.studies) return false;
        else if (this.node.studies == 'UNLOADED') return 'unknown';
        else return this.node.studies.length > 0;
    }

    showSubjectDetails() {
        this.router.navigate(['/subject/details/' + this.node.id]);
    }

    collapseAll() {
        console.log('collapse');
    }
}