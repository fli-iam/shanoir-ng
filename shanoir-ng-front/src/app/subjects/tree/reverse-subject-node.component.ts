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

import { SimpleStudy } from '../../studies/shared/study.model';
import { ReverseStudyNode, ReverseSubjectNode, ShanoirNode, UNLOADED } from '../../tree/tree.model';
import { Subject } from '../shared/subject.model';
import { Selection, TreeService } from 'src/app/studies/study/tree.service';
import { Tag } from 'src/app/tags/tag.model';


@Component({
    selector: 'reverse-subject-node',
    templateUrl: 'reverse-subject-node.component.html',
    standalone: false
})

export class ReverseSubjectNodeComponent implements OnChanges {

    @Input() input: ReverseSubjectNode | { subject: Subject, parentNode: ShanoirNode };
    @Input() studyId: number;
    @Output() nodeInit: EventEmitter<ReverseSubjectNode> = new EventEmitter();
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    node: ReverseSubjectNode = null;
    loading: boolean = false;
    menuOpened: boolean = false;
    showDetails: boolean;
    @Input() hasBox: boolean = false;
    awesome = "fas fa-user-injured";

    constructor(
            private router: Router,
            protected treeService: TreeService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input'] && this.input) {
            if (this.input instanceof ReverseSubjectNode) {
                this.node = this.input;
            } else {
                this.node = new ReverseSubjectNode(
                    this.input.parentNode,
                    this.input.subject.id,
                    this.input.subject.name,
                    this.input.subject.subjectStudyList?.map(subjectStudy => {
                        let tags: Tag[] = [];
                        if (!(this.input instanceof ReverseSubjectNode)) {
                            tags = this.input.subject.subjectStudyList[this.input.subject.subjectStudyList.findIndex(element => element.study.id == subjectStudy.study.id)].tags;
                        }
                        return ReverseStudyNode.fromStudy(subjectStudy.study, tags, this.node);
                    })
                );
                if(this.input.subject.preclinical){
                    this.awesome = "fas fa-hippo"
                }
            }
            this.nodeInit.emit(this.node);
            this.showDetails = this.router.url != '/subject/details/' + this.node.id;
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.studies) return false;
        else if (this.node.studies == 'UNLOADED') return 'unknown';
        else return this.node.studies.length > 0;
    }

}
