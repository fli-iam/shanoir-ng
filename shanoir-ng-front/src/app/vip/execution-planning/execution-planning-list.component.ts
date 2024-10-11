import {Component, Input, model, OnInit} from '@angular/core';
import {ExecutionService} from "../execution/execution.service";
import {AutomaticExecution} from "../models/automatic-execution";
import {ModesAware} from "../../preclinical/shared/mode/mode.decorator";
import { Mode } from '../../shared/components/entity/entity.component.abstract';
import {ExecutionPlanning} from "./execution-planning.component";

@Component({
    selector: 'execution-planning-list',
    templateUrl: './execution-planning-list.component.html',
    styleUrls: ['./execution-planning-list.component.css']
})
@ModesAware
export class ExecutionPlanningList {

    @Input() studyId: number
    @Input() mode: Mode
    executions: AutomaticExecution[]

    constructor(protected executionService: ExecutionService) {}

    ngOnInit(): void {
        this.executionService.getAutomaticExecutions(this.studyId).then(
            executionsReturned => this.executions = executionsReturned
        )
    }

    addNewExec() {
        let exec = new AutomaticExecution();
        exec.name = "new one yay";
        this.executions.push(exec)
    }

    deleteExec(execution) {
        this.executions = this.executions.slice(this.executions.indexOf(execution));
    }

}
