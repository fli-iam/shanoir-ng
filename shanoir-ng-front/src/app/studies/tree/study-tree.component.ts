import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AcquisitionEquipment } from 'src/app/acquisition-equipments/shared/acquisition-equipment.model';
import { CenterService } from '../../centers/shared/center.service';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { StudyCenter } from '../shared/study-center.model';
import { StudyUserType } from '../shared/study-user-type.enum';
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
    @Output() subjectUpdatedEvent = new EventEmitter();
    private userIconPath: string = ImagesUrlUtil.USER_ICON_PATH;
    private usersIconPath: string = ImagesUrlUtil.USERS_ICON_PATH;
    private xRay1IconPath: string = ImagesUrlUtil.X_RAY_1_ICON_PATH;
    private xRay2IconPath: string = ImagesUrlUtil.X_RAY_2_ICON_PATH;
    private acquisitionEquipments: AcquisitionEquipment[] = [];

    constructor(private examinationService: ExaminationService, private router: Router,
        private centerService: CenterService) {
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
    }

    getAcqEptForCenter(component: TreeNodeComponent) {
        component.dataLoading = true;
        let studyCenter: StudyCenter = component.nodeParams;
        this.centerService.get(studyCenter.center.id).then(
            center =>  {
                if (center) {
                    this.acquisitionEquipments = center.acquisitionEquipments;
                    component.hasChildren = true;
                }
                component.open();
            })
    }

    showAcquisitionEquipmentDetails(acquisitionEquipmentId: number) {
        this.router.navigate(['/acquisition-equipment/details/' + acquisitionEquipmentId])
    }

    showCenterDetails(centerId: number) {
        this.router.navigate(['/center/details/' + centerId])
    }

    showExaminationDetails(examinationId: number) {
        this.router.navigate(['/examination/details/' + examinationId])
    }

    showMemberDetails(userId: number) {
        this.router.navigate(['/user/details/' + userId])
    }

    showSubjectDetails(subjectStudy: SubjectStudy) {
        this.router.navigate(['/subject/details/' + subjectStudy.subject.id])
    }

    /* TODO: uncomment when dataset-acquisition done
    showDatasetAcquisitionDetails(datasetAcquisitionId: number) {
        this.router.navigate(['/dataset-acquisition/details/' + datasetAcquisitionId])
    }
    */

    showDatasetDetails(datasetId: number) {
        this.router.navigate(['/dataset/details/' + datasetId])
    }

    private getStudyUserTypeLabel(studyUserType: StudyUserType) {
        return StudyUserType.getLabel(studyUserType);
    }

}