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
import { Component, ElementRef, Input, OnChanges, QueryList, SimpleChanges, ViewChildren } from '@angular/core';
import { Router, RouterLink } from '@angular/router';


import { Entity } from 'src/app/shared/components/entity/entity.abstract';
import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { QualityCardService } from 'src/app/study-cards/shared/quality-card.service';
import { isDarkColor } from 'src/app/utils/app.utils';
import { SuperPromise } from 'src/app/utils/super-promise';

import { StudyCardService } from '../../study-cards/shared/study-card.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import {
    MemberNode,
    QualityCardNode,
    ShanoirNode,
    StudyCardNode,
    StudyNode,
    SubjectNode,
    UNLOADED
} from '../../tree/tree.model';
import { StudyRightsService } from "../shared/study-rights.service";
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { TreeService } from '../study/tree.service';
import { NgIf, NgFor } from '@angular/common';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { DropdownMenuComponent } from '../../shared/components/dropdown-menu/dropdown-menu.component';
import { MenuItemComponent } from '../../shared/components/dropdown-menu/menu-item/menu-item.component';
import { FormsModule } from '@angular/forms';
import { SubjectNodeComponent } from '../../subjects/tree/subject-node.component';
import { CenterNodeComponent } from '../../centers/tree/center-node.component';
import { StudyCardNodeComponent } from '../../study-cards/tree/study-card-node.component';
import { MemberNodeComponent } from '../../users/tree/member-node.component';

export type Sort = {field: 'name' | 'id', way : 'asc' | 'desc'}

@Component({
    selector: 'study-node',
    templateUrl: 'study-node.component.html',
    styleUrls: ['study-node.component.css'],
    imports: [NgIf, TreeNodeComponent, DropdownMenuComponent, RouterLink, MenuItemComponent, FormsModule, NgFor, SubjectNodeComponent, CenterNodeComponent, StudyCardNodeComponent, MemberNodeComponent]
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
    @ViewChildren('fakeSubject') fakeSubjectNodes: QueryList<ElementRef>;
    @ViewChildren('fakeMember') fakeMemberNodes: QueryList<ElementRef>;

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
            const id: number = this.input instanceof StudyNode ? this.input.id : this.input.study.id;
            this.idPromise.resolve(id);
            if (this.input instanceof StudyNode) {
                this.node = this.input;
            } else if (this.input.study && this.input.rights) {
                this.node = this.treeService.buildStudyNode(this.input.study, this.input.rights);
            } else {
                throw new Error('Illegal argument type');
            }
            this.sortSubjects({field: 'name', way: 'asc'});
            this.nodeInit.emit(this.node);
            this.showDetails = this.router.url != this.detailsPath + this.node.id;
            setTimeout(() => {
                if (this.fakeSubjectNodes.length > 0) {
                    this.setFakeTops();
                }
                this.subscriptions.push(this.fakeSubjectNodes.changes.subscribe(() => this.setFakeTops()));
            });
        }
    }

    /**
     * Tells the top position of the fake nodes so the auto-scroll doesn't have to wait for the real node to be loaded
     */
    private setFakeTops() {
        this.fakeSubjectNodes.forEach(fake => {
            const id: number = fake.nativeElement.getAttribute('id');
            if (!!this.node.subjectsNode.subjects && this.node.subjectsNode.subjects != UNLOADED) {
                const node: SubjectNode = this.node.subjectsNode.subjects?.find(n => n.id == id);
                node.getTop = () => fake.nativeElement?.offsetTop;
            }
        });
        this.fakeMemberNodes.forEach(fake => {
            const id: number = fake.nativeElement.getAttribute('id');
            if (!!this.node.membersNode.members && this.node.membersNode.members != UNLOADED) {
                const node: MemberNode = this.node.membersNode.members?.find(n => n.id == id);
                if (node) node.getTop = () => fake.nativeElement?.offsetTop;
            }
        });
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
        if (this.node.subjectsNode.subjects != 'UNLOADED' && this.filter?.trim().length > 0) {
            this.filteredNodes = (this.node.subjectsNode.subjects as SubjectNode[]).filter(node => {
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

    trackByFn(index, item: Entity) {
        return item.id;
    }

    protected clickFakeNode(node: ShanoirNode) {
        node.fake = false;
    }

    protected clickFakeOpen(node: ShanoirNode) {
        node.opened = true;
        node.fake = false;
    }

    getFontColor(colorInp: string): boolean {
        return isDarkColor(colorInp);
    }
}
