import {Component, Input, ViewChild} from '@angular/core';
import {TableComponent} from "../../shared/components/table/table.component";
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import {BrowserPaginEntityListComponent} from "../../shared/components/entity/entity-list.browser.component.abstract";
import {ExecutionTemplateParameter} from "../shared/execution-template-parameter.model";
import {ExecutionTemplateParameterService} from "./execution-template-parameter.service";

@Component({
    selector: 'execution-template-parameter-list',
    templateUrl: './execution-template-parameter-list.component.html',
    styleUrls: ['./execution-template-parameter-list.component.css'],
    standalone: false

})
export class ExecutionTemplateParameterListComponent extends BrowserPaginEntityListComponent<ExecutionTemplateParameter> {

    @Input() executionTemplateId: number

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(protected executionTemplateParameterService: ExecutionTemplateParameterService) {
        super('execution-template-parameter')
    }

    ngOnInit() {
        super.ngOnInit()
        this.breadcrumbsService.currentStep.addPrefilled("executionTemplateId", this.executionTemplateId)
    }

    getService(): EntityService<ExecutionTemplateParameter> {
        return this.executionTemplateParameterService
    }

    getEntities(): Promise<ExecutionTemplateParameter[]> {
        return this.executionTemplateParameterService.getExecutionTemplateParametersByExecutionTemplate(this.executionTemplateId)
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
