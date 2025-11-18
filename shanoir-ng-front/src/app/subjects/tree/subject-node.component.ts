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
import { Component, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { ConsoleService } from 'src/app/shared/console/console.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';
import { TreeService } from 'src/app/studies/study/tree.service';

import { ExaminationPipe } from '../../examinations/shared/examination.pipe';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import {
    ClinicalSubjectNode,
    ExaminationNode,
    PreclinicalSubjectNode,
    ShanoirNode,
    SubjectNode,
    UNLOADED
} from '../../tree/tree.model';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { DropdownMenuComponent } from '../../shared/components/dropdown-menu/dropdown-menu.component';
import { MenuItemComponent } from '../../shared/components/dropdown-menu/menu-item/menu-item.component';
import { ExaminationNodeComponent } from '../../examinations/tree/examination-node.component';


@Component({
    selector: 'subject-node',
    templateUrl: 'subject-node.component.html',
    imports: [TreeNodeComponent, DropdownMenuComponent, RouterLink, MenuItemComponent, ExaminationNodeComponent]
})

export class SubjectNodeComponent extends TreeNodeAbstractComponent<SubjectNode> implements OnChanges {

    @Input() input: SubjectNode | {subject: Subject, parentNode: ShanoirNode};
    @Input() rights: StudyUserRight[];
    @Input() studyId: number;
   
    constructor(
            private examinationService: ExaminationService,
            private router: Router,
            private examPipe: ExaminationPipe,
            private downloadService: MassDownloadService,
            protected treeService: TreeService,
            private consoleService: ConsoleService,
            private subjectService: SubjectService,
            elementRef: ElementRef) {
        super(elementRef);
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
                        const sortedExaminations = examinations.sort((a: SubjectExamination, b: SubjectExamination) => {
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

    onExaminationDelete(index: number) {
        (this.node.examinations as ExaminationNode[]).splice(index, 1) ;
    }

    download() {
        this.loading = true;
        this.downloadService.downloadAllByStudyIdAndSubjectId(this.studyId, this.node.id, this.downloadState)
            .finally(() => this.loading = false);
    }
}
