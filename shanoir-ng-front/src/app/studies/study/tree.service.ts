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
import { Coil } from "src/app/coils/shared/coil.model";
import { CoilService } from "src/app/coils/shared/coil.service";
import { DatasetAcquisitionService } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetProcessingService } from 'src/app/datasets/shared/dataset-processing.service';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { ExaminationService } from 'src/app/examinations/shared/examination.service';
import { PreclinicalSubject } from "src/app/preclinical/animalSubject/shared/preclinicalSubject.model";
import { Entity } from "src/app/shared/components/entity/entity.abstract";
import { KeycloakService } from "src/app/shared/keycloak/keycloak.service";
import { SuperPromise } from 'src/app/utils/super-promise';
import { AcquisitionEquipmentNode, CenterNode, CentersNode, ClinicalSubjectNode, CoilNode, DatasetAcquisitionNode, DatasetNode, ExaminationNode, MemberNode, MembersNode, MetadataNode, PreclinicalSubjectNode, ProcessingNode, QualityCardNode, RightNode, ShanoirNode, StudyCardNode, StudyNode, SubjectNode, SubjectsNode, UNLOADED } from '../../tree/tree.model';
import { StudyRightsService } from "../shared/study-rights.service";
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyService } from '../shared/study.service';
import { WaitBurstEnd } from "src/app/utils/wait-burst-end";

@Injectable()
export class TreeService {

    _selection: Selection = null;
    public studyNode: StudyNode = null;
    studyNodeOpenPromise: SuperPromise<void> = new SuperPromise();
    study: Study;
    studyPromise: SuperPromise<Study> = new SuperPromise();
    public studyNodeInit: SuperPromise<void> = new SuperPromise(); 
    private studyRights: StudyUserRight[]; 
    private _treeOpened: boolean = true;
    private _treeAvailable: boolean = false;
    selectedNode: ShanoirNode;

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
        if (!this._treeOpened && opened) {
            this.changeSelection();
        }
        this._treeOpened = opened;
        localStorage.setItem('treeOpened', this._treeOpened ? 'true' : 'false');
    }

    get canAdminStudy(): boolean {
        return this.studyRights.includes(StudyUserRight.CAN_ADMINISTRATE);
    }

    get canDownloadStudy(): boolean {
        return this.studyRights.includes(StudyUserRight.CAN_DOWNLOAD);
    }
    
    private openCloseBurst: WaitBurstEnd = new WaitBurstEnd(this.setTreeAvailable.bind(this), 100);
    private _lastTreeAvailable: boolean;
 
    get treeAvailable(): boolean {
        return this._treeAvailable;
    }

    set treeAvailable(treeAvailable: boolean) {
        this._lastTreeAvailable = treeAvailable;
        this.openCloseBurst.fire();
    }

    setTreeAvailable() {
        this._treeAvailable = this._lastTreeAvailable; 
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
            private router: Router) {

        if (localStorage.getItem('treeOpened') == undefined) {
            localStorage.setItem('treeOpened', 'true');
        }
        this.treeOpened = localStorage.getItem('treeOpened') == 'true';
        router.events.subscribe(event => {
            if (event instanceof ActivationStart) {
                setTimeout(() => {
                    this.treeAvailable = event?.snapshot?.data?.['treeAvailable'];
                });
            }
        });
    }

    updateTree() {
        // update everything
        let studyId: number = this.study?.id;
        this.study = null;
        this.initStudy(studyId);
    }

    removeCurrentNode() {
        const route: string = this.selectedNode.route;
        Object.entries(this.selectedNode.parent).forEach((entry, index) => {
            if (Array.isArray(entry[1])) {
                let i: number = entry[1].findIndex(node => node.route == route);
                entry[1].splice(i, 1);
            }
        });
    }

    goToParent() {
        this.router.navigate([this.selectedNode?.parent?.route]);
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
            } else {
                this.treeAvailable = false;
            }
            
            Promise.all([studyLoaded]).then(() => {
                return this.selectNode(this.selection)
            }).then(node => {
                this.selectedNode = node;
                this.treeAvailable = !!this.selectedNode;
            });
            
        }
    }

    selectNode(selection: Selection): Promise<ShanoirNode> {
        if (selection?.type == 'dataset') {
            return this.selectDataset(selection.entity as Dataset);
        } else if (selection?.type == 'dicomMetadata') {
            return this.selectDicomMetadata(selection.entity as Dataset);
        } else if (selection?.type == 'subject') {
            return this.selectSubject(selection.id);
        } else if (selection?.type == 'acquisition') {
            return this.selectAcquisition(selection.entity as DatasetAcquisition);
        } else if (selection?.type == 'processing') {
            return this.selectProcessing(selection.entity as DatasetProcessing);
        } else if (selection?.type == 'examination') {
            return this.selectExamination(selection.entity as Examination);
        } else if (selection?.type == 'center') {
            return this.selectCenter(selection.id);
        } else if (selection?.type == 'equipment') {
            return this.selectEquipment(selection.entity as AcquisitionEquipment);
        } else if (selection?.type == 'qualitycard') {
            return this.selectQualitycard(selection.id);
        } else if (selection?.type == 'studycard') {
            return this.selectStudycard(selection.id);
        } else if (selection?.type == 'user') {
            return this.selectUser(selection.id);
        } else if (selection?.type == 'coil') {
            return this.selectCoil(selection.entity as Coil);
        }
    }

    private selectDataset(dataset: number | Dataset): Promise<DatasetNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                return this.findDatasetParent(dataset).then(ret => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[])?.find(sn => {
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
                                            let dsNode: DatasetNode = (acqNode.datasets as DatasetNode[])?.find(acqDs => acqDs.id == ret.topParent.id);
                                            if (dsNode) {
                                                return dsNode.open().then(() => {
                                                    if (ret.topParent.id != (typeof dataset == 'number' ? dataset : dataset.id)) { // if sub processing/datasets 
                                                        let procNode: ProcessingNode = (dsNode.processings as ProcessingNode[])
                                                            .find(proc => (proc.datasets as DatasetNode[])?.find(outDs => outDs.id == (typeof dataset == 'number' ? dataset : dataset.id)));
                                                        if (procNode) {
                                                            return procNode.open().then(() => {
                                                                return (procNode.datasets as DatasetNode[])?.find(dsNd => dsNd.id == (typeof dataset == 'number' ? dataset : dataset.id));
                                                            });
                                                        }
                                                    } else {
                                                        return dsNode;
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

    private selectDicomMetadata(ds: number | Dataset): Promise<MetadataNode> {
        return this.selectDataset(ds).then(parentDsNode => {
            parentDsNode?.open();
            return new MetadataNode(parentDsNode, parentDsNode?.id, 'Dicom Metadata')
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

    private selectProcessing(processing: number | DatasetProcessing): Promise<ProcessingNode> {
        let processingPromise: Promise<DatasetProcessing>;
        if (typeof processing == 'number') {
            processingPromise = this.datasetProcessingService.get(processing);
        } else {
            processingPromise = Promise.resolve(processing);
        }
        return processingPromise.then(proc => {
            return this.selectDataset(proc.inputDatasets[0].id).then(parentDsNode => {
                return parentDsNode?.open().then(() => {
                    return (parentDsNode.processings as ProcessingNode[])?.find(pnode => pnode.id == proc.id);
                });
            });
        });
    }

    private selectAcquisition(acq: number | DatasetAcquisition): Promise<DatasetAcquisitionNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                let acqPromise: Promise<DatasetAcquisition>;
                if (typeof acq == 'number') {
                    acqPromise = this.datasetAcquisitionService.get(acq);
                } else {
                    acqPromise = Promise.resolve(acq);
                }
                return acqPromise.then(dsa => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[])?.find(sn => sn.id == dsa.examination?.subject?.id);
                    if (subjectNode) {
                        return subjectNode.open().then(() => {
                            let examNode: ExaminationNode = (subjectNode.examinations as ExaminationNode[])?.find(exam => exam.id == dsa.examination?.id);
                            if (examNode) {
                                return examNode.open().then(() => {
                                    return (examNode.datasetAcquisitions as DatasetAcquisitionNode[])?.find(dsan => dsan.id == dsa.id);
                                });
                            }
                        });
                    }
                });
            });
        });
    }

    private selectExamination(examination: number | Examination): Promise<ExaminationNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                let examPromise: Promise<Examination>;
                if (typeof examination == 'number') {
                    examPromise = this.examinationService.get(examination);
                } else {
                    examPromise = Promise.resolve(examination);
                }
                return examPromise.then(exam => {
                    let subjectNode: SubjectNode = (this.studyNode.subjectsNode.subjects as SubjectNode[])?.find(sn => sn.id == exam.subject?.id);
                    if (subjectNode) {
                        return subjectNode.open().then(() => {
                            return (subjectNode.examinations as ExaminationNode[])?.find(en => en.id == exam.id);
                        });
                    }
                });
            });
        });
    }

    private selectSubject(subjectId: number): Promise<SubjectNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                return (this.studyNode.subjectsNode.subjects as SubjectNode[])?.find(sn => sn.id == subjectId);
            });
        });
    }

    private selectCenter(centerId: number): Promise<CenterNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.centersNode.open().then(() => {
                return (this.studyNode.centersNode.centers as CenterNode[])?.find(cn => cn.id == centerId);
            });
        });
    }

    private selectEquipment(equipment: number | AcquisitionEquipment): Promise<AcquisitionEquipmentNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.centersNode.open().then(() => {
                return (typeof equipment == 'number' 
                    ? this.acquisitionEquipmentService.get(equipment)
                    : Promise.resolve(equipment)
                ).then(acqEq => {
                    let centerNode: CenterNode = (this.studyNode.centersNode.centers as CenterNode[])?.find(cn => acqEq.center.id == cn.id);
                    return centerNode?.open().then(() => {
                        if (centerNode.acquisitionEquipments != UNLOADED) {
                            return (centerNode.acquisitionEquipments as AcquisitionEquipmentNode[])?.find(aen => aen.id == acqEq.id);
                        } else return null;
                    });
                });
            });
        });
    }

    private selectCoil(coil: number | Coil): Promise<CoilNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.centersNode.open().then(() => {
                return (typeof coil == 'number' 
                    ? this.coilService.get(coil)
                    : Promise.resolve(coil)
                ).then(coil => {
                    let centerNode: CenterNode = (this.studyNode.centersNode.centers as CenterNode[])?.find(cn => coil.center.id == cn.id);
                    return centerNode?.open().then(() => {
                        return (centerNode.coils as CoilNode[])?.find(cn => cn.id == coil.id);
                    });
                });
            });

        });
    }

    private selectQualitycard(qualityCardId: number): Promise<QualityCardNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.qualityCardsNode.open().then(() => {
                return (this.studyNode.qualityCardsNode.cards as QualityCardNode[])?.find(qcn => qcn.id == qualityCardId);
            });
        });
    }

    private selectStudycard(studyCardId: number): Promise<StudyCardNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.studyCardsNode.open().then(() => {
                return (this.studyNode.studyCardsNode.cards as StudyCardNode[])?.find(scn => scn.id == studyCardId);
            });
        });
    }

    private selectUser(userId: number): Promise<MemberNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.membersNode.open().then(() => {
                return (this.studyNode.membersNode.members as MemberNode[])?.find(mn => mn.id == userId);
            });
        });
    }

    private initStudy(id: number): Promise<void> {
        if (this.study?.id == id) {
            return Promise.resolve();
        } else {
            this.studyNodeOpenPromise = new SuperPromise(); 
            this.studyNodeInit = new SuperPromise();
            let studyPromise: Promise<void> = this.studyService.get(id, null).then(study => {
                this.study = study;
                this.studyPromise = new SuperPromise();
                this.studyPromise.resolve(study);
            });

            let rightsPromise: Promise<StudyUserRight[]> = (this.keycloakService.isUserAdmin
                ? Promise.resolve(StudyUserRight.all())
                : this.studyRightsService.getMyRightsForStudy(id)
            ).then(rights => {
                this.studyRights = rights;
                return rights;
            });
            return Promise.all([studyPromise, rightsPromise]).then(() => {
                this.studyNode = this.buildStudyNode(this.study, this.studyRights);
                return this.studyNodeInit.then(() => {
                    return this.studyNode.open().then(() => {
                        this.studyNodeOpenPromise.resolve();
                    });
                });
            });
        }
    }

    public buildStudyNode(study: Study, rights: StudyUserRight[]): StudyNode {
        let studyNode: StudyNode = new StudyNode(
            null,
            study.id,
            study.name,
            null, //subjects
            null, //centers,
            UNLOADED,
            UNLOADED,
            null, //members
            rights
        );
            
        let subjects: SubjectNode[] = study.subjectStudyList.map(subjectStudy => {
            if(subjectStudy.subject.preclinical){
                return PreclinicalSubjectNode.fromSubjectStudy(
                    subjectStudy, 
                    studyNode, 
                    this.canAdminStudy, 
                    this.canDownloadStudy
                );
            } else {
                return ClinicalSubjectNode.fromSubjectStudy(
                    subjectStudy, 
                    studyNode, 
                    this.canAdminStudy, 
                    this.canDownloadStudy
                );
            }
        });
        let centers: CenterNode[] = study.studyCenterList.map(studyCenter => {
            return new CenterNode(studyNode, studyCenter.center.id, studyCenter.center.name, UNLOADED, UNLOADED);
        });
        let members: MemberNode[] = study.studyUserList.map(studyUser => {
            let memberNode: MemberNode = null;
            let rights: RightNode[] = studyUser.studyUserRights.map(suRight => new RightNode(memberNode, null, StudyUserRight.getLabel(suRight)));
            memberNode = new MemberNode(studyNode, studyUser.user?.id || studyUser.userId, studyUser.userName, rights);
            return memberNode;
        });
        members.sort((a: MemberNode, b: MemberNode) => {
            return a.label.toLowerCase().localeCompare(b.label.toLowerCase())
        })
        studyNode.subjectsNode = new SubjectsNode(studyNode, null, 'Subjects', subjects);
        studyNode.centersNode = new CentersNode(studyNode, null, 'Centers', centers);
        studyNode.membersNode = new MembersNode(studyNode, null, 'Members', members);
        studyNode.membersNode.open();
        return studyNode;
    }

    unSelectAll() {
        this.unSelectNode(this.studyNode);
    }

    /**
     * Unselect a ShanoirNode and it's children
     * @param node 
     */
    unSelectNode(node: ShanoirNode) {
        node.selected = false;
        Object.entries(node).forEach(attr => {
            if (attr[0] != 'parent' && attr[1] instanceof ShanoirNode) {
                this.unSelectNode(attr[1]);
            } else if (Array.isArray(attr[1]) && attr[1][0] instanceof ShanoirNode) {
                attr[1].forEach(sn => this.unSelectNode(sn));
            }
        });
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

    static fromPreclinicalSubject(preclinicalSubject: PreclinicalSubject): Selection {
        return new Selection(preclinicalSubject.subject.id, 'subject', preclinicalSubject.subject.subjectStudyList.map(ss => ss.study.id), preclinicalSubject.subject);
    }

    static fromExamination(examination: Examination): Selection {
        return new Selection(examination.id, 'examination', [examination.study.id], examination);
    }

    static fromAcquisition(acquisition: DatasetAcquisition): Selection {
        return new Selection(acquisition.id, 'acquisition', [acquisition.examination.study.id], acquisition);
    }

    static fromDataset(dataset: Dataset): Selection {
        return new Selection(dataset.id, 'dataset', dataset.datasetProcessing ? dataset.datasetProcessing.outputDatasets?.map(ods => ods.study.id) : [dataset.datasetAcquisition.examination.study.id], dataset);
    }

    static fromProcessing(processing: DatasetProcessing): Selection {
        return new Selection(processing.id, 'processing', [processing.studyId], processing);
    }

    static fromCenter(center: Center): Selection {
        return new Selection(center.id, 'center', center.studyCenterList.map(sc => sc.study.id), center);
    }

    static fromEquipment(equipment: AcquisitionEquipment): Selection {
        return new Selection(equipment.id, 'equipment', equipment.center.studyCenterList?.map(sc => sc.study.id), equipment, );
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