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
import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Location } from '@angular/common';
import { Subscription } from 'rxjs';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { slideDown } from '../../shared/animations/animations';
import { AcquisitionEquipmentNode, CenterNode, ClinicalSubjectNode, DatasetAcquisitionNode, DatasetNode, ExaminationNode, MemberNode, PreclinicalSubjectNode, ProcessingNode, RightNode, ShanoirNode, StudyNode, SubjectNode, UNLOADED } from '../../tree/tree.model';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { TreeService } from './tree.service';
import { DatasetAcquisitionService } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetProcessingService } from 'src/app/datasets/shared/dataset-processing.service';
import { AcquisitionEquipmentService } from 'src/app/acquisition-equipments/shared/acquisition-equipment.service';


@Component({
    selector: 'study-tree',
    templateUrl: 'study-tree.component.html',
    styleUrls: ['study-tree.component.css'],
    animations: [slideDown]
})

export class StudyTreeComponent implements OnDestroy {

    protected studyNode: StudyNode = null;
    private studyNodePromise: SuperPromise<void> = new SuperPromise();
    protected study: Study;
    protected subscriptions: Subscription[] = [];

    constructor(
            private breadcrumbsService: BreadcrumbsService,
            protected activatedRoute: ActivatedRoute,
            private studyService: StudyService,
            private datasetService: DatasetService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private datasetProcessingService: DatasetProcessingService,
            private acquisitionEquipmentService: AcquisitionEquipmentService,
            private location: Location,
            protected treeService: TreeService) {

        this.subscriptions.push(treeService.change.subscribe(selection => {
            if (selection?.type == 'study') {
                this.initStudy(selection.id).then(() => {
                    this.studyNode.subjectsNode.open();
                });
            } else {
                let studyLoaded: Promise<void>;
                console.log(selection.studyId)
                if (this.study?.id && selection.studyId?.includes(this.study?.id)) {
                    studyLoaded = Promise.resolve();
                } else if (selection.studyId?.[0]) {
                    studyLoaded = this.initStudy(selection.studyId[0]);
                }

                studyLoaded?.then(() => {
                    if (selection?.type == 'dataset') {
                        this.selectDataset(selection.id);
                    } else if (selection?.type == 'dicomMetadata') {
                        this.selectDicomMetadata(selection.id);
                    } else if (selection?.type == 'subject') {
                        this.selectSubject(selection.id);
                    } else if (selection?.type == 'acquisition') {
                        this.selectAcquisition(selection.id);
                    } else if (selection?.type == 'processing') {
                        this.selectProcessing(selection.id);
                    } else if (selection?.type == 'center') {
                        this.selectCenter(selection.id);
                    } else if (selection?.type == 'equipment') {
                        this.selectEquipment(selection.id);
                    } else if (selection?.type == 'qualitycard') {
                        this.selectQualitycard(selection.id);
                    } else if (selection?.type == 'studycard') {
                        this.selectStudycard(selection.id);
                    } else if (selection?.type == 'user') {
                        this.selectUser(selection.id);
                    }
                    
                });
            }
        }));    
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(sub => sub.unsubscribe());
    }

    selectDataset(id: number): Promise<DatasetNode> {
        return this.studyNodePromise.then(() => {
            return this.studyNode.subjectsNode.open().then(() => {
                return this.findDatasetParent(id).then(ret => {
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
                                                    if (ret.topParent.id != id) { // if sub processing/datasets 
                                                        let procNode: ProcessingNode = dsNode.processings[0] as ProcessingNode;
                                                        if (procNode) {
                                                            return (dsNode.processings[0] as ProcessingNode).open().then(() => {
                                                                return (procNode.datasets as DatasetNode[]).find(dsNd => dsNd.id == id);
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

    selectDicomMetadata(id: number) {
        this.selectDataset(id).then(parentDsNode => {
            parentDsNode?.open();
        });
    }

    findDatasetParent(childDatasetId: number, botomChild?: Dataset): Promise<{topParent: Dataset, bottomChild: Dataset}> {
        return this.datasetService.get(childDatasetId).then(ds => {
            if (!botomChild) botomChild = ds;
            if (ds.datasetProcessing) {
                if (!(ds.datasetProcessing.inputDatasets?.length > 0)) throw Error('no input ds on this processing');
                return this.findDatasetParent(ds.datasetProcessing.inputDatasets[0].id, botomChild);
            } else {
                return {topParent: ds, bottomChild: botomChild};
            }
        });
    }

    selectProcessing(id: number) {
        this.datasetProcessingService.get(id).then(proc => {
            this.selectDataset(proc.inputDatasets[0].id).then(parentDsNode => {
                parentDsNode?.open();
            });
        });
    }

    selectAcquisition(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.subjectsNode.open().then(() => {
                this.datasetAcquisitionService.get(id).then(dsa => {
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

    selectSubject(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.subjectsNode.open();
        });
    }

    selectCenter(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.centersNode.open();
        });
    }

    selectEquipment(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.centersNode.open().then(() => {
                this.acquisitionEquipmentService.get(id).then(acqEq => {
                    let centerNode: CenterNode = (this.studyNode.centersNode.centers as CenterNode[]).find(cn => acqEq.center.id == cn.id);
                    centerNode?.open();
                });
            });

        });
    }

    selectQualitycard(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.qualityCardsNode.open();
        });
    }

    selectStudycard(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.studyCardsNode.open();
        });
    }

    selectUser(id: number) {
        this.studyNodePromise.then(() => {
            this.studyNode.membersNode.open();
        });
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
            this.studyNode = new StudyNode(null, study.id, study.name, subjectNodes, centerNodes, UNLOADED, UNLOADED, memberNodes);
            this.studyNodePromise.resolve();
            this.studyNode.open();
        });
        return studyPromise;
    }

    // @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
    //     if (event.key == '²') {
    //         console.log('selection', this.selection);
    //         console.log('node', this.studyNode);
    //     }
    // }
}


