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
import { Task } from './task.model';
import { TaskService } from './task.service';
import { EntityService } from '../shared/components/entity/entity.abstract.service';
import { NotificationsService } from '../shared/notifications/notifications.service';
import { EntityListComponent } from '../shared/components/entity/entity-list.component.abstract';
import { Pageable, Page } from '../shared/components/table/pageable.model';
import { BrowserPaging } from '../shared/components/table/browser-paging.model';


@Component({
    selector: 'async-tasks',
    templateUrl: 'async-tasks.component.html',
    styleUrls: ['async-tasks.component.css']
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
        this.subscribtions.push(
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

    getPage(pageable: Pageable): Promise<Page<Task>> {
        return Promise.resolve(new BrowserPaging(this.tasks, this.columnDefs).getPage(pageable));
    }


    // getPage(pageable: FilterablePageable, forceRefresh: boolean = false): Promise<Page<Task>> {
    //     return this.entitiesPromise.then(() => {
    //         if (forceRefresh) {
    //             return this.loadEntities().then(() => this.browserPaging.getPage(pageable));
    //         } else {
    //             return this.browserPaging.getPage(pageable);
    //         }
    //     });
    // }

    getColumnDefs(): ColumnDefinition[] {
        return [
            { 
               headerName: 'Message', field: 'message', width: '100%', type:'link', route: (task: Task) => task.route
            }, { 
               headerName: 'Progress', field: 'progress', width: '110px', type: 'progress' 
            }, { 
               headerName: "Creation", field: "creationDate", width: '130px', type: 'date'
            },{
                headerName: "Last update", field: "lastUpdate", width: '130px', defaultSortCol: true, defaultAsc: false, type: 'date'
            },
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

}
