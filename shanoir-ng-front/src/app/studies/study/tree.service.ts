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
import { Subject as RxjsSubject } from 'rxjs';

import { AcquisitionEquipment } from 'src/app/acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from 'src/app/acquisition-equipments/shared/acquisition-equipment.service';
import { Center } from 'src/app/centers/shared/center.model';
import { Coil } from "src/app/coils/shared/coil.model";
import { CoilService } from "src/app/coils/shared/coil.service";
import { DatasetAcquisition } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetProcessing } from 'src/app/datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from 'src/app/datasets/shared/dataset-processing.service';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { Examination } from 'src/app/examinations/shared/examination.model';
import { ExaminationService } from 'src/app/examinations/shared/examination.service';
import { PreclinicalSubject } from "src/app/preclinical/animalSubject/shared/preclinicalSubject.model";
import { Entity } from "src/app/shared/components/entity/entity.abstract";
import { KeycloakService } from "src/app/shared/keycloak/keycloak.service";
import { QualityCard } from 'src/app/study-cards/shared/quality-card.model';
import { StudyCard } from 'src/app/study-cards/shared/study-card.model';
import { Subject } from "src/app/subjects/shared/subject.model";
import { User } from 'src/app/users/shared/user.model';

import { SuperPromise } from '../../utils/super-promise';
import { Study } from "../shared/study.model";
import { AcquisitionEquipmentNode, CenterNode, CentersNode, ClinicalSubjectNode, CoilNode, DatasetAcquisitionNode, DatasetNode, ExaminationNode, MemberNode, MembersNode, MetadataNode, PreclinicalSubjectNode, ProcessingNode, QualityCardNode, RightNode, ShanoirNode, StudyCardNode, StudyNode, SubjectNode, SubjectsNode, UNLOADED } from '../../tree/tree.model';
import { StudyRightsService } from "../shared/study-rights.service";
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyService } from '../shared/study.service';

export type DatasetForChain =  {id: number, outProcessing?: ProcessingForChain, acqId?: number, examId?: number, subjectId?: number, studyId?: number};
export type ProcessingForChain =  {id: number, outDataset: DatasetForChain};

@Injectable()
export class TreeService {
    
    private selection: Selection = null;
    public studyNode: StudyNode = null;
    studyNodeOpenPromise: SuperPromise<void> = new SuperPromise();
    study: Study;
    studyPromise: SuperPromise<Study> = new SuperPromise();
    public studyNodeInit: SuperPromise<void> = new SuperPromise(); 
    private studyRights: StudyUserRight[]; 
    private _treeOpened: boolean = true;
    private _treeAvailable: boolean = false;
    public previouslyOpened: boolean = false;
    selectedNode: ShanoirNode;
    onScrollToSelected: RxjsSubject<ShanoirNode> = new RxjsSubject();
    studyLoading: boolean = false;
    reopenAfterNavigation: boolean = false;
    
    isSelected(id: number, type: NodeType): boolean {
        return this.selection?.isSelected(id, type);
    }
    
    get treeOpened(): boolean {
        return this._treeOpened;
    }

    set treeOpened(opened: boolean) {
        this.previouslyOpened = this._treeOpened;
        if (!this._treeOpened && opened) {
            this.changeSelection();
            if (this.selection?.studyId && this.selection?.studyId?.includes(this.studyNode?.id)) {
                this.studyNodeInit.then(() => {
                    this._treeOpened = opened;
                });
            } else {
                this._treeOpened = opened;
            }
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
 
    get treeAvailable(): boolean {
        return this._treeAvailable;
    }

    set treeAvailable(treeAvailable: boolean) {
        this._treeAvailable = treeAvailable;
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
                if (this.reopenAfterNavigation) {
                    this.treeOpened = true;
                    this.reopenAfterNavigation = false;
                }
                setTimeout(() => {
                    let newState: boolean = event?.snapshot?.data?.['treeAvailable'];
                    if (newState && !this.treeAvailable) {
                        this.treeAvailable = true;
                    } else if (!newState && this.treeAvailable) {
                        this.treeAvailable = false;
                    }
                });
            }
        });
    }

    updateTree() {
        // update everything
        let studyId: number = this.study?.id;
        this.study = null;
        this.initStudy(studyId).then(() => {
            this.changeSelection();
        });
    }

    collapseAll() {
        this.collapseNode(this.studyNode, false);
    }

    private collapseNode(node: ShanoirNode, collapseRoot: boolean = true): Promise<void> {
        return Promise.all(
            Object.getOwnPropertyNames(node).map(propLabel => {
                let prop = node[propLabel];
                if (prop instanceof ShanoirNode && propLabel != 'parent') {
                    return this.collapseNode(prop);
                } else if (Array.isArray(prop) && prop[0] instanceof ShanoirNode) {
                    return Promise.all(prop.map(el => this.collapseNode(el))).then();
                }
            })
        ).then(() => {
            if (collapseRoot) {
                return node.close();
            }
        });
        
    }

    scrollToSelected() {
        return this.selectNode(this.selection).then(() => {
            this.scrollTo(this.selectedNode);
        })
    }

    scrollTo(node: ShanoirNode) {
        setTimeout(() => {
            this.onScrollToSelected.next(node);
        })
    }

    removeCurrentNode() {
        if (this.selectedNode?.parent) {
            const route: string = this.selectedNode.route;
            Object.entries(this.selectedNode.parent).forEach((entry, index) => {
                if (Array.isArray(entry[1])) {
                    let i: number = entry[1].findIndex(node => node.route == route);
                    entry[1].splice(i, 1);
                }
            });
        }
    }

    goToParent() {
        if (this.selectedNode instanceof StudyNode) {
            this.router.navigate(['/study/list']);
        } else {
            this.router.navigate([this.selectedNode?.parent?.route]);
        }
    }

    activateTree(activatedRoute: ActivatedRoute) {
        activatedRoute.snapshot.data['treeAvailable'] = true;
    }
    
    private changeSelection(): Promise<ShanoirNode> {
        if (this.selection?.type == 'study') {
            return this.initStudy(this.selection.id).then(() => {
                this.studyNode.subjectsNode.open();
                this.selectedNode = this.studyNode;
                this.treeAvailable = !!this.selectedNode;
                return this.studyNode;
            });
        } else {
            let studyLoaded: Promise<void>;
            if (this.study?.id && this.selection?.studyId?.includes(this.study?.id)) {
                studyLoaded = Promise.resolve();
            } else if (this.selection?.studyId?.[0]) {
                studyLoaded = this.initStudy(this.selection.studyId[0]);
            } else {
                this.treeAvailable = false;
            }
            
            return Promise.all([studyLoaded]).then(() => {
                return this.selectNode(this.selection)
            }).then(node => {
                this.selectedNode = node;
                this.treeAvailable = !!this.selectedNode;
                return node;
            });
            
        }
    }

    select(selection: Selection): Promise<void> {
        if (selection.equals(this.selection)) return Promise.resolve();
        this.selection = selection;
        if (this.treeOpened) {
            return this.changeSelection().then(() => {
                this.scrollToSelected();
            });
        }
    }

    private selectNode(selection: Selection): Promise<ShanoirNode> {
        let node: Promise<ShanoirNode>;
        if (selection?.type == 'dataset') {
            node = this.selectDataset(selection.entity as Dataset);
        } else if (selection?.type == 'dicomMetadata') {
            node = this.selectDicomMetadata(selection.entity as Dataset);
        } else if (selection?.type == 'subject') {
            node = this.selectSubject(selection.id);
        } else if (selection?.type == 'acquisition') {
            node = this.selectAcquisition(selection.entity as DatasetAcquisition);
        } else if (selection?.type == 'processing') {
            node = this.selectProcessing(selection.entity as DatasetProcessing);
        } else if (selection?.type == 'examination') {
            node = this.selectExamination(selection.entity as Examination);
        } else if (selection?.type == 'center') {
            node = this.selectCenter(selection.id);
        } else if (selection?.type == 'equipment') {
            node = this.selectEquipment(selection.entity as AcquisitionEquipment);
        } else if (selection?.type == 'qualitycard') {
            node = this.selectQualitycard(selection.id);
        } else if (selection?.type == 'studycard') {
            node = this.selectStudycard(selection.id);
        } else if (selection?.type == 'user') {
            node = this.selectUser(selection.id);
        } else if (selection?.type == 'coil') {
            node = this.selectCoil(selection.entity as Coil);
        } else node = Promise.resolve(null);
        node.then(n => {
            if(!!n) n.fake = false;
        });
        return node;
    }

    private selectDataset(dataset: number | Dataset): Promise<DatasetNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                return this.findDatasetChainFromBottomDataset(dataset).then(ret => {
                    if (this.studyNode.subjectsNode.subjects != UNLOADED) {
                        let subjectNode: SubjectNode = this.studyNode.subjectsNode.subjects?.find(sn => {
                            return sn.id == ret.subjectId;
                        });
                        if (subjectNode) {
                            subjectNode.fake = false;
                            this.scrollTo(subjectNode);
                            return subjectNode.open().then(() => {
                                if (subjectNode.examinations != UNLOADED) {
                                    let examNode: ExaminationNode = subjectNode.examinations?.find(exam => exam.id == ret.examId);
                                    if (examNode) {
                                        this.scrollTo(examNode);
                                        return examNode.open().then(() => {
                                            if (examNode.datasetAcquisitions != UNLOADED) {
                                                let acqNode: DatasetAcquisitionNode = examNode.datasetAcquisitions?.find(acq => acq.id == ret.acqId);
                                                if (acqNode) {
                                                    this.scrollTo(acqNode);
                                                    return acqNode.open()?.then(() => {
                                                        if (acqNode.datasets != UNLOADED) {
                                                            let dsNode: DatasetNode = acqNode.datasets?.find(acqDs => acqDs.id == ret.id);
                                                            if (dsNode) {
                                                                this.scrollTo(dsNode);
                                                                return dsNode.open().then(() => {
                                                                    return this.openDatasetProcessingChain(dsNode, ret);
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            });
        });
    }

    private openDatasetProcessingChain(dsNode: DatasetNode, chain: DatasetForChain): any {
        if (chain.outProcessing) { // if sub processing/datasets 
            if (dsNode.processings != UNLOADED) {
                let procNode: ProcessingNode = dsNode.processings.find(proc => {
                    return proc.id == chain.outProcessing.id;
                });
                if (procNode) {
                    return procNode.open().then(() => {
                        if (procNode.datasets != UNLOADED) {
                            let childDsNode: DatasetNode = procNode.datasets?.find(dsNd => dsNd.id == chain.outProcessing?.outDataset?.id);
                            return this.openDatasetProcessingChain(childDsNode, chain.outProcessing.outDataset);
                        }
                    });
                }
            }
        } else {
            return dsNode;
        }
    }

    private selectDicomMetadata(ds: number | Dataset): Promise<MetadataNode> {
        return this.selectDataset(ds).then(parentDsNode => {
            return parentDsNode?.open().then(() => {
                return parentDsNode.metadata;
            });
        });
    }

    public findDatasetChainFromBottomDataset(dataset: Dataset | number): Promise<DatasetForChain> {
        let datasetPromise: Promise<Dataset>;
        if (typeof dataset == 'number') {
            datasetPromise = this.datasetService.get(dataset);
        } else {
            datasetPromise = Promise.resolve(dataset);
        }
        return datasetPromise.then(ds => {
            if (ds.hasProcessing) {
                return this.findDatasetChain({id: ds.datasetProcessing.inputDatasets[0].id, outProcessing: {id: ds.datasetProcessing.id, outDataset: {id: ds.id}}})
            } else {
                return Promise.resolve({id: ds.id,
                    acqId: ds.datasetAcquisition?.id,
                    examId: ds.datasetAcquisition?.examination?.id,
                    subjectId: ds.datasetAcquisition?.examination?.subject?.id || ds.subject?.id,
                    studyId: ds.datasetAcquisition?.examination?.study?.id || ds.study?.id});
            }
        });
    }

    private findDatasetChain(bottomChain: DatasetForChain): Promise<DatasetForChain> {
        let childDatasetPromise: Promise<Dataset>;
        if (!bottomChain) return Promise.reject('cannot have null or undefined as input');
        return this.datasetService.get(bottomChain.id).then(ds => {
            if (ds.hasProcessing) {
                return this.findDatasetChain({id: ds.datasetProcessing.inputDatasets[0].id, outProcessing: {id: ds.datasetProcessing.id, outDataset: bottomChain}})
            } else {
                bottomChain.acqId = ds.datasetAcquisition?.id;
                bottomChain.examId = ds.datasetAcquisition?.examination?.id;
                bottomChain.subjectId = ds.datasetAcquisition?.examination?.subject?.id || ds.subject?.id;
                bottomChain.studyId = ds.datasetAcquisition?.examination?.study?.id || ds.study?.id;
                return Promise.resolve(bottomChain);
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
                    if (parentDsNode.processings != UNLOADED) {
                        return parentDsNode.processings?.find(pnode => pnode.id == proc.id);
                    }
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
                    if (this.studyNode.subjectsNode.subjects != UNLOADED) {
                        let subjectNode: SubjectNode = this.studyNode.subjectsNode.subjects?.find(sn => sn.id == dsa.examination?.subject?.id);
                        if (subjectNode) {
                            this.scrollTo(subjectNode);
                            return subjectNode.open().then(() => {
                                if (subjectNode.examinations != UNLOADED) {
                                    let examNode: ExaminationNode = subjectNode.examinations?.find(exam => exam.id == dsa.examination?.id);
                                    if (examNode) {
                                        this.scrollTo(examNode);
                                        return examNode.open().then(() => {
                                            if (examNode.datasetAcquisitions != UNLOADED) {
                                                return examNode.datasetAcquisitions?.find(dsan => dsan.id == dsa.id);
                                            }
                                        });
                                    }
                                }
                            });
                        }
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
                    if (this.studyNode.subjectsNode.subjects != UNLOADED) {
                        let subjectNode: SubjectNode = this.studyNode.subjectsNode.subjects?.find(sn => sn.id == exam.subject?.id);
                        if (subjectNode) {
                            this.scrollTo(subjectNode);
                            return subjectNode.open().then(() => {
                                if (subjectNode.examinations != UNLOADED) {
                                    return subjectNode.examinations?.find(en => en.id == exam.id);
                                }
                            });
                        }
                    }                    
                });
            });
        });
    }

    private selectSubject(subjectId: number): Promise<SubjectNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                if (this.studyNode.subjectsNode.subjects != UNLOADED) {
                    return this.studyNode.subjectsNode.subjects?.find(sn => sn.id == subjectId);
                }
            });
        });
    }

    private selectCenter(centerId: number): Promise<CenterNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.centersNode.open().then(() => {
                if (this.studyNode.centersNode.centers != UNLOADED) {
                    return this.studyNode.centersNode.centers?.find(cn => cn.id == centerId);
                }
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
                    if (this.studyNode.centersNode.centers != UNLOADED) {
                        let centerNode: CenterNode = this.studyNode.centersNode.centers?.find(cn => acqEq.center.id == cn.id);
                        return centerNode?.open().then(() => {
                            if (centerNode.acquisitionEquipments != UNLOADED) {
                                return centerNode.acquisitionEquipments?.find(aen => aen.id == acqEq.id);
                            }
                        });
                    }
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
                    if (this.studyNode.centersNode.centers != UNLOADED) {
                        let centerNode: CenterNode = this.studyNode.centersNode.centers?.find(cn => coil.center.id == cn.id);
                        return centerNode?.open().then(() => {
                            if (centerNode.coils != UNLOADED) {
                                return centerNode.coils?.find(cn => cn.id == coil.id);
                            }
                        });
                    }
                });
            });

        });
    }

    private selectQualitycard(qualityCardId: number): Promise<QualityCardNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.qualityCardsNode.open().then(() => {
                if (this.studyNode.qualityCardsNode.cards != UNLOADED) {
                    return (this.studyNode.qualityCardsNode.cards as QualityCardNode[])?.find(qcn => qcn.id == qualityCardId);
                }
            });
        });
    }

    private selectStudycard(studyCardId: number): Promise<StudyCardNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.studyCardsNode.open().then(() => {
                if (this.studyNode.studyCardsNode.cards != UNLOADED) {
                    return (this.studyNode.studyCardsNode.cards as StudyCardNode[])?.find(scn => scn.id == studyCardId);
                }
            });
        });
    }

    private selectUser(userId: number): Promise<MemberNode> {
        return this.studyNodeOpenPromise.then(() => {
            return this.studyNode.membersNode.open().then(() => {
                if (this.studyNode.membersNode.members != UNLOADED) {
                    return this.studyNode.membersNode.members?.find(mn => mn.id == userId);
                }
            });
        });
    }

    private initStudy(id: number): Promise<void> {
        if (this.study?.id == id) {
            return Promise.resolve();
        } else {
            this.studyLoading = true;
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
                }).finally(() => {
                    this.studyLoading = false;
                });
            }).finally(() => {
                this.studyLoading = false;
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
        }).map(s => {
            s.fake = !this.isSelected(s.id, 'subject');
            return s;
        });
        let centers: CenterNode[] = study.studyCenterList.map(studyCenter => {
            return new CenterNode(studyNode, studyCenter.center.id, studyCenter.center.name, UNLOADED, UNLOADED);
        });
        let members: MemberNode[] = study.studyUserList.map(studyUser => {
            let memberNode: MemberNode = null;
            let rights: RightNode[] = studyUser.studyUserRights.map(suRight => new RightNode(memberNode, null, StudyUserRight.getLabel(suRight)));
            memberNode = new MemberNode(studyNode, studyUser.user?.id || studyUser.userId, studyUser.userName, rights);
            memberNode.fake = !this.isSelected(memberNode.id, 'user');
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

    memberStudyOpenedAndTreeActive(userId: number): boolean {
        return this.treeOpened && this.treeAvailable
            && this.studyNode?.membersNode?.members 
            && this.studyNode?.membersNode?.members != UNLOADED
            && !!(this.studyNode?.membersNode?.members as MemberNode[])?.find(member => member.id == userId);
    }

    closeTemporarily() {
        this.treeOpened = false;
        this.reopenAfterNavigation = true;
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

    equals(otherSelection: Selection): boolean {
        return otherSelection && this.id == otherSelection.id && this.type == otherSelection.type;
    }

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
        return new Selection(dataset.id, 'dataset', dataset.datasetProcessing ? [dataset.study.id] : [dataset.datasetAcquisition.examination.study.id], dataset);
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