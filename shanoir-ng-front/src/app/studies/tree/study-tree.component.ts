import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { Study } from '../shared/study.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { SubjectType } from '../../subjects/shared/subject-type';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';

@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
})

export class StudyTreeComponent {

    @Input() study: Study;

    constructor(private router: Router) {
    }

    showAcquisitionEquipmentDetails(acquisitionEquipmentId: number) {
        this.router.navigate(['/acquisition-equipment'], { queryParams: { id: acquisitionEquipmentId, mode: "view" } });        
    }

    showCenterDetails(centerId: number) {
        this.router.navigate(['/center'], { queryParams: { id: centerId, mode: "view" } });        
    }

    showMemberDetails(userId: number) {
        this.router.navigate(['/user'], { queryParams: { id: userId } });        
    }

}