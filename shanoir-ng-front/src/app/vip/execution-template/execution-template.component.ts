import {Component, Input} from '@angular/core';
import {ExecutionTemplate} from "../models/execution-template";
import {ModesAware} from "../../preclinical/shared/mode/mode.decorator";
import {EntityComponent, Mode} from "../../shared/components/entity/entity.component.abstract";
import {ActivatedRoute} from "@angular/router";
import {StudyRightsService} from "../../studies/shared/study-rights.service";
import {KeycloakService} from "../../shared/keycloak/keycloak.service";
import {ExecutionTemplateService} from "./execution-template.service";
import {FormGroup, UntypedFormControl, UntypedFormGroup, ValidatorFn, Validators} from "@angular/forms";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import {PipelineService} from "../pipelines/pipeline/pipeline.service";
import {Pipeline} from "../models/pipeline";
import {PipelineParameter} from "../models/pipelineParameter";
import {ParameterType} from "../models/parameterType";
import {Option} from "../../shared/select/select.component";


@Component({
    selector: 'execution-template',
    templateUrl: './execution-template.component.html',
    styleUrls: ['./execution-template.component.css'],
    standalone: false
})
@ModesAware
export class ExecutionTemplateComponent extends EntityComponent<ExecutionTemplate> {

    niftiConverters: Option<number>[] = [
        new Option<number>(1, 'DCM2NII_2008_03_31', null, null, null, false),
        new Option<number>(2, 'MCVERTER_2_0_7', null, null, null, false),
        new Option<number>(4, 'DCM2NII_2014_08_04', null, null, null, false),
        new Option<number>(5, 'MCVERTER_2_1_0', null, null, null, false),
        new Option<number>(6, 'DCM2NIIX', null, null, null, false),
        new Option<number>(7, 'DICOMIFIER', null, null, null, false),
        new Option<number>(8, 'MRICONVERTER', null, null, null, false),
    ];

    studyId: number;
    pipelines: Pipeline[]
    pipelineNames: string[]
    selectedPipeline: Pipeline
    executionForm: UntypedFormGroup

    constructor(
        private route: ActivatedRoute,
        private studyRightsService: StudyRightsService,
        keycloakService: KeycloakService,
        private pipelineService: PipelineService,
        protected executionTemplateService: ExecutionTemplateService) {
        super(route, 'execution-template');
        if ( this.breadcrumbsService.currentStep ) {
            this.studyId = this.breadcrumbsService.currentStep.getPrefilledValue("studyId")
        }
    }

    get executionTemplate(): ExecutionTemplate { return this.entity; }
    set executionTemplate(et: ExecutionTemplate) { this.entity= et; }

    buildForm(): FormGroup {
        this.executionForm = this.formBuilder.group({
            'name': [this.executionTemplate.name, [Validators.required, Validators.minLength(2), this.registerOnSubmitValidator('unique', 'name')]],
            'examinationNameFilter': [this.executionTemplate.examinationNameFilter],
            'studyId': [this.executionTemplate.studyId, [Validators.required]],
            'vipPipeline': [this.executionTemplate.vipPipeline, [Validators.required]]
        });
        return this.executionForm;
    }

    getService(): EntityService<ExecutionTemplate> {
        return this.executionTemplateService;
    }

    initCreate(): Promise<void> {
        this.entity = new ExecutionTemplate();
        this.entity.studyId = this.studyId;
        this.pipelineService.listPipelines().subscribe(
            (pipelines :Pipeline[])=>{
                this.pipelines = pipelines;
                this.pipelineNames = pipelines.map(pipeline => pipeline.identifier)
            }
        )
        return Promise.resolve()
    }

    initEdit(): Promise<void> {
        this.pipelineService.listPipelines().subscribe(
            (pipelines :Pipeline[])=>{
                this.pipelines = pipelines;
                this.pipelineNames = pipelines.map(pipeline => pipeline.identifier)
            }
        )
        return Promise.resolve();
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    isAFile(parameter: PipelineParameter): boolean {
        return parameter.type == ParameterType.File;
    }

    updatePipeline(selectedPipelineIdentifier): void {
        if (selectedPipelineIdentifier) {
            this.pipelineService.getPipeline(selectedPipelineIdentifier).subscribe(pipeline => {
                this.selectedPipeline = pipeline;
                this.selectedPipeline.parameters.forEach(
                    parameter => {
                        let validators: ValidatorFn[] = [];
                        if (!parameter.isOptional && parameter.type != ParameterType.Boolean && parameter.type != ParameterType.File) {
                            validators.push(Validators.required);
                        }
                        let control = new UntypedFormControl(parameter.defaultValue, validators);
                        if (parameter.name != "executable") {
                            this.executionForm.addControl(parameter.name, control);
                        }
                    }
                )
                let groupByControl = new UntypedFormControl("dataset", [Validators.required]);
                this.executionForm.addControl("groupBy", groupByControl);

                let exportControl = new UntypedFormControl("dcm", [Validators.required]);
                this.executionForm.addControl("exportFormat", exportControl);

                let niiConverterControl = new UntypedFormControl(6, []);
                this.executionForm.addControl("niftiConverter", niiConverterControl);
            });

        } else {
            this.selectedPipeline = null;
            this.executionForm = this.buildForm();
        }
    }

}
