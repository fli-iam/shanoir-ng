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
import {AbstractControl, AsyncValidatorFn, ValidatorFn} from "@angular/forms"

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

    getStudyName(template) {
        this.studyService.get(template.entity.studyId).then(study => {template.studyName = study.name})
    }

    get(id: number, mode: 'eager' | 'lazy' = 'eager'): Promise<ExecutionTemplate> {
         return this.http.get<any>(this.API_URL + '/' + id)
            .toPromise()
            .then(eheh => this.mapEntity(eheh))
    }

    initParameters(template){
        template.pipelineParameters = {
            execution_name: '',
            export_format: 'nii',
            group_by: 'examination',
            converter: '',
            processing_type: '',
            pipeline_identifier: ''
        }
    }

    getParameters(template) {
        template.pipelineParameters = {}
        template.entity.parameters.forEach(parameter => {
            template.pipelineParameters[parameter.name] = parameter.value
        })
    }

    cleanParameters(template) {
        const allowedKeys = ['execution_name','export_format','group_by','converter','processing_type','pipeline_identifier']
        template.pipeline.parameters.forEach(parameter => allowedKeys.push(parameter.name))
        Object.keys(template.pipelineParameters).forEach(key => {
            if (!allowedKeys.includes(key)) {
                delete template.pipelineParameters[key]
            }
        })
    }

    shapeParameterEntities(template) {
        template.entity.parameters = template.entity.parameters || [];
        (Object.entries(template.pipelineParameters) as [string, string][]).forEach(([key, value]) => {
            var existingParameter = template.entity.parameters.find(parameter => parameter.name === key)
            if(existingParameter){
                existingParameter.value = value
            } else {
                var param = new ExecutionTemplateParameter()
                param.name = key
                param.value = value
                template.entity.parameters.push(param)
            }
        })
    }

    updateExecutionName(template) {
        if(template.pipeline != undefined){
            template.pipelineParameters['execution_name'] = 'Auto-exec-'+ template.entity.pipelineName.replace(/[^a-zA-Z0-9_]/g, '_')
            template.pipelineParameters['pipeline_identifier'] = template.pipeline.identifier
            template.pipelineParameters['processing_type'] = template.pipeline.processingType
        }
    }

    createSpecificPipelineParameters(template) {
        template.pipeline.parameters.forEach(specificParameter => template.pipelineParameters[specificParameter.name] = '')
    }

    filterCombinationControl(template: ExecutionTemplateComponent): ValidatorFn {
        return (control: AbstractControl) => {
            if (template.entity.filterCombination && this.checkFilterCombinationValidity(template)) {
                return {filterCombinationControl: true}
            }
            return null
        }
    }

    checkFilterCombinationValidity(template) {
        const filterCombination = template.entity.filterCombination.replace(/\s+/g, ' ').trim().toUpperCase()
        if(filterCombination === 'OR' || filterCombination === 'AND') {
            return false
        } else if(!this.checkParenthesisBalance(filterCombination)) {
            return true
        }

        const tokens = filterCombination.split(/(\s+|\b|\(|\))/).map(t => t.trim()).filter(t => t.length > 0)
        return !this.checkTokensCombinationValidity(tokens)
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

    onPipelineUpdate(template: ExecutionTemplateComponent): void{
        if(template.entity.pipelineName !== null){
            this.pipelineService.getPipeline(template.entity.pipelineName).subscribe(pipeline => {
                template.pipeline = pipeline
                this.updateExecutionName(template)
                this.createSpecificPipelineParameters(template)
                template.cdr.detectChanges()
            })
        }
    }

    onPipelineLoading(template: ExecutionTemplateComponent): void{
        this.pipelineService.getPipeline(template.entity.pipelineName).subscribe(pipeline => {
            template.pipeline = pipeline
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
        return (control: AbstractControl) => {
            if (template.entity.priority && this.checkPriorityValidity(template)) {
                return {uniqueInStudyControl: true}
            }
            return null
        }
    }

    checkPriorityValidity(template: ExecutionTemplateComponent): boolean {
        const priority = template.entity.priority
        if(priority === undefined || !Number.isInteger(priority)) {
            return false
        }
        if (template.existingPriorities !== undefined){
            return template.existingPriorities.includes(priority)
        }
        return false
    }

    getExistingPriorities(template: ExecutionTemplateComponent) {
        if(template.existingPriorities === undefined) template.existingPriorities = []
        this.getExecutionTemplatesByStudy(template.entity.studyId).then(templates => {
            for(let templateEntity of templates) {
                template.existingPriorities.push(templateEntity.priority)
            }
        })
    }
}
