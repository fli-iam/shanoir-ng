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

import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { ExaminationService } from '../../examinations/shared/examination.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { IdName } from '../../shared/models/id-name.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { SubjectStudy } from '../shared/subject-study.model';
import { Subject } from '../shared/subject.model';



@Component({
    selector: 'subject-tree',
    templateUrl: 'subject-tree.component.html',
    styleUrls: ['subject-tree.component.css'],
})

export class SubjectTreeComponent {

    constructor(private examinationService: ExaminationService, private router: Router) {
    }
   
    @Input() subject: Subject;
    @Input() studies: IdName[];
    public fileIconPath: string = ImagesUrlUtil.FILE_ICON_PATH;
    public folderIconPath: string = ImagesUrlUtil.FOLDER_12_ICON_PATH;
    private listIconPath: string = ImagesUrlUtil.LIST_ICON_PATH;
    private xRay2IconPath: string = ImagesUrlUtil.X_RAY_2_ICON_PATH;


    getStudyDetails(component: TreeNodeComponent) {
        component.dataLoading = true;
        let subjectStudy: SubjectStudy = component.nodeParams;
        this.examinationService.findExaminationsBySubjectAndStudy(subjectStudy.subject.id, subjectStudy.study.id)
            .then(examinations => {
                if (examinations) {
                    subjectStudy.examinations = examinations;
                    component.hasChildren = true;
                }
                component.open();
            });
    }

    showExaminationDetails(examinationId: number) {
        this.router.navigate(['/examination/details/' + examinationId])
    }

    showStudyDetails(studyId: number) {
        this.router.navigate(['/study/details/' + studyId])
    }

    showAcquisitionDetails(studyId: number) {
        
    }

    showDatasetDetails(datasetId: number) {
        this.router.navigate(['/dataset/details/' + datasetId])
    }

}