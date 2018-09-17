import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';

import { ExaminationService } from '../../examinations/shared/examination.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Study } from '../shared/study.model';

@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
})

export class StudyTreeComponent {

    private brainIconPath: string = ImagesUrlUtil.BRAIN_ICON_PATH;
    public folderIconPath: string = ImagesUrlUtil.FOLDER_12_ICON_PATH;
    private homeIconPath: string = ImagesUrlUtil.HOME_ICON_PATH;
    private listIconPath: string = ImagesUrlUtil.LIST_ICON_PATH;
    @Input() study: Study;
    private studyCardIconPath: string = ImagesUrlUtil.STUDY_CARD_ICON_PATH;
    @Output() subjectUpdatedEvent = new EventEmitter();
    private userIconPath: string = ImagesUrlUtil.USER_ICON_PATH;
    private usersIconPath: string = ImagesUrlUtil.USERS_ICON_PATH;
    private xRay1IconPath: string = ImagesUrlUtil.X_RAY_1_ICON_PATH;
    private xRay2IconPath: string = ImagesUrlUtil.X_RAY_2_ICON_PATH;

    constructor(private examinationService: ExaminationService, private router: Router) {
    }

    getSubjectDetails(component: TreeNodeComponent) {
        component.dataLoading = true;
        let subjectStudy: SubjectStudy = component.nodeParams;
        this.examinationService.findExaminationsBySubjectAndStudy(subjectStudy.subject.id, this.study.id)
            .then(examinations => {
                if (examinations) {
                    subjectStudy.examinations = examinations;
                    component.hasChildren = true;
                }
                component.open();
            })
            .catch((error) => {
                component.open();
                // TODO: display error
                console.log("error getting examination list!");
            });
    }

    showAcquisitionEquipmentDetails(acquisitionEquipmentId: number) {
        this.router.navigate(['/acquisition-equipment'], { queryParams: { id: acquisitionEquipmentId, mode: "view" } });
    }

    showCenterDetails(centerId: number) {
        this.router.navigate(['/center'], { queryParams: { id: centerId, mode: "view" } });
    }

    showExaminationDetails(examinationId: number) {
        this.router.navigate(['/examination'], { queryParams: { id: examinationId, mode: "view" } });
    }

    showMemberDetails(userId: number) {
        this.router.navigate(['/user'], { queryParams: { id: userId } });
    }

    showSubjectDetails(subjectStudy: SubjectStudy) {
        this.router.navigate(['/subject'], { queryParams: { id: subjectStudy.subject.id, mode: "view" } });
    }

    /* TODO: uncomment when dataset-acquisition done
    showDatasetAcquisitionDetails(datasetAcquisitionId: number) {
        this.router.navigate(['/dataset-acquisition'], { queryParams: { id: datasetAcquisitionId, mode: "view" } });
    }
    */

    showDatasetDetails(datasetId: number) {
        this.router.navigate(['/dataset'], { queryParams: { id: datasetId, mode: "view" } });
    }

}