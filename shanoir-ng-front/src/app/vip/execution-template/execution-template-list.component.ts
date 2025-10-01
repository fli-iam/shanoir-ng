import {Component, Input, ViewChild} from '@angular/core';
import {ExecutionTemplate} from "../models/execution-template";
import {TableComponent} from "../../shared/components/table/table.component";
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import { ExecutionTemplateService } from "./execution-template.service";
import {BrowserPaginEntityListComponent} from "../../shared/components/entity/entity-list.browser.component.abstract";
import {StudyUserRight} from "../../studies/shared/study-user-right.enum";
import {StudyRightsService} from "../../studies/shared/study-rights.service";

@Component({
    selector: 'execution-template-list',
    templateUrl: './execution-template-list.component.html',
    standalone: false

})
export class ExecutionTemplateListComponent extends BrowserPaginEntityListComponent<ExecutionTemplate> {

    @Input() studyId: number
    @Input() studyName: string

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(protected executionTemplateService: ExecutionTemplateService,
                protected studyRightsService: StudyRightsService) {
        super('execution-template')
    }

    ngOnInit() {
        super.ngOnInit()
        this.breadcrumbsService.currentStep.addPrefilled("studyId", this.studyId)
        this.breadcrumbsService.currentStep.addPrefilled("studyName", this.studyName)
    }

    getService(): EntityService<ExecutionTemplate> {
        return this.executionTemplateService
    }

    getEntities(): Promise<ExecutionTemplate[]> {
        if (this.studyId != undefined) {
            return this.executionTemplateService.getExecutionTemplatesByStudy(this.studyId)
        }
        return Promise.resolve([]);
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            {headerName: "Template name", field: "name", type: "string", width: "100px", defaultSortCol: false, defaultAsc: false},
            {headerName: "Pipeline name", field: "pipelineName", type: "string", width: "100px", defaultSortCol: false, defaultAsc: false},
            {headerName: "Priority", field: "priority", type: "number", width: "30px", defaultSortCol: true, defaultAsc: true},
        ];
    }

    protected completeColDefs(): void {
        this.columnDefs.push({ headerName: "Edit", type: "button", width: "10px", awesome: "fa-regular fa-edit", action: item => this.goToEdit(item.id), condition: item => this.canEdit(item) });
        this.columnDefs.push({ headerName: "Delete", type: "button", width: "10px", awesome: "fa-regular fa-trash-can", action: (item) => this.openDeleteConfirmDialog(item) , condition: item => this.canDelete(item)});
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: this.hasAdminRightsOnStudy(),
            view: true,
            edit: this.hasAdminRightsOnStudy(),
            delete: this.hasAdminRightsOnStudy(),
            id: false
        };
    }

    private hasAdminRightsOnStudy(): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) {
            return Promise.resolve(true);
        } else {
            return this.studyRightsService.getMyRightsForStudy(this.studyId).then(rights => {
                return rights.includes(StudyUserRight.CAN_ADMINISTRATE);
            });
        }
    }

}
