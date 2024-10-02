import {Component, Input} from '@angular/core';
import {AutomaticExecution} from "../models/automatic-execution";



@Component({
    selector: 'execution-planning',
    templateUrl: './execution-planning.component.html',
    styleUrls: ['./execution-planning-component.css']
})
export class ExecutionPlanning {
    @Input() executionPlanning: AutomaticExecution
    onSubmitExecutionForm() {
        console.log("we send the form, but actually no");
    }
}
