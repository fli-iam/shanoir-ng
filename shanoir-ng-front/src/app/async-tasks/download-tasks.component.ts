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
import { DatasetService } from '../datasets/shared/dataset.service';
import { MrDataset } from '../datasets/dataset/mr/dataset.mr.model';


@Component({
    selector: 'download-tasks',
    templateUrl: 'download-tasks.component.html',
    styleUrls: ['download-tasks.component.css']
})

export class DownloadTasksComponent extends BrowserPaginEntityListComponent<Task> {
    
    @ViewChild('table') table: TableComponent;
    
    private tasks: Task[];
    constructor(
        private taskService: TaskService,
        private datasetService: DatasetService) {       
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
        return this.taskService.getTaskOfTypes(["downloadDatasets.event"]);
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
            { headerName: 'Dataset name', field: 'message'},
            {
                headerName: "Creation", field: "creationDate", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.creationDate);
                }
            },
            { headerName: 'Dataset ID', field: 'objectId'},
            {
                headerName: "download", type: "button", awesome: "fa-download", action: item => this.download(item.objectId, item.message)
            },
        ];
    }
    
    download(id: number, message: string) {
        let ds = new MrDataset();
        ds.id = id;
        this.datasetService.download(ds, message.substr(message.length - 3));
    }

    getCustomActionsDefs(): any[] {
        return [{title: "Refresh",awesome: "fa-sync", action: item => {
            this.reloadData();
            this.table.refresh();
            }
        }];
    }

}