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
    styleUrls: ['./execution-template-list.component.css'],
    standalone: false

})
export class ExecutionTemplateListComponent extends BrowserPaginEntityListComponent<ExecutionTemplate> {

    @Input() studyId: number

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(protected executionTemplateService: ExecutionTemplateService,
                protected studyRightsService: StudyRightsService) {
        super('execution-template')
    }

    ngOnInit() {
        super.ngOnInit()
        this.breadcrumbsService.currentStep.addPrefilled("studyId", this.studyId)
    }

    getService(): EntityService<ExecutionTemplate> {
        return this.executionTemplateService
    }

    getEntities(): Promise<ExecutionTemplate[]> {
        return this.executionTemplateService.getExecutionTemplatesByStudy(this.studyId)
    }

    getColumnDefs(): ColumnDefinition[] {
        return [
            {headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Name", field: "name", type: "string", width: "60px", defaultSortCol: false, defaultAsc: false},
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: this.hasAdminRightsOnStudy(),
            view: true,
            edit: this.hasAdminRightsOnStudy(),
            delete: this.hasAdminRightsOnStudy()
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
