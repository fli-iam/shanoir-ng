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
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { EntityListComponent } from 'src/app/shared/components/entity/entity-list.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { BrowserPaging } from 'src/app/shared/components/table/browser-paging.model';
import { Page, Pageable } from 'src/app/shared/components/table/pageable.model';
import { TableComponent } from 'src/app/shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { ExecutionMonitoring } from '../models/execution-monitoring.model';
import { ExecutionMonitoringService } from './execution-monitoring.service';

@Component({
    selector: 'app-execution-monitorings',
    templateUrl: './execution-monitorings.component.html',
    styleUrls: ['./execution-monitorings.component.css'],
    standalone: false
})
export class ExecutionMonitoringsComponent extends EntityListComponent<ExecutionMonitoring> implements AfterViewInit {
    
    @ViewChild('table', { static: false }) table: TableComponent;
    private executionMonitorings: ExecutionMonitoring[] = [];

    constructor(protected breadcrumbsService: BreadcrumbsService, private executionMonitoringService: ExecutionMonitoringService) {
        super('execution-monitoring');
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('VIP dataset processings');
    }

    ngAfterViewInit(): void {
        this.subscriptions.push(
            this.executionMonitoringService.getAllExecutionMonitorings().subscribe(executionMonitorings => {
                if (executionMonitorings == null) {
                    this.executionMonitorings = [];
                } else {
                    this.executionMonitorings = executionMonitorings;
                }
                this.table.refresh();
            })
        );
    }

    getService(): EntityService<ExecutionMonitoring> {
        return this.executionMonitoringService;
    }

    getOptions() {
        return { 'new': false, 'edit': false, 'view': false, 'delete': false, 'reload': true, id: false };
    }

    getPage(pageable: Pageable): Promise<Page<ExecutionMonitoring>> {
        return Promise.resolve(new BrowserPaging(this.executionMonitorings, this.columnDefs).getPage(pageable));
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            { headerName: "ID", field: "id", width: '130px', defaultSortCol: true, defaultAsc: false },
            {
                headerName: 'Name', field: 'name', width: '100%', type: 'link',
                route: (executionMonitoring: ExecutionMonitoring) => {
                    // return the link of the execution monitoring + id
                    return `/dataset-processing/details/${executionMonitoring.id}`;
                }
            },
            { headerName: 'Status', field: 'status', width: '70px' },
            { headerName: "Creation", field: "startDate", width: '130px' },
            {
                headerName: "Workflow ID", field: "comment", width: '130px'
            },
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}
