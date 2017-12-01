import { Component, Input } from '@angular/core';

import { Study } from '../shared/study.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { SubjectType } from '../../subjects/shared/subject-type';
import { TreeNodeComponent } from '../../shared/components/tree/tree.node.component';

@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
})

export class StudyTreeComponent {

    @Input() study: Study;

}