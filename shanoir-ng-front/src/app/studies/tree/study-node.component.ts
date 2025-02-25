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
import { Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { StudyCardService } from '../../study-cards/shared/study-card.service';

import { QualityCardService } from 'src/app/study-cards/shared/quality-card.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import {
    QualityCardNode,
    StudyCardNode,
    StudyNode,
    SubjectNode,
    UNLOADED
} from '../../tree/tree.model';
import { StudyRightsService } from "../shared/study-rights.service";
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { TreeService } from '../study/tree.service';
import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { Entity } from 'src/app/shared/components/entity/entity.abstract';

export type Sort = {field: 'name' | 'id', way : 'asc' | 'desc'}

@Component({
    selector: 'study-node',
    templateUrl: 'study-node.component.html',
    styleUrls: ['study-node.component.css'],
    standalone: false
})

export class StudyNodeComponent extends TreeNodeAbstractComponent<StudyNode> implements OnChanges {

    @Input() input: StudyNode | { study: Study, rights: StudyUserRight[] };
    subjectsMenuOpened: boolean = false;
    studyCardsLoading: boolean = false;
    qualityCardsLoading: boolean = false;
    showDetails: boolean;
    detailsPath: string = '/study/details/';
    idPromise: SuperPromise<number> = new SuperPromise();
    protected rights: StudyUserRight[];
    filter: string;
    filteredNodes: SubjectNode[];
    subjectsOrder: Sort;
    protected nbSubjectsInit: number = 0;
    private subjectsInited: SuperPromise<void>;

    constructor(
            private router: Router,
            private studyCardService: StudyCardService,
            private qualityCardService: QualityCardService,
            private keycloakService: KeycloakService,
            private studyRightsService: StudyRightsService,
            protected treeService: TreeService,
            elementRef: ElementRef) {

        super(elementRef);
        this.idPromise.then(id => {
            (this.keycloakService.isUserAdmin
                    ? Promise.resolve(StudyUserRight.all())
                    : this.studyRightsService.getMyRightsForStudy(id)
            ).then(rights => {
                this.rights = rights;
            });
        });
    }

    get canAdmin(): boolean {
        return this.rights.includes(StudyUserRight.CAN_ADMINISTRATE);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            let id: number = this.input instanceof StudyNode ? this.input.id : this.input.study.id;
            this.idPromise.resolve(id);
            if (this.input instanceof StudyNode) {
                this.subjectsInited = new SuperPromise();
                this.nbSubjectsInit = 0;
                this.node = this.input;
            } else if (this.input.study && this.input.rights) {
                this.subjectsInited = new SuperPromise();
                this.nbSubjectsInit = 0;
                this.node = this.treeService.buildStudyNode(this.input.study, this.input.rights);
            } else {
                throw new Error('Illegal argument type');
            }
            this.sortSubjects({field: 'name', way: 'asc'});
            this.node.subjectsNode.registerOpenPromise(this.subjectsInited);
            this.nodeInit.emit(this.node);
            this.showDetails = this.router.url != this.detailsPath + this.node.id;
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
        (this.node.studyCardsNode.cards as StudyCardNode[]).splice(index, 1);
    }

    onQualityCardDelete(index: number) {
        (this.node.qualityCardsNode.cards as StudyCardNode[]).splice(index, 1);
    }

    onFilterChange() {
        console.log('on filter change')
        if (this.node.subjectsNode.subjects != 'UNLOADED' && this.filter?.trim().length > 0) {
            this.filteredNodes = (this.node.subjectsNode.subjects as SubjectNode[]).slice(0, 100).filter(node => {
                return node.label.toLowerCase().includes(this.filter.toLowerCase());
            });
        } else {
            this.filteredNodes = null;
        }
    }

    resetFilter() {
        this.filter = null;
        this.filteredNodes = null;
    }

    sortSubjects(sort: Sort) {
        this.subjectsOrder = sort;
        if (!(this.node.subjectsNode.subjects == 'UNLOADED')) {
            if (this.subjectsOrder.field == 'name') {
                this.node.subjectsNode.subjects.sort((a, b) => a.label?.trim().localeCompare(b.label.trim()));
            } else if (this.subjectsOrder.field == 'id') {
                this.node.subjectsNode.subjects.sort((a, b) => b.id - a.id);
            }
            if (this.subjectsOrder.way == 'desc') {
                this.node.subjectsNode.subjects.reverse();
            }
            this.onFilterChange();
        }
    }

    onSubjectNodeInit() {
        this.nbSubjectsInit++;
        console.log('subject inited', this.nbSubjectsInit)
        if (this.nbSubjectsInit == this.node.subjectsNode?.subjects?.length) {
            console.log('!!! all subject inited')
            this.subjectsInited.resolve();
        }
    }

    onOpenedChange(state) {
        if (!state) {
            this.nbSubjectsInit = 0;
        }
    }

    trackByFn(index, item: Entity) {
        return item.id;
    }
}
