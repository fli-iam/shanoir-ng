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

import { CardNode } from '../../tree/tree.model';
import { StudyCard } from '../shared/study-card.model';
import { StudyCardService } from "../shared/study-card.service";
import { QualityCard } from '../shared/quality-card.model';
import { QualityCardService } from '../shared/quality-card.service';
import { TreeService } from 'src/app/studies/study/tree.service';


@Component({
    selector: 'card-node',
    templateUrl: 'study-card-node.component.html',
    standalone: false
})

export class StudyCardNodeComponent implements OnChanges {

    @Input() input: CardNode | StudyCard | QualityCard;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() onCardDelete: EventEmitter<void> = new EventEmitter();
    node: CardNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    @Input() detailsPath: string;
    @Input() withMenu: boolean = true;

    constructor(
        private studycardService: StudyCardService,
        private qualitycardService: QualityCardService,
        protected treeService: TreeService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof CardNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }

    deleteStudyCard() {
        const service = this.node.type == 'studycard' ? this.studycardService : this.qualitycardService;

        service.get(this.node.id).then(entity => {
            service.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.onCardDelete.emit();
                }
            });
        })
    }
}
