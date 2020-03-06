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

import { Component, ViewChild } from '@angular/core';
import { BrowserPaginEntityListComponent } from '../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../shared/components/table/table.component';
import { Task } from './task.model';
import { TaskService } from './task.service';


@Component({
    selector: 'async-tasks',
    templateUrl: 'async-tasks.component.html',
    styleUrls: ['async-tasks.component.css']
})

export class AsyncTasksComponent extends BrowserPaginEntityListComponent<Task> {
    
    @ViewChild('table') table: TableComponent;
    
    private tasks: Task[];
    constructor(
        private taskService: TaskService) {       
        super('task');
    }
    
    ngOnInit() {
        super.ngOnInit();
        this.getEntities().then(entities => { this.tasks = entities});
    }

    getOptions() {
        return {'new': false, 'edit': false, 'view': false, 'delete': false, 'reload':true};
    }

    getEntities(): Promise<Task[]> {
        return this.taskService.getTasks();
    }

    // Grid columns definition
    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleString();
            }
            return null;
        };
        return [
            { headerName: 'Message', field: 'message'},
            { headerName: 'Status', field: 'status', type: 'Status', cellRenderer: function (params: any) {
                    if (params.data.status == 0) {
                        return "In progress"
                    }
                    if (params.data.status == 1) {
                        return "Success"
                    }
                    if (params.data.status == -1) {
                        return "Error"
                    }
                } 
            },
            { headerName: 'Progress', field: 'progress', type: 'progress', cellRenderer: function (params: any) {
                    return params.data.progress * 100 + '%';
                } 
            },
            {
                headerName: "Creation", field: "creationDate", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.creationDate);
                }
            },
            {
                headerName: "Last update", field: "lastUpdate", defaultSortCol: true, defaultAsc: false, cellRenderer: function (params: any) {
                    return dateRenderer(params.data.lastUpdate);
                }
            },
        ];
    }
    
    getCustomActionsDefs(): any[] {
        return [{title: "Refresh",awesome: "fa-sync", action: item => {
            this.reloadData();
            this.table.refresh();
            }
        }];
    }

}