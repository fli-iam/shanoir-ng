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


@Component({
    selector: 'async-tasks',
    templateUrl: 'async-tasks.component.html',
    styleUrls: ['async-tasks.component.css']
})

export class AsyncTasksComponent extends BrowserPaginEntityListComponent<Task> {
    
    @ViewChild('table') table: TableComponent;

    constructor() {       
        super('task', {'new': false, 'edit': false, 'view': false, 'delete': false});
    }

    getEntities(): Promise<Task[]> {
        let tasks: Task[] = [
            AsyncTasksComponent.buildTask('Dataset import for subject 54', new Date(), new Date(), 1),
            AsyncTasksComponent.buildTask('Dataset import for subject 12', new Date(), new Date(), 1),
            AsyncTasksComponent.buildTask('Dataset import for subject 24', new Date(), null, 0.4),
            AsyncTasksComponent.buildTask('Dataset import for subject 64', new Date(), null, 0.5),
            AsyncTasksComponent.buildTask('Dataset import for subject 34', new Date(), new Date(), 1)
        ];
        return Promise.resolve(tasks);
    }

    // Grid columns definition
    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        return [
            { headerName: 'Label', field: 'label', defaultSortCol: true, defaultAsc: true },
            { headerName: 'Progress', field: 'progress', type: 'progress'},
            {
                headerName: "Start", field: "startDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End", field: "endDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.endDate);
                }
            },
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    private static buildTask(label: string, startDate: Date, endDate: Date, progress: number): Task {
        let task = new Task(); 
        task.label = label;
        task.startDate = startDate;
        task.endDate = endDate;
        task.progress = progress;
        return task;
    }

}