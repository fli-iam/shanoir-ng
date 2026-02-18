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
import { Component, Input, ViewChild } from '@angular/core';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';

import { TableComponent } from "../../../shared/components/table/table.component";
import { BrowserPaginEntityListComponent } from "../../../shared/components/entity/entity-list.browser.component.abstract";
import { ExecutionTemplateFilter } from "../../models/execution-template-filter";

import { ExecutionTemplateFilterService } from "./execution-template-filter.service";

@Component({
    selector: 'execution-template-filter-list',
    templateUrl: './execution-template-filter-list.component.html',
    standalone: false

})
export class ExecutionTemplateFilterListComponent extends BrowserPaginEntityListComponent<ExecutionTemplateFilter> {

    @Input() templateId: number
    @Input() templateName: string


    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(protected executionTemplateFilterService: ExecutionTemplateFilterService) {
        super('execution-template-filter')
    }

    ngOnInit() {
        super.ngOnInit()
        this.breadcrumbsService.currentStep.addPrefilled("templateId", this.templateId)
        this.breadcrumbsService.currentStep.addPrefilled("templateName", this.templateName)
    }

    getService(): EntityService<ExecutionTemplateFilter> {
        return this.executionTemplateFilterService
    }

    getEntities(): Promise<ExecutionTemplateFilter[]> {
        if(this.templateId != undefined){
            return this.executionTemplateFilterService.getExecutionTemplateFiltersByExecutionTemplate(this.templateId)
        }
        return Promise.resolve([]);
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            {headerName: "Field name", field: "fieldName", type: "string", width: "60px", defaultSortCol: true, defaultAsc: true},
            {headerName: "Excluded", field: "excluded", type: "string", width: "60px", defaultSortCol: false, defaultAsc: false},
            {headerName: "Regex to compare", field: "comparedRegex", type: "string", width: "60px", defaultSortCol: false, defaultAsc: false},
            {headerName: "Identifier", field: "identifier", type: "number", width: "60px", defaultSortCol: false, defaultAsc: false},
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: true,
            view: true,
            edit: true,
            delete: true,
            id: false
        };
    }

    protected completeColDefs(): void {
        this.columnDefs.push({ headerName: "Edit", type: "button", width: "10px", awesome: "fa-regular fa-edit", action: item => this.goToEdit(item.id), condition: item => this.canEdit(item) });
        this.columnDefs.push({ headerName: "Delete", type: "button", width: "10px", awesome: "fa-regular fa-trash-can", action: (item) => this.openDeleteConfirmDialog(item) , condition: item => this.canDelete(item)});
    }
}
