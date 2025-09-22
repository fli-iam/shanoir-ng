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

import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import * as AppUtils from "../../../utils/app.utils";
import {EntityService} from "../../../shared/components/entity/entity.abstract.service";
import {ExecutionTemplateFilter} from "../../models/execution-template-filter";
import {ExecutionTemplateService} from "../execution-template.service";
import {ExecutionTemplateFilterComponent} from "./execution-template-filter.component";
import {AbstractControl, ValidationErrors, ValidatorFn} from "@angular/forms";

@Injectable()
export class ExecutionTemplateFilterService extends EntityService<ExecutionTemplateFilter> {

    API_URL: string = AppUtils.BACKEND_API_VIP_EXEC_TEMPLATE_FILTER_URL;

    constructor(protected httpClient: HttpClient,
                protected templateService: ExecutionTemplateService) {super(httpClient);}

    getEntityInstance(entity: ExecutionTemplateFilter | undefined): ExecutionTemplateFilter {return entity;}

    getExecutionTemplateFiltersByExecutionTemplate(execution_template_id: number): Promise<ExecutionTemplateFilter[]> {
        return this.httpClient.get<ExecutionTemplateFilter[]>(this.API_URL + "/byExecutionTemplate/" + execution_template_id).toPromise();
    }

    getTemplateName(filter: ExecutionTemplateFilterComponent) {
        this.templateService.get(filter.entity.executionTemplateId).then(template => {filter.templateName = template.name})
    }

    uniqueInTemplateControl(filter: ExecutionTemplateFilterComponent): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const identifier = control.value
            if (identifier !== undefined &&
                Number.isInteger(identifier) &&
                filter.existingIdentifiers &&
                filter.existingIdentifiers.includes(identifier)) {
                return {uniqueInTemplateControl: true}
            }
            return null
        }
    }

    getExistingIdentifiers(filter: ExecutionTemplateFilterComponent) {
        if(filter.existingIdentifiers === undefined) filter.existingIdentifiers = []
        this.getExecutionTemplateFiltersByExecutionTemplate(filter.entity.executionTemplateId).then(filters => {
            for(let filterEntity of filters) {
                filter.existingIdentifiers.push(filterEntity.identifier)
            }
            const index = filter.existingIdentifiers.indexOf(filter.entity.identifier);
            if (index > -1) {
                filter.existingIdentifiers.splice(index, 1);
            }
        })
    }

    fieldNameFormatControl(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const fieldName = control.value
                if (fieldName !== undefined &&
                    !this.checkFieldNameFormat(fieldName)) {
                return {fieldNameFormatControl: true}
            }
            return null
        }
    }

    checkFieldNameFormat(fieldName: string): boolean {
        return /^[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+$/.test(fieldName);
    }

    comparedRegexFormatControl(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const regex = control.value
            if (regex !== undefined &&
                !this.checkComparedRegexFormat(regex)) {
                return {comparedRegexFormatControl: true}
            }
            return null
        }
    }

    checkComparedRegexFormat(regex: string): boolean {
        return /^[a-zA-Z0-9_%-]+$/.test(regex)
    }

    updateEntityOnSave(filter: ExecutionTemplateFilterComponent) {
        const formValue = filter.form.value
        filter.entity.excluded = formValue.excluded
        filter.entity.fieldName = formValue.fieldName
        filter.entity.comparedRegex = formValue.comparedRegex
        filter.entity.identifier = formValue.identifier
    }
}
