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

    constructor(
            private taskService: TaskService,
            private notificationsService: NotificationsService) {
        super('task');
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

    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleString();
            }
            return null;
        };
        return [
            { headerName: 'Message', field: 'message', width: '100%', type:'link',
				route: (task: Task) => task.status === 1 && task.eventType === 'importDataset.event' ? 
				'/examination/details/' + task.message.slice(task.message.lastIndexOf('in examination ') + ('in examination '.length)) :
				'/home'},
            { headerName: 'Progress', field: 'progress', width: '110px', type: 'progress' },
            { headerName: 'Status', field: 'status', width: '70px', type: 'Status', cellRenderer: function (params: any) {
                    if (params.data.status == 2) {
                        return "In progress"
                    }
                    if (params.data.status == 1) {
                        return {text: 'Success', color: 'darkgreen'}
                    }
                    if (params.data.status == -1) {
                        return {text: 'Error', color: 'red'}
                    }
                } 
            },
            {
                headerName: "Creation", field: "creationDate", width: '130px', cellRenderer: function (params: any) {
                    return dateRenderer(params.data.creationDate);
                }
            },
            {
                headerName: "Last update", field: "lastUpdate", width: '130px', defaultSortCol: true, defaultAsc: false, cellRenderer: function (params: any) {
                    return dateRenderer(params.data.lastUpdate);
                }
            },
        ];
    }
    
    getCustomActionsDefs(): any[] {
        return [];
    }

}