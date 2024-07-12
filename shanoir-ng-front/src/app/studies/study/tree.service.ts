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

import { Injectable } from "@angular/core";
import { ActivatedRoute, ActivationStart, Router } from '@angular/router';
import { AcquisitionEquipment } from 'src/app/acquisition-equipments/shared/acquisition-equipment.model';
import { Center } from 'src/app/centers/shared/center.model';
import { DatasetAcquisition } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetProcessing } from 'src/app/datasets/shared/dataset-processing.model';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { Examination } from 'src/app/examinations/shared/examination.model';
import { QualityCard } from 'src/app/study-cards/shared/quality-card.model';
import { StudyCard } from 'src/app/study-cards/shared/study-card.model';
import { Subject } from "src/app/subjects/shared/subject.model";
import { User } from 'src/app/users/shared/user.model';
import { Study } from "../shared/study.model";

import { AcquisitionEquipmentService } from 'src/app/acquisition-equipments/shared/acquisition-equipment.service';
import { DatasetAcquisitionService } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetProcessingService } from 'src/app/datasets/shared/dataset-processing.service';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { ExaminationService } from 'src/app/examinations/shared/examination.service';
import { KeycloakService } from "src/app/shared/keycloak/keycloak.service";
import { SuperPromise } from 'src/app/utils/super-promise';
import { CenterNode, ClinicalSubjectNode, DatasetAcquisitionNode, DatasetNode, ExaminationNode, MemberNode, PreclinicalSubjectNode, ProcessingNode, RightNode, StudyNode, SubjectNode, UNLOADED } from '../../tree/tree.model';
import { StudyRightsService } from "../shared/study-rights.service";
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyService } from '../shared/study.service';
import { CoilService } from "src/app/coils/shared/coil.service";
import { Coil } from "src/app/coils/shared/coil.model";
import { Entity } from "src/app/shared/components/entity/entity.abstract";
import { SubjectStudy } from "src/app/subjects/shared/subject-study.model";
import { SubjectStudyPipe } from "src/app/subjects/shared/subject-study.pipe";

@Injectable()
export class TreeService {

    _selection: Selection = null;
    public studyNode: StudyNode = null;
    private studyNodePromise: SuperPromise<void> = new SuperPromise();
    protected study: Study;
    public nodeInit: boolean = false; 
    public canAdminStudy: boolean;
    private _treeOpened: boolean = true;
    public treeAvailable: boolean = false;

    isSelected(id: number, type: NodeType): boolean {
        return this.selection?.isSelected(id, type);
    }
    
    get selection(): Selection {
        return this._selection;
    }

    set selection(selection: Selection) {
        this._selection = selection;
        if (this.treeOpened) {
            this.changeSelection();
        }
    }   

    get treeOpened(): boolean {
        return this._treeOpened;
    }

    set treeOpened(opened: boolean) {
        localStorage.setItem('treeOpened', this.treeOpened ? 'true' : 'false');
        if (!this._treeOpened && opened) {
            this.changeSelection();
        }
        this._treeOpened = opened;
    }

    constructor(
            protected activatedRoute: ActivatedRoute,
            private studyService: StudyService,
            private datasetService: DatasetService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private datasetProcessingService: DatasetProcessingService,
            private acquisitionEquipmentService: AcquisitionEquipmentService,
            private coilService: CoilService,
            private examinationService: ExaminationService,
            private keycloakService: KeycloakService,
            private studyRightsService: StudyRightsService,
            private subjectStudyPipe: SubjectStudyPipe,
            private router: Router) {

        this.treeOpened = localStorage.getItem('treeOpened') == 'true';
        router.events.subscribe(event => {
            if (event instanceof ActivationStart) {
                setTimeout(() => {
                    this.treeAvailable = event?.snapshot?.data?.['treeAvailable'];
                });
            }
        });
    }

    activateTree(activatedRoute: ActivatedRoute) {
        activatedRoute.snapshot.data['treeAvailable'] = true;
    }
    
    private changeSelection(): void {
        if (this.selection?.type == 'study') {
            this.initStudy(this.selection.id).then(() => {
                this.studyNode.subjectsNode.open();
            });
        } else {
            let studyLoaded: Promise<void>;
            if (this.study?.id && this.selection.studyId?.includes(this.study?.id)) {
                studyLoaded = Promise.resolve();
            } else if (this.selection.studyId?.[0]) {
                studyLoaded = this.initStudy(this.selection.studyId[0]);
            }

            studyLoaded?.then(() => {
                if (this.selection?.type == 'dataset') {
                    this.selectDataset(this.selection.entity as Dataset);
                } else if (this.selection?.type == 'dicomMetadata') {
                    this.selectDicomMetadata(this.selection.entity as Dataset);
                } else if (this.selection?.type == 'subject') {
                    this.selectSubject();
                } else if (this.selection?.type == 'acquisition') {
                    this.selectAcquisition(this.selection.entity as DatasetAcquisition);
                } else if (this.selection?.type == 'processing') {
                    this.selectProcessing(this.selection.entity as DatasetProcessing);
                } else if (this.selection?.type == 'examination') {
                    this.selectExamination(this.selection.entity as Examination);
                } else if (this.selection?.type == 'center') {
                    this.selectCenter();
                } else if (this.selection?.type == 'equipment') {
                    this.selectEquipment(this.selection.entity as AcquisitionEquipment);
                } else if (this.selection?.type == 'qualitycard') {
                    this.selectQualitycard();
                } else if (this.selection?.type == 'studycard') {
                    this.selectStudycard();
                } else if (this.selection?.type == 'user') {
                    this.selectUser();
                } else if (this.selection?.type == 'coil') {
                    this.selectCoil(this.selection.entity as Coil);
                }
                
            });
        }
    }

    private selectDataset(dataset: number | Dataset): Promise<DatasetNode> {
        return this.studyNodePromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                return this.findDatasetParent(dataset).then(ret => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[]).find(sn => {
                        return sn.id == ret.topParent.datasetAcquisition?.examination?.subject?.id;
                    });
                    if (subjectNode) {
                        return subjectNode.open().then(() => {
                            let examNode: ExaminationNode = (subjectNode.examinations as ExaminationNode[])?.find(exam => exam.id == ret.topParent.datasetAcquisition?.examination?.id);
                            if (examNode) {
                                return examNode.open().then(() => {
                                    let acqNode: DatasetAcquisitionNode = (examNode.datasetAcquisitions as DatasetAcquisitionNode[])?.find(acq => acq.id == ret.topParent.datasetAcquisition?.id);
                                    if (acqNode) {
                                        return acqNode.open()?.then(() => {
                                            let dsNode: DatasetNode = (acqNode.datasets as DatasetNode[]).find(acqDs => acqDs.id == ret.topParent.id);
                                            if (dsNode) {
                                                return dsNode.open().then(() => {
                                                    if (ret.topParent.id != (typeof dataset == 'number' ? dataset : dataset.id)) { // if sub processing/datasets 
                                                        let procNode: ProcessingNode = dsNode.processings[0] as ProcessingNode;
                                                        if (procNode) {
                                                            return (dsNode.processings[0] as ProcessingNode).open().then(() => {
                                                                return (procNode.datasets as DatasetNode[]).find(dsNd => dsNd.id == (typeof dataset == 'number' ? dataset : dataset.id));
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            });
        });
    }

    private selectDicomMetadata(ds: number | Dataset) {
        this.selectDataset(ds).then(parentDsNode => {
            parentDsNode?.open();
        });
    }

    private findDatasetParent(childDataset: number | Dataset, botomChild?: Dataset): Promise<{topParent: Dataset, bottomChild: Dataset}> {
        let childDatasetPromise: Promise<Dataset>;
        if (typeof childDataset == 'number') {
            childDatasetPromise = this.datasetService.get(childDataset);
        } else {
            childDatasetPromise = Promise.resolve(childDataset);
        }
        return childDatasetPromise.then(ds => {
            if (!botomChild) botomChild = ds;
            if (ds.datasetProcessing) {
                if (!(ds.datasetProcessing.inputDatasets?.length > 0)) throw Error('no input ds on this processing');
                return this.findDatasetParent(ds.datasetProcessing.inputDatasets[0].id, botomChild);
            } else {
                return {topParent: ds, bottomChild: botomChild};
            }
        });
    }

    private selectProcessing(processing: number | DatasetProcessing) {
        let processingPromise: Promise<DatasetProcessing>;
        if (typeof processing == 'number') {
            processingPromise = this.datasetProcessingService.get(processing);
        } else {
            processingPromise = Promise.resolve(processing);
        }
        processingPromise.then(proc => {
            this.selectDataset(proc.inputDatasets[0].id).then(parentDsNode => {
                parentDsNode?.open();
            });
        });
    }

    private selectAcquisition(acq: number | DatasetAcquisition) {
        this.studyNodePromise.then(() => {
            this.studyNode.subjectsNode.open().then(() => {
                let acqPromise: Promise<DatasetAcquisition>;
                if (typeof acq == 'number') {
                    acqPromise = this.datasetAcquisitionService.get(acq);
                } else {
                    acqPromise = Promise.resolve(acq);
                }
                acqPromise.then(dsa => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[]).find(sn => sn.id == dsa.examination?.subject?.id);
                    if (subjectNode) {
                        subjectNode.open().then(() => {
                            let examNode: ExaminationNode = (subjectNode.examinations as ExaminationNode[])?.find(exam => exam.id == dsa.examination?.id);
                            if (examNode) {
                                examNode.open();
                            }
                        });
                    }
                });
            });
        });
    }

    private selectExamination(examination: number | Examination) {
        this.studyNodePromise.then(() => {
            this.studyNode.subjectsNode.open().then(() => {
                let examPromise: Promise<Examination>;
                if (typeof examination == 'number') {
                    examPromise = this.examinationService.get(examination);
                } else {
                    examPromise = Promise.resolve(examination);
                }
                examPromise.then(exam => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[]).find(sn => sn.id == exam.subject?.id);
                    if (subjectNode) {
                        subjectNode.open();
                    }
                });
            });
        });
    }

    private selectSubject() {
        this.studyNodePromise.then(() => {
            this.studyNode.subjectsNode.open();
        });
    }

    private selectCenter() {
        this.studyNodePromise.then(() => {
            this.studyNode.centersNode.open();
        });
    }

    private selectEquipment(equipment: number | AcquisitionEquipment) {
        this.studyNodePromise.then(() => {
            this.studyNode.centersNode.open().then(() => {
                (typeof equipment == 'number' 
                    ? this.acquisitionEquipmentService.get(equipment)
                    : Promise.resolve(equipment)
                ).then(acqEq => {
                    let centerNode: CenterNode = (this.studyNode.centersNode.centers as CenterNode[]).find(cn => acqEq.center.id == cn.id);
                    centerNode?.open();
                });
            });

        });
    }

    private selectCoil(coil: number | Coil) {
        this.studyNodePromise.then(() => {
            this.studyNode.centersNode.open().then(() => {
                (typeof coil == 'number' 
                    ? this.coilService.get(coil)
                    : Promise.resolve(coil)
                ).then(coil => {
                    let centerNode: CenterNode = (this.studyNode.centersNode.centers as CenterNode[]).find(cn => coil.center.id == cn.id);
                    centerNode?.open();
                });
            });

        });
    }

    private selectQualitycard() {
        this.studyNodePromise.then(() => {
            this.studyNode.qualityCardsNode.open();
        });
    }

    private selectStudycard() {
        this.studyNodePromise.then(() => {
            this.studyNode.studyCardsNode.open();
        });
    }

    private selectUser() {
        this.studyNodePromise.then(() => {
            this.studyNode.membersNode.open();
        });
    }

    private initStudy(id: number): Promise<void> {
        if (this.study?.id == id) {
            return Promise.resolve();
        } else {
            let studyPromise: Promise<any> = this.studyService.get(id, null).then(study => this.study = study);
            let rightsPromise: Promise<void> = this.studyRightsService.getMyRightsForStudy(id).then(rights => {
                this.canAdminStudy =  this.keycloakService.isUserAdmin()
                    || (this.keycloakService.isUserExpert() && rights.includes(StudyUserRight.CAN_ADMINISTRATE));
            });
            return Promise.all([studyPromise, rightsPromise]).then(() => {
                this.studyNode = this.buildStudyNode(this.study, this.canAdminStudy);
                this.studyNodePromise.resolve();
                this.studyNode.open();
            });
        }
    }

    private sortSubjects(study: Study) {
        study.subjectStudyList = study.subjectStudyList.sort(
            function(a: SubjectStudy, b:SubjectStudy) {
                let aname = a.subjectStudyIdentifier ? a.subjectStudyIdentifier : a.subject.name;
                let bname = b.subjectStudyIdentifier ? b.subjectStudyIdentifier : b.subject.name;
                return aname.localeCompare(bname);
            });
    }

    public buildStudyNode(study: Study, canAdmin: boolean): StudyNode {
        let node: StudyNode;
        let subjects: SubjectNode[] = study.subjectStudyList.map(subjectStudy => {
            if(subjectStudy.subject.preclinical){
                return new PreclinicalSubjectNode(
                    node, 
                    subjectStudy.subject.id, 
                    this.subjectStudyPipe.transform(subjectStudy), 
                    subjectStudy.tags, 
                    UNLOADED, 
                    subjectStudy.qualityTag, 
                    canAdmin);
            } else {
                return new ClinicalSubjectNode(
                    node, 
                    subjectStudy.subject.id, 
                    this.subjectStudyPipe.transform(subjectStudy), 
                    subjectStudy.tags, 
                    UNLOADED, 
                    subjectStudy.qualityTag, 
                    canAdmin);
            }
        });
        let centers: CenterNode[] = study.studyCenterList.map(studyCenter => {
            return new CenterNode(node, studyCenter.center.id, studyCenter.center.name, UNLOADED, UNLOADED);
        });
        let members: MemberNode[] = study.studyUserList.map(studyUser => {
            let memberNode: MemberNode = null;
            let rights: RightNode[] = studyUser.studyUserRights.map(suRight => new RightNode(memberNode, null, StudyUserRight.getLabel(suRight)));
            memberNode = new MemberNode(node, studyUser.user?.id || studyUser.userId, studyUser.userName, rights);
            return memberNode;
        });
        members.sort((a: MemberNode, b: MemberNode) => {
            return a.label.toLowerCase().localeCompare(b.label.toLowerCase())
        })
        node = new StudyNode(
                null,
                study.id,
                study.name,
                subjects,
                centers,
                UNLOADED,
                UNLOADED,
                members);
        return node;
    }
}


export type NodeType = 'study' | 'subject' | 'examination' | 'acquisition' | 'dataset' | 'processing' | 'center' | 'equipment' | 'coil' | 'studycard' | 'qualitycard' | 'user' | 'dicomMetadata';

export class Selection {

    constructor(
        public id: number,
        public type: NodeType,
        public studyId: number[],
        public entity?: Entity
    ) {}

    isSelected(id: number, type: NodeType): boolean {
        return id == this.id && type == this.type;
    }

    static fromStudy(study: Study): Selection {
        return new Selection(study.id, 'study', [study.id], study);
    }

    static fromSubject(subject: Subject): Selection {
        return new Selection(subject.id, 'subject', subject.subjectStudyList.map(ss => ss.study.id), subject);
    }

    static fromExamination(examination: Examination): Selection {
        return new Selection(examination.id, 'examination', [examination.study.id], examination);
    }

    static fromAcquisition(acquisition: DatasetAcquisition): Selection {
        return new Selection(acquisition.id, 'acquisition', [acquisition.examination.study.id], acquisition);
    }

    static fromDataset(dataset: Dataset): Selection {
        return new Selection(dataset.id, 'dataset', [dataset.datasetProcessing ? dataset.datasetProcessing.outputDatasets?.[0]?.study.id : dataset.datasetAcquisition.examination.study.id], dataset);
    }

    static fromProcessing(processing: DatasetProcessing): Selection {
        return new Selection(processing.id, 'processing', [processing.studyId], processing);
    }

    static fromCenter(center: Center): Selection {
        return new Selection(center.id, 'center', center.studyCenterList.map(sc => sc.study.id), center);
    }

    static fromEquipment(equipment: AcquisitionEquipment): Selection {
        return new Selection(equipment.id, 'equipment', equipment.center.studyCenterList?.map(sc => sc.study.id), equipment);
    }

    static fromCoil(coil: Coil): Selection {
        return new Selection(coil.id, 'coil', coil.center.studyCenterList?.map(sc => sc.study.id), coil);
    }

    static fromStudycard(studycard: StudyCard): Selection {
        return new Selection(studycard.id, 'studycard', [studycard.study.id], studycard);
    }

    static fromQualitycard(qualitycard: QualityCard): Selection {
        return new Selection(qualitycard.id, 'qualitycard', [qualitycard.study.id], qualitycard);
    }

    static fromUser(user: User): Selection {
        return new Selection(user.id, 'user', user.studyUserList?.map(su => su.studyId), user);
    }

    static fromDicomMetadata(dataset: Dataset): Selection {
        return new Selection(dataset.id, 'dicomMetadata', [dataset.datasetAcquisition.examination.study.id], dataset);
    }
}