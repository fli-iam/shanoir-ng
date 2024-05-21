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
import { Component, HostListener } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Subject, Subscription } from 'rxjs';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { EntityType } from 'src/app/shared/components/entity/entity.abstract';
import { slideDown } from '../../shared/animations/animations';
import { CenterNode, ClinicalSubjectNode, DatasetAcquisitionNode, ExaminationNode, MemberNode, PreclinicalSubjectNode, RightNode, StudyNode, SubjectNode, UNLOADED } from '../../tree/tree.model';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { Dataset } from 'src/app/datasets/shared/dataset.model';

@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
    animations: [slideDown]
})

export class StudyTreeComponent {

    protected studyNode: StudyNode = null;
    protected study: Study;
    protected subscriptions: Subscription[] = [];
    protected selection: Selection = new Selection();

    constructor(
            private breadcrumbsService: BreadcrumbsService,
            protected activatedRoute: ActivatedRoute,
            private studyService: StudyService,
            private datasetService: DatasetService) {

        this.subscriptions.push(this.activatedRoute.params.subscribe(
            params => {
                const id = +params['id'];
                this.initStudy(id).then(() => {
                    this.breadcrumbsService.currentStepAsMilestone(this.study.name);
                    this.selectDataset(4);
                });
            }
        ));
    }

    selectDataset(id: number) {
        //test
        this.selection.datasetId = id;
        let datasetAlreadyLoaded: boolean = false; 
        if (!datasetAlreadyLoaded) {
            this.studyNode.subjectsNode.open().then(() => {
                this.datasetService.get(id).then(ds => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[]).find(sn => sn.id == ds.datasetAcquisition?.examination?.subject?.id);
                    if (subjectNode) {
                        subjectNode.open().then(() => {
                            let examNode: ExaminationNode = (subjectNode.examinations as ExaminationNode[])?.find(exam => exam.id == ds.datasetAcquisition?.examination?.id);
                            if (examNode) {
                                examNode.open().then(() => {
                                    let acqNode: DatasetAcquisitionNode = (examNode.datasetAcquisitions as DatasetAcquisitionNode[])?.find(acq => acq.id == ds.datasetAcquisition?.id);
                                    if (acqNode) {
                                        acqNode.open();
                                    }
                                });
                            }
                        });
                    }
                });
            });
        }
    }

    initStudy(id: number): Promise<void> {
        let studyPromise: Promise<any> = this.studyService.get(id, null).then(study => {
            this.study = study;
            let subjectNodes: SubjectNode[] = study.subjectStudyList?.map(ss => {
                let subjectNode: SubjectNode;
                if (ss.subject?.preclinical){
                    subjectNode = new PreclinicalSubjectNode(
                        this.studyNode,
                        ss.subject?.id,
                        ss.subject?.name,
                        ss.tags,
                        UNLOADED,
                        null,
                        false);
                } else {
                    subjectNode = new ClinicalSubjectNode(
                        this.studyNode,
                        ss.subject?.id,
                        ss.subject?.name,
                        ss.tags,
                        UNLOADED,
                        null,
                        false);
                }
                return subjectNode;
            });
            let centerNodes: CenterNode[] = study.studyCenterList?.map(sc => new CenterNode(this.studyNode, sc.center.id, sc.center.name, UNLOADED));
            let memberNodes: MemberNode[] = study.studyUserList?.map(su => {
                let memberNode = null;
                memberNode = new MemberNode(this.studyNode, su.user?.id || su.userId, su.userName, su.studyUserRights?.map(sur => new RightNode(memberNode, null, StudyUserRight.getLabel(sur))));
                return memberNode;
            });
            this.studyNode = new StudyNode(null, study.id, study.name, subjectNodes, centerNodes, UNLOADED, memberNodes);
            this.studyNode.open();
        });
        return studyPromise;
    }

    protected navigate(param: {type: EntityType, id: number}) {
        this.selection.selectedEntity = param.type;
        this.selection.id = param.id;
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            console.log('selection', this.selection);
            console.log('node', this.studyNode);
        }
    }
}

export class Selection {

    public selectedEntity: EntityType | 'datasetMetadata';
    public id: number;

    set studyId(id: number) {
        this.selectedEntity = 'study';
        this.id = id;
    }

    get studyId(): number {
        return this.selectedEntity == 'study' ? this.id : null;
    }

    set subjectId(id: number) {
        this.selectedEntity = 'subject';
        this.id = id;
    }

    get subjectId(): number {
        return this.selectedEntity == 'subject' ? this.id : null;
    }

    set examinationId(id: number) {
        this.selectedEntity = 'examination';
        this.id = id;
    }

    get examinationId(): number {
        return this.selectedEntity == 'examination' ? this.id : null;
    }

    set acquisitionId(id: number) {
        this.selectedEntity = 'acquisition';
        this.id = id;
    }

    get acquisitionId(): number {
        return this.selectedEntity == 'acquisition' ? this.id : null;
    }

    set datasetId(id: number) {
        this.selectedEntity = 'dataset';
        this.id = id;
    }

    get datasetId(): number {
        return this.selectedEntity == 'dataset' ? this.id : null;
    }

    set processingId(id: number) {
        this.selectedEntity = 'processing';
        this.id = id;
    }

    get processingId(): number {
        return this.selectedEntity == 'processing' ? this.id : null;
    }

    set centerId(id: number) {
        this.selectedEntity = 'center';
        this.id = id;
    }

    get centerId(): number {
        return this.selectedEntity == 'center' ? this.id : null;
    }

    set equipmentId(id: number) {
        this.selectedEntity = 'equipment';
        this.id = id;
    }

    get equipmentId(): number {
        return this.selectedEntity == 'equipment' ? this.id : null;
    }

    set studycardId(id: number) {
        this.selectedEntity = 'studycard';
        this.id = id;
    }

    get studycardId(): number {
        return this.selectedEntity == 'studycard' ? this.id : null;
    }

    set qualitycardId(id: number) {
        this.selectedEntity = 'qualitycard';
        this.id = id;
    }

    get qualitycardId(): number {
        return this.selectedEntity == 'qualitycard' ? this.id : null;
    }

    set memberId(id: number) {
        this.selectedEntity = 'user';
        this.id = id;
    }

    get memberId(): number {
        return this.selectedEntity == 'user' ? this.id : null;
    }

    set datasetMetadataId(id: number) {
        this.selectedEntity = 'datasetMetadata';
        this.id = id;
    }

    get datasetMetadataId(): number {
        return this.selectedEntity == 'datasetMetadata' ? this.id : null;
    }
}
