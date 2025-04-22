import {Component} from '@angular/core';
import {ExecutionTemplate} from "../models/execution-template";
import {ModesAware} from "../../preclinical/shared/mode/mode.decorator";
import {EntityComponent} from "../../shared/components/entity/entity.component.abstract";
import {ActivatedRoute} from "@angular/router";
import {ExecutionTemplateService} from "./execution-template.service";
import {FormGroup, UntypedFormControl, UntypedFormGroup, ValidatorFn, Validators} from "@angular/forms";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import {PipelineService} from "../pipelines/pipeline/pipeline.service";
import {Pipeline} from "../models/pipeline";
import {ParameterType} from "../models/parameterType";
import {Option} from "../../shared/select/select.component";

@Component({
    selector: 'execution-template',
    templateUrl: './execution-template.component.html',
    standalone: false
})
@ModesAware
export class ExecutionTemplateComponent extends EntityComponent<ExecutionTemplate> {


    id: number;
    studyId: number;
    studyName: string;
    pipelines: Pipeline[]
    pipelineNames: string[]
    priority: number;
    executionForm: UntypedFormGroup

    constructor(
        private route: ActivatedRoute,
        private pipelineService: PipelineService,
        protected templateService: ExecutionTemplateService) {
        super(route, 'execution-template');
        if ( this.breadcrumbsService.currentStep ) {
            this.studyId = this.breadcrumbsService.currentStep.getPrefilledValue("studyId")
            this.studyName = this.breadcrumbsService.currentStep.getPrefilledValue("studyName")
        }
    }

    get executionTemplate(): ExecutionTemplate { return this.entity; }
    set executionTemplate(et: ExecutionTemplate) { this.entity= et; }

    buildForm(): FormGroup {
        this.executionForm = this.formBuilder.group({
            'name': [this.executionTemplate.name, [Validators.required, Validators.minLength(2), this.registerOnSubmitValidator('unique', 'name')]],
            'studyId': [this.executionTemplate.studyId, [Validators.required]],
            'vipPipeline': [this.executionTemplate.vipPipeline, [Validators.required]],
            'priority': [this.executionTemplate.priority, [Validators.required, this.registerOnSubmitValidator('unique', 'priority')]]
        });
        return this.executionForm;
    }

    getService(): EntityService<ExecutionTemplate> {
        return this.templateService;
    }

    initCreate(): Promise<void> {
        this.entity = new ExecutionTemplate();
        this.entity.studyId = this.studyId;
        this.entity.studyName = this.studyName;
        this.pipelineService.listPipelines().subscribe(
            (pipelines :Pipeline[])=>{
                this.pipelines = pipelines;
                this.pipelineNames = pipelines.map(pipeline => pipeline.identifier)
            }
        )
        return Promise.resolve()
    }

    initEdit(): Promise<void> {
        this.templateService.setStudy(this)
        this.pipelineService.listPipelines().subscribe(
            (pipelines :Pipeline[])=>{
                this.pipelines = pipelines;
                this.pipelineNames = pipelines.map(pipeline => pipeline.identifier)
            }
        )
        return Promise.resolve();
    }

    initView(): Promise<void> {
        if(this.studyName){
            this.entity.studyName = this.studyName;
        } else {
            this.templateService.setStudy(this);
        }
        return Promise.resolve();
    }

    save(): Promise<ExecutionTemplate> {
        super.save().then(() => this.router.navigate(['study/details/' + this.studyId], {fragment: 'executions'}));
        return Promise.resolve(this.entity);
    }

    goBack(): void{
        this.router.navigate(['study/details/' + this.studyId], {fragment: 'executions'})
    }
}
