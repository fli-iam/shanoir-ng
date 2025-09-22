import {ChangeDetectorRef, Component} from '@angular/core'
import {ExecutionTemplate} from "../models/execution-template"
import {EntityComponent} from "../../shared/components/entity/entity.component.abstract"
import {ActivatedRoute} from "@angular/router"
import {ExecutionTemplateService} from "./execution-template.service"
import {
    FormGroup,
    UntypedFormGroup,
    Validators
} from "@angular/forms"
import {EntityService} from "../../shared/components/entity/entity.abstract.service"
import {Pipeline} from "../models/pipeline"
import {Option} from "../../shared/select/select.component"
import {DatasetProcessingType} from "../../enum/dataset-processing-type.enum"

@Component({
    selector: 'execution-template',
    templateUrl: './execution-template.component.html',
    styleUrls: ['./execution-template.component.css'],
    standalone: false
})

export class ExecutionTemplateComponent extends EntityComponent<ExecutionTemplate> {

    studyName: string
    studyId: number
    pipeline: Pipeline
    pipelines: Pipeline[]
    pipelineNames: string[]
    priority: number
    executionForm: UntypedFormGroup
    existingPriorities: number[]
    pipelineParameters: { [key: string]: string }
    processingType = Object.values(DatasetProcessingType)

    niftiConverters: Option<number>[] = [
        new Option<number>(1, 'DCM2NII_2008_03_31'),
        new Option<number>(2, 'MCVERTER_2_0_7'),
        new Option<number>(4, 'DCM2NII_2014_08_04'),
        new Option<number>(5, 'MCVERTER_2_1_0'),
        new Option<number>(6, 'DCM2NIIX'),
        new Option<number>(7, 'DICOMIFIER'),
        new Option<number>(8, 'MRICONVERTER'),
    ]

    constructor(
        private route: ActivatedRoute,
        protected templateService: ExecutionTemplateService,
        public cdr: ChangeDetectorRef) {
        super(route, 'execution-template')
        if (this.breadcrumbsService.currentStep && this.mode == "create") {
            Promise.all([
                this.breadcrumbsService.currentStep.getPrefilledValue("studyId"),
                this.breadcrumbsService.currentStep.getPrefilledValue("studyName")
            ]).then(([studyId, studyName]) => {
                this.entity.studyId = studyId;
                this.studyId = studyId;
                this.studyName = studyName;

                this.templateService.getExistingPriorities(this);
                this.templateService.getPipelinesFromVIP(this);
                this.templateService.initParameters(this);
            });
        }
    }

    get executionTemplate(): ExecutionTemplate { return this.entity }
    set executionTemplate(et: ExecutionTemplate) { this.entity= et }

    buildForm(): FormGroup {
        if(this.pipelineParameters === undefined) {this.pipelineParameters = {}}
        this.executionForm = this.formBuilder.group({
            'name': [this.entity.name, [Validators.required, Validators.minLength(2), Validators.maxLength(100), this.registerOnSubmitValidator('unique', 'name')]],
            'pipelineName': [this.entity.pipelineName, [Validators.required]],
            'filterCombination': [this.entity.filterCombination, [this.templateService.filterCombinationControl()]],
            'priority': [this.entity.priority, [Validators.required, this.templateService.uniqueInStudyControl(this)]],
            'processing_type': [this.pipelineParameters['processing_type'], [Validators.required]],
            'export_format': [this.pipelineParameters['export_format'], [Validators.required]],
            'execution_name': [this.pipelineParameters['execution_name'], [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
            'converter': [this.pipelineParameters['converter'], [this.templateService.requiredIfTypeIsNii()]]
        })

        this.executionForm.get('export_format')?.valueChanges.subscribe(() => {
            this.executionForm.get('converter')?.updateValueAndValidity()
        })
        this.executionForm.get('export_format')?.valueChanges.subscribe(() => {
            this.executionForm.get('converter')?.updateValueAndValidity()
        })
        return this.executionForm
    }

    getService(): EntityService<ExecutionTemplate> {
        return this.templateService
    }

    initCreate(): Promise<void> {
        this.entity = new ExecutionTemplate()
        return Promise.resolve()
    }

    initEdit(): Promise<void> {
        this.studyId = this.entity.studyId
        this.templateService.getExistingPriorities(this);
        this.templateService.getPipelinesFromVIP(this);
        this.templateService.onPipelineLoading(this, this.formBuilder)
        this.templateService.getStudyName(this)
        this.templateService.getParameters(this)
        this.cdr.detectChanges()

        return Promise.resolve()
    }

    initView(): Promise<void> {
        this.templateService.onPipelineLoading(this, this.formBuilder)
        this.templateService.getStudyName(this)
        this.templateService.getParameters(this)
        this.cdr.detectChanges()
        return Promise.resolve()
    }

    save(): Promise<ExecutionTemplate> {
        this.templateService.checkOneDatasetGroup(this)
        this.templateService.cleanParameters(this)
        this.templateService.shapeParameterEntities(this)
        this.templateService.updateEntityOnSave(this)
        super.save()
        return Promise.resolve(this.entity)
    }


    goBack(){
        this.router.navigate(['study/details/' + this.entity.studyId], {fragment: 'executions'})
    }

    onParameterUpdate(parameterName: string) {
        this.pipelineParameters[parameterName.replace(/_value$/, '')] = this.form.get(parameterName)?.value
        if(parameterName === 'export_format' && this.form.get('export_format')?.value !== 'nii') {
            this.form.get('converter')?.setValue(null)
            this.pipelineParameters['converter'] = null
        }
    }

    onPipelineUpdate() {
        this.templateService.onPipelineUpdate(this, this.formBuilder)
    }

    getConverterLabel(converterNumber: string): string {
        const match = this.niftiConverters.find(opt => opt.value === Number(converterNumber))
        return match ? match.label : converterNumber
    }
}
