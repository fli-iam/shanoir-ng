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
import { Component, Input, OnChanges, OnDestroy, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { Subscription } from 'rxjs';
import { HttpClient } from "@angular/common/http";
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { BrowserPaging } from '@app/shared/components/table/browser-paging.model';
import { ColumnDefinition } from '@app/shared/components/table/column.definition.type';
import { FilterablePageable, Page } from '@app/shared/components/table/pageable.model';
import { MassDownloadService } from '@app/shared/mass-download/mass-download.service';
import { QualityCardComponent } from '@app/study-cards/quality-card/quality-card.component';

import { NotificationsService } from '../../shared/notifications/notifications.service';
import {Task} from '../task.model';
import {TaskService} from "../task.service";
import {KeycloakService} from "../../shared/keycloak/keycloak.service";
import {ConsoleService} from "../../shared/console/console.service";
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { TableComponent } from '../../shared/components/table/table.component';
import { LocalDateFormatPipe } from '../../shared/localLanguage/localDateFormat.pipe';

@Component({
    selector: 'task-status',
    templateUrl: 'task-status.component.html',
    styleUrls: ['task-status.component.css'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormsModule, LoadingBarComponent, RouterLink, TableComponent, LocalDateFormatPipe]
})
export class TaskStatusComponent implements OnDestroy, OnChanges {

    importTs: number;
    protected subscriptions: Subscription[] = [];
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
    browserCompatible: boolean = !!(window as any).showDirectoryPicker;
    loading: boolean = false;

    constructor(
        private notificationsService: NotificationsService,
        private taskService: TaskService,
        private downloadService: MassDownloadService,
        private http: HttpClient,
        private keycloakService: KeycloakService,
        private consoleService: ConsoleService
    ) {}

    private reportFetchInFlight: boolean = false;

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.task && this.task) {
            this.updateReport();
            this.fetchReportIfNeeded();

            this.subscriptions.push(
                this.notificationsService.getNotifications().subscribe(tasks => {
                    // Live SSE updates only ever carry a "light" task (hasReport flag, never the
                    // actual report content - cf. ShanoirEvent.toLightEvent()). 
                    // Merge in place via updateWith() rather than replacing the reference : it
                    // only overwrites report when the incoming value is truthy, so it can never
                    // erase a report already fetched below via taskService.get().
                    const liveTask = tasks.find(task => task.id == this.task.id);
                    if (liveTask) this.task.updateWith(liveTask);
                    this.updateReport();
                    this.fetchReportIfNeeded();
                })
            );
        }
    }

    /**
     * The task's report becomes available only once the job is done, but the live SSE stream
     * never carries its actual content (only the hasReport flag) - so as soon as that flag turns
     * true, fetch the full task once to get the real report, instead of waiting for a page reload.
     */
    private fetchReportIfNeeded(): void {
        if (this.task?.hasReport && !this.task.report && !this.reportFetchInFlight) {
            this.reportFetchInFlight = true;
            this.taskService.get(this.task.completeId)
                .then(fullTask => {
                    this.task.updateWith(fullTask);
                    this.updateReport();
                })
                .finally(() => this.reportFetchInFlight = false);
        }
    }

    private updateReport(): void {
        this.report = null;
        if (this.task) {
            let reportArray: [];
            try {
                reportArray = JSON.parse(this.task.report);
            } catch {
                reportArray = null;
            }
            if (reportArray && Array.isArray(reportArray)) {
                this.report = new BrowserPaging(reportArray, this.reportColumns);
                if (this.tableRefresh) this.tableRefresh();
            }
        }
    }

    ngOnDestroy() {
        for (const subscription of this.subscriptions) {
            subscription.unsubscribe();
        }
    }

    getPage(pageable: FilterablePageable): Promise<Page<any>> {
        return Promise.resolve(this.report?.getPage(pageable));
    }

    downloadReport() {
        QualityCardComponent.downloadReport(this.report);
    }

    registerTableRefresh(refresh: () => void) {
        this.tableRefresh = refresh;
    }

    retry() {
        this.loading = true;
        this.downloadService.retry(this.task).finally(() => this.loading = false);
    }

    downloadStats(event: MouseEvent) {
        event.preventDefault();
        this.taskService.downloadStats(this.task);
    }

    downloadProcessingOutputs(event: MouseEvent) {
        event.preventDefault();
        this.taskService.downloadProcessingOutputs(this.task);
    }
}
