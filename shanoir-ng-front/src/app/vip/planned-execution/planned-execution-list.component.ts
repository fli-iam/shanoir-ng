import {Component, Input, ViewChild} from '@angular/core';
import {PlannedExecution} from "../models/planned-execution";
import {TableComponent} from "../../shared/components/table/table.component";
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import { PlannedExecutionService } from "./planned-execution.service";
import {BrowserPaginEntityListComponent} from "../../shared/components/entity/entity-list.browser.component.abstract";
import {StudyUserRight} from "../../studies/shared/study-user-right.enum";
import {StudyRightsService} from "../../studies/shared/study-rights.service";

@Component({
    selector: 'planned-execution-list',
    templateUrl: './planned-execution-list.component.html',
    styleUrls: ['./planned-execution-list.component.css']
})
export class PlannedExecutionListComponent extends BrowserPaginEntityListComponent<PlannedExecution> {

    @Input() studyId: number

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(protected plannedExecutionService: PlannedExecutionService,
                protected studyRightsService: StudyRightsService) {
        super('plannedExecution')
    }

    getService(): EntityService<PlannedExecution> {
        return this.plannedExecutionService
    }

    getEntities(): Promise<PlannedExecution[]> {
        return this.plannedExecutionService.getPlannedExecutionsByStudy(this.studyId)
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
