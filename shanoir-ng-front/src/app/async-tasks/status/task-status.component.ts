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
import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationsService } from '../../shared/notifications/notifications.service';
import { Task } from '../task.model';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import { FilterablePageable, Page } from 'src/app/shared/components/table/pageable.model';
import { BrowserPaging } from 'src/app/shared/components/table/browser-paging.model';
import { browserDownloadFile } from 'src/app/utils/app.utils';
import { TableComponent } from 'src/app/shared/components/table/table.component';
import { QualityCardComponent } from 'src/app/study-cards/quality-card/quality-card.component';


@Component({
    selector: 'task-status',
    templateUrl: 'task-status.component.html',
    styleUrls: ['task-status.component.css'],
})
export class TaskStatusComponent implements OnDestroy, OnChanges {

    importTs: number;
    protected subscribtions: Subscription[] = [];
    @Input() task: Task;
    private tableRefresh: () => void;

    reportColumns: ColumnDefinition[] = [
        {headerName: 'Subject Name', field: 'subjectName', width: '20%'},
        {headerName: 'Examination Comment', field: 'examinationComment', width: '25%'},
        {headerName: 'Examination Date', field: 'examinationDate', type: 'date', width: '100px'},
        {headerName: 'Details', field: 'message', wrap: true}
    ];
    report: BrowserPaging<any>;
    reportActions: any = [{title: "Download as csv", awesome: "fa-solid fa-download", action: () => this.downloadReport()}];


    ngOnChanges(changes: SimpleChanges): void {
        if (changes.task && this.task) {
            this.report = null;
            if (this.task) {
                let reportArray: [];
                try {
                    reportArray = JSON.parse(this.task.report);
                } catch (e) {}
                if (reportArray && Array.isArray(reportArray)) {
                    this.report = new BrowserPaging(reportArray, this.reportColumns);
                    if (this.tableRefresh) this.tableRefresh();
                }
            }
        } 
    }

    ngOnDestroy() {
        for (let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }

    getPage(pageable: FilterablePageable): Promise<Page<any>> {
        return Promise.resolve(this.report.getPage(pageable));
    }

    downloadReport() {
        QualityCardComponent.downloadReport(this.report);
    }

    registerTableRefresh(refresh: () => void) {
        this.tableRefresh = refresh;
    }
}