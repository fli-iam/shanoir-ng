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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { StudyCardService } from '../../study-cards/shared/study-card.service';

import { QualityCardService } from 'src/app/study-cards/shared/quality-card.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { SubjectStudyPipe } from '../../subjects/shared/subject-study.pipe';
import {
    CenterNode,
    ClinicalSubjectNode,
    MemberNode,
    PreclinicalSubjectNode,
    QualityCardNode,
    RightNode,
    StudyCardNode,
    StudyNode,
    SubjectNode,
    UNLOADED
} from '../../tree/tree.model';
import { StudyRightsService } from "../shared/study-rights.service";
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { Selection, TreeService } from '../study/tree.service';

@Component({
    selector: 'study-node',
    templateUrl: 'study-node.component.html'
})

export class StudyNodeComponent implements OnChanges {

    @Input() input: StudyNode | Study;
    @Output() nodeInit: EventEmitter<StudyNode> = new EventEmitter();
    @Output() selectedChange: EventEmitter<StudyNode> = new EventEmitter();
    node: StudyNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    studyCardsLoading: boolean = false;
    qualityCardsLoading: boolean = false;
    showDetails: boolean;
    @Input() canAdmin: boolean;
    @Input() hasBox: boolean = false;
    detailsPath: string = '/study/details/';
    @Input() withMenu: boolean = true;

    constructor(
            private router: Router,
            private subjectStudyPipe: SubjectStudyPipe,
            private studyCardService: StudyCardService,
            private qualityCardService: QualityCardService,
            private keycloakService: KeycloakService,
            private studyRightsService: StudyRightsService,
            protected treeService: TreeService) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            let canAdminPromise: Promise<void>;
            if (this.canAdmin == undefined) {
                canAdminPromise = this.studyRightsService.getMyRightsForStudy(this.input.id).then(rights => {
                    this.canAdmin =  this.keycloakService.isUserAdmin()
                        || (this.keycloakService.isUserExpert() && rights.includes(StudyUserRight.CAN_ADMINISTRATE));
                });
            } else {
                canAdminPromise = Promise.resolve();
            }
            canAdminPromise.then(() => {    
                if (this.input instanceof StudyNode) {
                    this.node = this.input;
                } else {
                    let subjects: SubjectNode[] = this.input.subjectStudyList.map(subjectStudy => {
                        if(subjectStudy.subject.preclinical){
                            return new PreclinicalSubjectNode(this.node, subjectStudy.subject.id, this.subjectStudyPipe.transform(subjectStudy), subjectStudy.tags, UNLOADED, subjectStudy.qualityTag, this.canAdmin);
                        }
                        return new ClinicalSubjectNode(this.node, subjectStudy.subject.id, this.subjectStudyPipe.transform(subjectStudy), subjectStudy.tags, UNLOADED, subjectStudy.qualityTag, this.canAdmin);
                    });
                    let centers: CenterNode[] = this.input.studyCenterList.map(studyCenter => {
                        return new CenterNode(this.node, studyCenter.center.id, studyCenter.center.name, UNLOADED, UNLOADED);
                    });
                    let members: MemberNode[] = this.input.studyUserList.map(studyUser => {
                        let memberNode: MemberNode = null;
                        let rights: RightNode[] = studyUser.studyUserRights.map(suRight => new RightNode(memberNode, null, StudyUserRight.getLabel(suRight)));
                        memberNode = new MemberNode(this.node, studyUser.userId, studyUser.userName, []);
                        return memberNode;
                    });
                    members.sort((a: MemberNode, b: MemberNode) => {
                        return a.label.toLowerCase().localeCompare(b.label.toLowerCase())
                    })
    
                    this.node = new StudyNode(
                            null,
                            this.input.id,
                            this.input.name,
                            subjects,
                            centers,
                            UNLOADED,
                            UNLOADED,
                            members);  // members
                }
                this.nodeInit.emit(this.node);
                this.showDetails = this.router.url != this.detailsPath  + this.node.id;
            })
        }
    }
    
    hasDependency(dependencyArr: any[] | UNLOADED): boolean | 'unknown' {
        if (!dependencyArr) return false;
        else if (dependencyArr == UNLOADED) return 'unknown';
        else return dependencyArr.length > 0;
    }

    loadStudyCards() {
        if (this.node.studyCardsNode.cards == UNLOADED) {
            this.studyCardsLoading = true;
            this.studyCardService.getAllForStudy(this.node.id).then(studyCards => {
                if (studyCards) {
                   this.node.studyCardsNode.cards = studyCards.map(studyCard => new StudyCardNode(this.node, studyCard.id, studyCard.name, this.canAdmin));
                } else this.node.studyCardsNode.cards = [];
                this.studyCardsLoading = false;
                this.node.studyCardsNode.open();
            }).catch(() => this.studyCardsLoading = false);
        }
    }

    loadQualityCards() {
        if (this.node.qualityCardsNode.cards == UNLOADED) {
            this.qualityCardsLoading = true;
            this.qualityCardService.getAllForStudy(this.node.id).then(qualityCards => {
                if (qualityCards) {
                   this.node.qualityCardsNode.cards = qualityCards.map(studyCard => new QualityCardNode(this.node, studyCard.id, studyCard.name, this.canAdmin));
                } else this.node.qualityCardsNode.cards = [];
                this.qualityCardsLoading = false;
                this.node.qualityCardsNode.open();
            }).catch(() => this.qualityCardsLoading = false);
        }
    }

    onStudyCardDelete(index: number) {
        (this.node.studyCardsNode.cards as StudyCardNode[]).splice(index, 1) ;
    }

    onQualityCardDelete(index: number) {
        (this.node.qualityCardsNode.cards as StudyCardNode[]).splice(index, 1) ;
    }
}
