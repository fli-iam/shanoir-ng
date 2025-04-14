import {Component} from '@angular/core';
import {ModesAware} from "../../preclinical/shared/mode/mode.decorator";
import {EntityComponent} from "../../shared/components/entity/entity.component.abstract";
import {ActivatedRoute} from "@angular/router";
import {FormGroup, UntypedFormGroup, Validators} from "@angular/forms";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import {Option} from "../../shared/select/select.component";
import {ExecutionTemplateParameter} from "../shared/execution-template-parameter.model";
import {ExecutionTemplateParameterService} from "./execution-template-parameter.service";


@Component({
    selector: 'execution-template-parameter',
    templateUrl: './execution-template-parameter.component.html',
    styleUrls: ['./execution-template-parameter.component.css'],
    standalone: false
})
@ModesAware
export class ExecutionTemplateParameterComponent extends EntityComponent<ExecutionTemplateParameter> {

    niftiConverters: Option<number>[] = [
        new Option<number>(1, 'DCM2NII_2008_03_31', null, null, null, false),
        new Option<number>(2, 'MCVERTER_2_0_7', null, null, null, false),
        new Option<number>(4, 'DCM2NII_2014_08_04', null, null, null, false),
        new Option<number>(5, 'MCVERTER_2_1_0', null, null, null, false),
        new Option<number>(6, 'DCM2NIIX', null, null, null, false),
        new Option<number>(7, 'DICOMIFIER', null, null, null, false),
        new Option<number>(8, 'MRICONVERTER', null, null, null, false),
    ];

    executionTemplateId: number
    groupBy: string
    executionForm: UntypedFormGroup

    constructor(
        private route: ActivatedRoute,
        private executionTemplateParameterService: ExecutionTemplateParameterService) {
        super(route, 'execution-template-parameter');
        if ( this.breadcrumbsService.currentStep ) {
            this.executionTemplateId = this.breadcrumbsService.currentStep.getPrefilledValue("executionTemplateId")
        }
    }

    get executionTemplateParameter(): ExecutionTemplateParameter { return this.entity; }
    set executionTemplateParameter(et: ExecutionTemplateParameter) { this.entity= et; }

    buildForm(): FormGroup {
        this.executionForm = this.formBuilder.group({
            'groupBy': [this.executionTemplateParameter.groupBy, [Validators.required, Validators.minLength(2), this.registerOnSubmitValidator('unique', 'name')]],
            'filter': [this.executionTemplateParameter.filter],
            'executionTemplateId': [this.executionTemplateParameter.executionTemplateId, [Validators.required]],
        });
        return this.executionForm;
    }

    getService(): EntityService<ExecutionTemplateParameter> {
        return this.executionTemplateParameterService;
    }

    initCreate(): Promise<void> {
        this.entity = new ExecutionTemplateParameter();
        this.entity.executionTemplateId = this.executionTemplateId;
        return Promise.resolve()
    }

    initEdit(): Promise<void> {return Promise.resolve();}

    initView(): Promise<void> {return Promise.resolve();}
}
