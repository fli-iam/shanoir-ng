import {Component, Input} from '@angular/core';
import {AutomaticExecution} from "../models/automatic-execution";
import {ModesAware} from "../../preclinical/shared/mode/mode.decorator";
import {Mode} from "../../shared/components/entity/entity.component.abstract";


@Component({
    selector: 'execution-planning',
    templateUrl: './execution-planning.component.html',
    styleUrls: ['./execution-planning.component.css']
})
@ModesAware
export class ExecutionPlanning {
    @Input() executionPlanning: AutomaticExecution
    @Input() mode: Mode;
    // Define if the current execution planning is being created / edited or only viewed, 'view' by default
    @Input() state = 'view'

    saveExec() {
        console.log("we save the exec")
        this.state = 'view'
    }

    editExec() {
        this.state = 'edit'
    }
}
