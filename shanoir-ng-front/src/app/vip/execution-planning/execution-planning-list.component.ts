import {Component, Input, OnInit} from '@angular/core';
import {ExecutionService} from "../execution/execution.service";
import {AutomaticExecution} from "../models/automatic-execution";

@Component({
    selector: 'execution-planning-list',
    templateUrl: './execution-planning-list.component.html',
    styleUrls: ['./execution-planning-list.component.css']
})
export class ExecutionPlanningList {

    @Input() studyId: number
    executions: AutomaticExecution[]

    constructor(protected executionService: ExecutionService) {}

    ngOnInit(): void {
        this.executionService.getAutomaticExecutions(this.studyId).then(
            executionsReturned => this.executions = executionsReturned
        )
    }

}
