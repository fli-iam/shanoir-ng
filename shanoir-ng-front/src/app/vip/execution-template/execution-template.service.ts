/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import {Injectable} from "@angular/core"
import {HttpClient} from "@angular/common/http"
import * as AppUtils from "../../utils/app.utils"
import {ExecutionTemplate} from "../models/execution-template"
import {EntityService} from "../../shared/components/entity/entity.abstract.service"
import {StudyService} from "../../studies/shared/study.service"
import {ExecutionTemplateParameter} from "../models/execution-template-parameter"
import {Pipeline} from "../models/pipeline"
import {ExecutionTemplateComponent} from "./execution-template.component"
import {PipelineService} from "../pipelines/pipeline/pipeline.service"
import {
    AbstractControl,
    UntypedFormBuilder,
    UntypedFormControl, ValidationErrors,
    ValidatorFn,
    Validators
} from "@angular/forms"

@Injectable()
export class ExecutionTemplateService extends EntityService<ExecutionTemplate> {

    API_URL: string = AppUtils.BACKEND_API_VIP_EXEC_TEMPLATE_URL

    constructor(protected httpClient: HttpClient,
                protected studyService: StudyService,
                protected pipelineService: PipelineService) {
        super(httpClient)
    }

    getEntityInstance(entity: ExecutionTemplate | undefined): ExecutionTemplate {return new ExecutionTemplate()}

    getExecutionTemplatesByStudy(study_id: number): Promise<ExecutionTemplate[]> {
        return this.httpClient.get<ExecutionTemplate[]>(this.API_URL + "/byStudy/" + study_id).toPromise()
    }

    getStudyName(template: ExecutionTemplateComponent) {
        this.studyService.get(template.entity.studyId).then(study => {template.studyName = study.name})
    }

    get(id: number, mode: 'eager' | 'lazy' = 'eager'): Promise<ExecutionTemplate> {
         return this.http.get<any>(this.API_URL + '/' + id)
            .toPromise()
            .then(eheh => this.mapEntity(eheh))
    }

    initParameters(template: ExecutionTemplateComponent){
        template.pipelineParameters = {
            execution_name: '',
            export_format: 'nii',
            converter: '',
            processing_type: '',
            pipeline_identifier: '',
        }
    }

    getParameters(template: ExecutionTemplateComponent) {
        template.pipelineParameters = {}
        template.entity.parameters.forEach(parameter => {
            this.updateFormValueAndPipelineParameters(parameter.name, parameter.value, template)
        })
    }

    checkOneDatasetGroup(template: ExecutionTemplateComponent) {
        var datasetGroup = false;
        (Object.entries(template.pipelineParameters) as [string, string][]).forEach(([key, value]) => {
            if(key.endsWith("_group") && value === "dataset"){
                if(!datasetGroup){
                    datasetGroup = true
                } else {
                    new Error("Cannot save: there can not be multiple dataset group.")
                }
            }
        })
    }

    cleanParameters(template: ExecutionTemplateComponent) {
        const allowedKeys = ['execution_name','export_format','converter','processing_type','pipeline_identifier']
        template.pipeline.parameters.forEach(parameter => {
            allowedKeys.push(parameter.name)
            allowedKeys.push(parameter.name + "_group")
        })
        Object.keys(template.pipelineParameters).forEach(key => {
            if (!allowedKeys.includes(key)) {
                delete template.pipelineParameters[key]
            }
        })
    }

    shapeParameterEntities(template: ExecutionTemplateComponent) {
        template.entity.parameters = template.entity.parameters || [];
        var tempo: ExecutionTemplateParameter[] = [];
        (Object.entries(template.pipelineParameters) as [string, string][]).forEach(([key, value]) => {
            var existingParameter = template.entity.parameters.find(parameter => parameter.name === key.replace(/_value$/, ''))
            if(existingParameter){
                existingParameter.value = value
                tempo.push(existingParameter)
            } else {
                var param = new ExecutionTemplateParameter()
                param.name = key.replace(/_value$/, '')
                param.value = value
                tempo.push(param)
            }
        })
        template.entity.parameters = tempo
    }

    updateExecutionName(template: ExecutionTemplateComponent) {
        if(template.pipeline !== undefined){
            this.updateFormValueAndPipelineParameters('execution_name', 'Auto-exec-'+ template.entity.pipelineName.replace(/[^a-zA-Z0-9_]/g, '_'), template)
            this.updateFormValueAndPipelineParameters('pipeline_identifier', template.pipeline.identifier, template)
        }
    }

    createSpecificPipelineParameters(template: ExecutionTemplateComponent) {
        template.pipeline.parameters.forEach(specificParameter => {
            this.updateFormValueAndPipelineParameters(specificParameter.name, '', template)
            this.updateFormValueAndPipelineParameters(specificParameter.name + "_group", '', template)
        })
    }

    filterCombinationControl(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const raw = control.value
            if (raw && this.checkFilterCombinationValidity(raw)) {
                return { filterCombinationControl: true }
            }
            return null
        };
    }

    checkFilterCombinationValidity(filterCombination: string): boolean {
        const cleaned = filterCombination.replace(/\s+/g, ' ').trim().toUpperCase();

        if (cleaned === 'OR' || cleaned === 'AND') {
            return false;
        } else if (!this.checkParenthesisBalance(cleaned)) {
            return true;
        }

        const tokens = cleaned.split(/(\s+|\b|\(|\))/).map(t => t.trim()).filter(t => t.length > 0);
        return !this.checkTokensCombinationValidity(tokens);
    }

    checkTokensCombinationValidity(tokens: string[]) {
        const operatorSet = new Set(['AND', 'OR'])
        let expectOperand = true

        for (const token of tokens) {
            if (token === '(') {
                if (!expectOperand) return false
                continue
            }
            if (token === ')') {
                if (expectOperand) return false
                continue
            }
            if (operatorSet.has(token)) {
                if (expectOperand) return false
                expectOperand = true
                continue
            }
            if (/^[1-9]\d*$/.test(token)) {
                if (!expectOperand) return false
                expectOperand = false
                continue
            }
            return false
        }

        return !expectOperand
    }

    checkParenthesisBalance(filterCombination: string) {
        let balance = 0
        for (const char of filterCombination) {
            if (char === '(') balance++
            if (char === ')') balance--
            if (balance < 0) return false
        }
        return balance === 0

    }

    getPipelinesFromVIP(template: ExecutionTemplateComponent) {
        this.pipelineService.listPipelines().subscribe(
            (pipelines :Pipeline[])=>{
                template.pipelines = pipelines
                template.pipelineNames = pipelines.map(pipeline => pipeline.identifier)
            }
        )
    }

    onPipelineUpdate(template: ExecutionTemplateComponent, formBuilder: UntypedFormBuilder): void{
        template.entity.pipelineName = template.form.get('pipelineName').value
        if(template.entity.pipelineName !== null){
            this.pipelineService.getPipeline(template.entity.pipelineName).subscribe(pipeline => {
                template.pipeline = pipeline
                this.updateExecutionName(template)
                this.clearSpecificParametersControls(template)
                this.createSpecificParametersControls(template, formBuilder)
                this.createSpecificPipelineParameters(template)
                template.cdr.detectChanges()
            })
        }
    }

    onPipelineLoading(template: ExecutionTemplateComponent, formBuilder: UntypedFormBuilder): void{
        this.pipelineService.getPipeline(template.entity.pipelineName).subscribe(pipeline => {
            template.pipeline = pipeline
            this.createSpecificParametersControls(template, formBuilder)
        })
    }

    requiredIfTypeIsNii(): ValidatorFn {
        return (control: AbstractControl) => {
            const export_format = control?.parent?.get('export_format')?.value
            const isRequired = export_format === 'nii'
            const value = control?.value

            if (isRequired && !value) {
                return {requiredIfTypeIsNii: true}
            }
            return null
        }
    }

    uniqueInStudyControl(template: ExecutionTemplateComponent): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const priority = control.value
            if (
                priority !== undefined &&
                Number.isInteger(priority) &&
                template.existingPriorities &&
                template.existingPriorities.includes(priority)) {
                    return { uniqueInStudyControl: true}
                }
            return null
        }
    }

    getExistingPriorities(template: ExecutionTemplateComponent) {
        if(template.existingPriorities === undefined) template.existingPriorities = []
        this.getExecutionTemplatesByStudy(template.studyId).then(templates => {
            for(let templateEntity of templates) {
                template.existingPriorities.push(templateEntity.priority)
            }
            const index = template.existingPriorities.indexOf(template.entity.priority);
            if (index > -1) {
                template.existingPriorities.splice(index, 1);
            }
        })
    }

    createSpecificParametersControls(template: ExecutionTemplateComponent, formBuilder: UntypedFormBuilder) {
        template.pipeline.parameters.forEach(specificParameter => {
            if (!template.form.contains(specificParameter.name)) {
                template.form.addControl(specificParameter.name + "_group", new UntypedFormControl(template.pipelineParameters[specificParameter.name + "_group"], Validators.required))
                template.form.addControl(specificParameter.name + "_value", new UntypedFormControl(template.pipelineParameters[specificParameter.name]))
            }
        })
    }

    clearSpecificParametersControls(template: ExecutionTemplateComponent) {
        const controls = template.form.controls;
        Object.keys(controls).forEach(controlName => {
            if (controlName.endsWith('_group') || controlName.endsWith('_value')) {
                template.form.removeControl(controlName);
            }
        });
    }

    updateEntityOnSave(template: ExecutionTemplateComponent) {
        const formValue = template.form.value
        template.entity.filterCombination = formValue.filterCombination
        template.entity.priority = formValue.priority
        template.entity.name = formValue.name
        template.entity.pipelineName = formValue.pipelineName
    }

    updateFormValueAndPipelineParameters(name: string, value: any, template: ExecutionTemplateComponent){
        template.pipelineParameters[name] = value
        const commonParameter = ['execution_name','export_format','converter','processing_type','pipeline_identifier']
        if(!commonParameter.includes(name) && !name.endsWith('_group')){
            name = name + '_value'
        }
        if(template.form !== undefined){
            template.form.get(name)?.setValue(value);
        }
    }
}
