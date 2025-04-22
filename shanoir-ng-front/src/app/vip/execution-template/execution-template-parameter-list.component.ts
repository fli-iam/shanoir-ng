import {Component, Input, ViewChild} from '@angular/core';
import {TableComponent} from "../../shared/components/table/table.component";
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import {BrowserPaginEntityListComponent} from "../../shared/components/entity/entity-list.browser.component.abstract";
import {ExecutionTemplateParameter} from "../models/execution-template-parameter.model";
import {ExecutionTemplateParameterService} from "./execution-template-parameter.service";

@Component({
    selector: 'execution-template-parameter-list',
    templateUrl: './execution-template-parameter-list.component.html',
    standalone: false

})
export class ExecutionTemplateParameterListComponent extends BrowserPaginEntityListComponent<ExecutionTemplateParameter> {

    @Input() templateId: number
    @Input() templateName: String


    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(protected executionTemplateParameterService: ExecutionTemplateParameterService) {
        super('execution-template-parameter')
    }

    ngOnInit() {
        super.ngOnInit()
        this.breadcrumbsService.currentStep.addPrefilled("executionTemplateId", this.templateId)
    }

    getService(): EntityService<ExecutionTemplateParameter> {
        return this.executionTemplateParameterService
    }

    getEntities(): Promise<ExecutionTemplateParameter[]> {
        return this.executionTemplateParameterService.getExecutionTemplateParametersByExecutionTemplate(this.templateId)
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            {headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Group by", field: "groupBy", type: "string", width: "60px", defaultSortCol: false, defaultAsc: false},
            {headerName: "Filter", field: "filter", type: "string", width: "60px", defaultSortCol: false, defaultAsc: false},
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
            delete: true
        };
    }

}
