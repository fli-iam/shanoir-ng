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

import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Coil } from '../coils/shared/coil.model';
import { CoilService } from '../coils/shared/coil.service';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../shared/components/table/browser-paging.model';
import { ColumnDefinition } from '../shared/components/table/column.definition.type';
import { FilterablePageable, Page, Pageable } from '../shared/components/table/pageable.model';
import { StudyCard } from '../study-cards/shared/study-card.model';
import { StudyCardService } from '../study-cards/shared/study-card.service';

@Component({
    selector: 'quality-control',
    templateUrl: 'quality-control.component.html',
    styleUrls: ['quality-control.component.css'] 
})

export class QualityControlComponent implements OnChanges {
    
    @Input() studyId: number;
    studyCards: StudyCard[] = [];
    allCoils: Coil[];
    selectedStudyCard: StudyCard;
    timeoutId: number;
    overTimeout: any;
    pagings: Map<number, BrowserPaging<any>> = new Map();
    getPage: Map<number, (FilterablePageable) => Promise<Page<any>>> = new Map();
    loading: Map<number, boolean> = new Map();
    columnDefs: ColumnDefinition[] = [
        {headerName: 'Subject Name', field: 'subjectName'},
        {headerName: 'Examination Comment', field: 'examinationComment'}
    ];
    
    constructor(
            private studyCardService: StudyCardService,
            coilService: CoilService,
            private confirmService: ConfirmDialogService
    ) {
        coilService.getAll().then(coils => this.allCoils = coils);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.studyId && this.studyId) {
            this.studyCardService.getAllForStudy(this.studyId).then(studyCards => this.studyCards = studyCards?.filter(sc => sc.acquisitionEquipment == null));
        }
    }

    apply(studyCard: StudyCard) {
        this.confirmService.confirm(
            'Apply Quality Card', 
            `Do you want to apply the quality card named "${studyCard.name}" all over the study "${studyCard.study.name}" ? This would permanentely overwrite previous Shanoir metadata that has been previously updated by any study/quality cards for theses datasets.`
        ).then(accept => {
            if (accept) {
                this.loading.set(studyCard.id, true);
                this.studyCardService.applyStudyCardOnStudy(studyCard.id).then(result => {
                    this.pagings.set(studyCard.id, new BrowserPaging(result, this.columnDefs));
                    this.getPage.set(studyCard.id, (pageable: FilterablePageable) => {
                        return Promise.resolve(this.pagings.get(studyCard.id).getPage(pageable));
                    });
                }).finally(() => this.loading.set(studyCard.id, false));
            }
        });

    }

    onMouseOverNbRules(studyCard: StudyCard, event: any) {
        if (studyCard.id != this.timeoutId) {
            this.timeoutId = studyCard.id;
            clearTimeout(this.overTimeout);
            this.overTimeout = setTimeout(() => {
                this.selectedStudyCard = studyCard;
            }, 500);
        }
    }

    onMouseOutNbRules(studyCard: StudyCard) {
        if (studyCard.id == this.timeoutId) {
            this.timeoutId = null;
            clearTimeout(this.overTimeout);
            this.selectedStudyCard = null;
        }
    }
}