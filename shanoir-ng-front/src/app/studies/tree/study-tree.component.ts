import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';

import { ExaminationService } from '../../examinations/shared/examination.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Study } from '../shared/study.model';
import { StudyUserType } from '../shared/study-user-type.enum';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { SubjectType } from '../../subjects/shared/subject-type';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';

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

    getMemberCategoryLabel(studyUserTypeStr: string): string {
        let studyUserType: StudyUserType = StudyUserType[studyUserTypeStr];
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

    getSubjectDetails(component: TreeNodeComponent) {
        component.dataLoading = true;
        let subject: SubjectStudy = component.nodeParams;
        this.examinationService.findExaminationsBySubjectAndStudy(subject.subjectId, this.study.id)
            .then(examinations => {
                if (examinations) {
                    subject.examinations = examinations;
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

}