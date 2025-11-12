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

import { AfterViewInit, Component, ViewChild } from '@angular/core';

import { TableComponent } from '../shared/components/table/table.component';
import { ColumnDefinition } from '../shared/components/table/column.definition.type';
import { EntityService } from '../shared/components/entity/entity.abstract.service';
import { NotificationsService } from '../shared/notifications/notifications.service';
import { EntityListComponent } from '../shared/components/entity/entity-list.component.abstract';
import { Pageable, Page } from '../shared/components/table/pageable.model';
import { BrowserPaging } from '../shared/components/table/browser-paging.model';

import { TaskService } from './task.service';
import { Task } from './task.model';
import { NgIf } from '@angular/common';
import { TaskStatusComponent } from './status/task-status.component';


@Component({
    selector: 'async-tasks',
    templateUrl: 'async-tasks.component.html',
    styleUrls: ['async-tasks.component.css'],
    imports: [TableComponent, NgIf, TaskStatusComponent]
})

export class AsyncTasksComponent extends EntityListComponent<Task> implements AfterViewInit {

    @ViewChild('table', { static: false }) table: TableComponent;
    private tasks: Task[] = [];
    protected selected: Task;

    constructor(
            private taskService: TaskService,
            private notificationsService: NotificationsService) {
        super('task');
        notificationsService.nbNew = 0;
        notificationsService.nbNewError = 0;
    }

    ngAfterViewInit(): void {
        this.subscriptions.push(
            this.notificationsService.getNotifications().subscribe(tasks => {
                this.tasks = tasks;
                this.table.refresh();
            })
        );
    }

    getService(): EntityService<Task> {
        return this.taskService;
    }

    getOptions() {
        return {'new': false, 'edit': false, 'view': false, 'delete': false, 'reload': true, id: false};
    }

    getPage = (pageable: Pageable): Promise<Page<Task>> => {
        return Promise.resolve(new BrowserPaging(this.tasks, this.columnDefs).getPage(pageable));
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            {
               headerName: 'Message', field: 'message', width: '100%', type:'link', route: (task: Task) => task.route
            }, {
               headerName: 'Progress', field: 'progress', width: '110px', type: 'progress',
               cellRenderer: params => { return {progress: params.data?.progress, status: params.data?.status}; }
            }, {
               headerName: "Creation", field: "creationDate", width: '130px', type: 'dateTime', defaultSortCol: true, defaultAsc: false,
            }, {
                headerName: "Last update", field: "lastUpdate", width: '130px', type: 'dateTime'
            }
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    downloadStats(item: any) {
        if (item instanceof Task && item.eventType == "downloadStatistics.event" && item.progress == 1) {
            this.taskService.downloadStats(item);
        }
    }

    select(lightTask: Task) {
        this.notificationsService.nbNew = 0;
        this.notificationsService.nbNewError = 0;
        this.selected = null;
        if (!lightTask) return;
        if (lightTask.report || !lightTask.hasReport) {
            this.selected = lightTask;
        } else {
            this.taskService.get(lightTask.completeId).then(task => this.selected = task);
        }
    }
}
