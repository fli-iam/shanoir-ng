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

import { Coil } from '../coils/shared/coil.model';
import { CoilService } from '../coils/shared/coil.service';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../shared/components/table/browser-paging.model';
import { ColumnDefinition } from '../shared/components/table/column.definition.type';
import { FilterablePageable, Page } from '../shared/components/table/pageable.model';
import { TableComponent } from '../shared/components/table/table.component';
import { QualityCard } from '../study-cards/shared/quality-card.model';
import { QualityCardService } from '../study-cards/shared/quality-card.service';
import * as AppUtils from '../utils/app.utils';

import { RouterLinkActive, RouterLink } from '@angular/router';
import { StudyCardRulesComponent } from '../study-cards/study-card-rules/study-card-rules.component';
import { FormsModule } from '@angular/forms';
import { CheckboxComponent } from '../shared/checkbox/checkbox.component';


@Component({
    selector: 'quality-control',
    templateUrl: 'quality-control.component.html',
    styleUrls: ['quality-control.component.css'],
    imports: [RouterLinkActive, RouterLink, StudyCardRulesComponent, FormsModule, CheckboxComponent, TableComponent]
})

export class QualityControlComponent implements OnChanges {

    @Input() studyId: number;
    @Output() tagUpdate: EventEmitter<void> = new EventEmitter();
    qualityCards: QualityCard[] = [];
    allCoils: Coil[];
    selectedQualityCard: QualityCard;
    timeoutId: number;
    message: string;
    overTimeout: any;
    pagings: Map<number, BrowserPaging<any>> = new Map();
    getPage: Map<number, (FilterablePageable) => Promise<Page<any>>> = new Map();
    loading: Map<number, boolean> = new Map();
    columnDefs: ColumnDefinition[] = [
        {headerName: 'Subject Name', field: 'subjectName', width: 'auto'},
        {headerName: 'Examination Comment', field: 'examinationComment', width: 'auto'},
        {headerName: 'Examination Date', field: 'examinationDate', type: 'date', width: 'auto'},
        {headerName: 'Details', field: 'message', wrap: true, width: 'auto'}
    ];

    constructor(
            private qualityCardService: QualityCardService,
            coilService: CoilService,
            private confirmService: ConfirmDialogService
    ) {
        coilService.getAll().then(coils => this.allCoils = coils);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.studyId && this.studyId) {
            this.qualityCardService.getAllForStudy(this.studyId).then(qualityCards => {
                this.qualityCards = qualityCards;
            });
        }
    }

    apply(qualityCard: QualityCard) {
        this.confirmService.confirm(
            'Apply Quality Card',
            `Do you want to apply the quality card named "${qualityCard.name}" all over the study "${qualityCard.study.name}" ? This would permanentely overwrite previous quality tags for the study's subjects.`
        ).then(accept => {
            if (accept) {
                this.loading.set(qualityCard.id, true);
                this.qualityCardService.applyOnStudy(qualityCard.id).then(result => {
                    this.pagings.set(qualityCard.id, new BrowserPaging(result, this.columnDefs));
                    this.getPage.set(qualityCard.id, (pageable: FilterablePageable) => {
                        return Promise.resolve(this.pagings.get(qualityCard.id).getPage(pageable));
                    });
                    this.tagUpdate.emit();
                }).finally(() => this.loading.set(qualityCard.id, false));
            }
        });
    }

    onMouseOverNbRules(qualityCard: QualityCard) {
        if (qualityCard.id != this.timeoutId) {
            this.timeoutId = qualityCard.id;
            clearTimeout(this.overTimeout);
            this.overTimeout = setTimeout(() => {
                this.selectedQualityCard = qualityCard;
            }, 500);
        }
    }

    onMouseOutNbRules(qualityCard: QualityCard) {
        if (qualityCard.id == this.timeoutId) {
            this.timeoutId = null;
            clearTimeout(this.overTimeout);
            this.selectedQualityCard = null;
        }
    }

    downloadReport(qualityCard: QualityCard) {
        const browserPaging: BrowserPaging<any> = this.pagings.get(qualityCard?.id);
        let csvStr: string = '';
        csvStr += browserPaging.columnDefs.map(col => col.headerName).join(',');
        for (const entry of browserPaging.items) {
            csvStr += '\n' + browserPaging.columnDefs.map(col => '"' + TableComponent.getCellValue(entry, col) + '"').join(',');
        }
        const csvBlob = new Blob([csvStr], {
            type: 'text/csv'
        });
        AppUtils.browserDownloadFile(csvBlob, this.getReportFileName(qualityCard));
    }

    private getReportFileName(qualityCard: QualityCard): string {
        return 'qcReport_' + qualityCard.name + '_' + Date.now().toLocaleString('fr-FR');
    }

    updateQualityCard(qualityCard: QualityCard) {
        this.qualityCardService.update(qualityCard.id, qualityCard);
    }
}
