import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { Study } from '../shared/study.model';
import { StudyUserType } from '../shared/study-user-type.enum';
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

    getMemberCategoryLabel(studyUserTypeStr: string): string {
        let studyUserType:StudyUserType = StudyUserType[studyUserTypeStr];
        switch (studyUserType) {
            case StudyUserType.RESPONSIBLE: {
                return 'Responsible';
            }
            case StudyUserType.SEE_DOWNLOAD_IMPORT_MODIFY: {
                return 'Members that can modify the research study';
            }
            case StudyUserType.SEE_DOWNLOAD_IMPORT: {
                return 'Members that can import datasets in the research study';
            }
            case StudyUserType.NOT_SEE_DOWNLOAD: {
                return 'Members that can\'t see the datasets produced in the research study and can\'t download them';
            }
            case StudyUserType.SEE_DOWNLOAD: {
                return 'Members that can download datasets produced in the research study';
            }
       }

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