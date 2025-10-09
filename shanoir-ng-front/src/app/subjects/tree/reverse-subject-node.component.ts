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
import { Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { TreeService } from 'src/app/studies/study/tree.service';
import { ReverseStudyNode, ReverseSubjectNode, ShanoirNode } from '../../tree/tree.model';
import { Subject } from '../shared/subject.model';


@Component({
    selector: 'reverse-subject-node',
    templateUrl: 'reverse-subject-node.component.html',
    standalone: false
})

export class ReverseSubjectNodeComponent extends TreeNodeAbstractComponent<ReverseSubjectNode> implements OnChanges {

    @Input() input: ReverseSubjectNode | { subject: Subject, parentNode: ShanoirNode };
    @Input() studyId: number;
    @Output() nodeInit: EventEmitter<ReverseSubjectNode> = new EventEmitter();
    awesome = "fas fa-user-injured";

    constructor(
            private router: Router,
            protected treeService: TreeService,
            elementRef: ElementRef) {
        super(elementRef);
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
                    ReverseStudyNode.fromStudy(this.input.subject.study, this.input.subject.tags, this.node)
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
        else return this.node.studies != null;
    }

}
