import {Component, Input, ViewChild} from '@angular/core';
import {TableComponent} from "../../../shared/components/table/table.component";
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import {BrowserPaginEntityListComponent} from "../../../shared/components/entity/entity-list.browser.component.abstract";
import {ExecutionTemplateFilter} from "../../models/execution-template-filter";
import {ExecutionTemplateFilterService} from "./execution-template-filter.service";

@Component({
    selector: 'execution-template-filter-list',
    templateUrl: './execution-template-filter-list.component.html',
    standalone: false

})
export class ExecutionTemplateFilterListComponent extends BrowserPaginEntityListComponent<ExecutionTemplateFilter> {

    @Input() templateId: number
    @Input() templateName: String


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

}
