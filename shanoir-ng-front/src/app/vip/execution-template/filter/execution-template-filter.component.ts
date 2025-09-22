import {Component} from '@angular/core'
import {EntityComponent} from "../../../shared/components/entity/entity.component.abstract"
import {ActivatedRoute} from "@angular/router"
import {FormGroup, UntypedFormGroup, Validators} from "@angular/forms"
import {EntityService} from "../../../shared/components/entity/entity.abstract.service"
import {ExecutionTemplateFilter} from "../../models/execution-template-filter"
import {ExecutionTemplateFilterService} from "./execution-template-filter.service"

@Component({
    selector: 'execution-template-filter',
    templateUrl: './execution-template-filter.component.html',
    styleUrls: ['./execution-template-filter.component.css'],
    standalone: false
})

export class ExecutionTemplateFilterComponent extends EntityComponent<ExecutionTemplateFilter> {

    templateId: number
    templateName: string
    fieldName: string
    comparedRegex: string
    identifier: number
    executionFilterForm: UntypedFormGroup
    existingIdentifiers: number[]

    constructor(
        private route: ActivatedRoute,
        private filterService: ExecutionTemplateFilterService) {
        super(route, 'execution-template-filter')
        if (this.breadcrumbsService.currentStep && this.mode == "create") {
            Promise.all([
                this.breadcrumbsService.currentStep.getPrefilledValue("templateId"),
                this.breadcrumbsService.currentStep.getPrefilledValue("templateName")
            ]).then(([templateId, templateName]) => {
                this.entity.executionTemplateId = templateId;
                this.templateId = templateId;
                this.templateName = templateName;

                this.filterService.getExistingIdentifiers(this)
            });
        }
    }

    get executionTemplateFilter(): ExecutionTemplateFilter { return this.entity }
    set executionTemplateFilter(et: ExecutionTemplateFilter) { this.entity= et }

    buildForm(): FormGroup {
        this.executionFilterForm = this.formBuilder.group({
            'fieldName': [this.entity.fieldName, [Validators.required, this.filterService.fieldNameFormatControl()]],
            'comparedRegex': [this.entity.comparedRegex, [Validators.required, this.filterService.comparedRegexFormatControl()]],
            'identifier': [this.entity.identifier, [Validators.required, this.filterService.uniqueInTemplateControl(this)]],
            'excluded': [this.entity.excluded]
        })
        return this.executionFilterForm
    }

    getService(): EntityService<ExecutionTemplateFilter> {
        return this.filterService
    }

    initCreate(): Promise<void> {
        this.entity = new ExecutionTemplateFilter()
        return Promise.resolve()
    }

    initEdit(): Promise<void> {
        this.templateId = this.entity.executionTemplateId
        this.filterService.getTemplateName(this)
        this.filterService.getExistingIdentifiers(this)
        return Promise.resolve()}

    initView(): Promise<void> {
        this.filterService.getTemplateName(this)
        return Promise.resolve()
    }

    save(): Promise<ExecutionTemplateFilter> {
        this.filterService.updateEntityOnSave(this)
        super.save().then(() => this.router.navigate(['execution-template/details/' + this.entity.executionTemplateId]))
        return Promise.resolve(this.entity)
    }

    goBack(): void{
        this.router.navigate(['execution-template/details/' + this.entity.executionTemplateId])
    }
}
