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
import { Component, HostListener, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { SubjectStudyPipe } from '../../subjects/shared/subject-study.pipe';

import { CenterNode, MemberNode, RightNode, StudyNode, SubjectNode, UNLOADED } from '../../tree/tree.model';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';

@Component({
    selector: 'study-node',
    templateUrl: 'study-node.component.html'
})

export class StudyNodeComponent implements OnChanges {

    @Input() input: StudyNode | Study;
    node: StudyNode;
    loading: boolean = false;

    constructor(
        private router: Router,
        private subjectStudyPipe: SubjectStudyPipe) {
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof StudyNode) {
                this.node = this.input;
            } else {
                let subjects: SubjectNode[] = this.input.subjectStudyList.map(subjectStudy => {
                    return new SubjectNode(subjectStudy.subject.id, this.subjectStudyPipe.transform(subjectStudy), UNLOADED);
                });
                let centers: CenterNode[] = this.input.studyCenterList.map(studyCenter => {
                    return new CenterNode(studyCenter.center.id, studyCenter.center.name, UNLOADED);
                });
                let members: MemberNode[] = this.input.studyUserList.map(studyUser => {
                    let rights: RightNode[] = studyUser.studyUserRights.map(suRight => new RightNode(null, StudyUserRight.getLabel(suRight)));
                    return new MemberNode(studyUser.userId, studyUser.userName, rights);
                });

                this.node = new StudyNode(
                    this.input.id,
                    this.input.name,
                    subjects,
                    centers,
                    UNLOADED,
                    members);  // members
            }
        }
    }

    hasSubjects(): boolean | 'unknown' {
        if (!this.node.subjects) return false;
        else if (this.node.subjects == 'UNLOADED') return 'unknown';
        else return this.node.subjects.length > 0;
    }
}