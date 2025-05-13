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
import {AbstractControl, ValidatorFn} from "@angular/forms";

@Injectable()
export class ExecutionTemplateFilterService extends EntityService<ExecutionTemplateFilter> {

    API_URL: string = AppUtils.BACKEND_API_VIP_EXEC_TEMPLATE_FILTER_URL;

    constructor(protected httpClient: HttpClient,
                protected templateService: ExecutionTemplateService) {super(httpClient);}

    getEntityInstance(entity: ExecutionTemplateFilter | undefined): ExecutionTemplateFilter {return entity;}

    getExecutionTemplateFiltersByExecutionTemplate(execution_template_id: number): Promise<ExecutionTemplateFilter[]> {
        return this.httpClient.get<ExecutionTemplateFilter[]>(this.API_URL + "/byExecutionTemplate/" + execution_template_id).toPromise();
    }

    getStudyName(filter: ExecutionTemplateFilterComponent) {
        this.templateService.get(filter.entity.executionTemplateId).then(template => {filter.templateName = template.name})
    }

    uniqueInTemplateControl(filter: ExecutionTemplateFilterComponent): ValidatorFn {
        return (control: AbstractControl) => {
            if (filter.entity.identifier && this.checkIdentifierValidity(filter)) {
                return {uniqueInTemplateControl: true}
            }
            return null
        }
    }

    checkIdentifierValidity(filter: ExecutionTemplateFilterComponent): boolean {
        const identifier = filter.entity.identifier
        if(identifier === undefined || !Number.isInteger(identifier)) {
            return false
        }
        if (filter.existingIdentifiers!== undefined){
            return filter.existingIdentifiers.includes(identifier)
        }
        return false
    }

    getExistingIdentifiers(filter: ExecutionTemplateFilterComponent) {
        if(filter.existingIdentifiers === undefined) filter.existingIdentifiers = []
        this.getExecutionTemplateFiltersByExecutionTemplate(filter.entity.executionTemplateId).then(filters => {
            for(let filterEntity of filters) {
                filter.existingIdentifiers.push(filterEntity.identifier)
            }
        })
    }

    fieldNameFormatControl(filter: ExecutionTemplateFilterComponent): ValidatorFn {
        return (control: AbstractControl) => {
            if (filter.entity.fieldName && !this.checkFieldNameFormat(filter)) {
                return {fieldNameFormatControl: true}
            }
            return null
        }
    }

    checkFieldNameFormat(filter: ExecutionTemplateFilterComponent): boolean {
        return /^[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+$/.test(filter.entity.fieldName);
    }

    comparedRegexFormatControl(filter: ExecutionTemplateFilterComponent): ValidatorFn {
        return (control: AbstractControl) => {
            if (filter.entity.fieldName && !this.checkComparedRegexFormat(filter)) {
                return {comparedRegexFormatControl: true}
            }
            return null
        }
    }

    checkComparedRegexFormat(filter: ExecutionTemplateFilterComponent): boolean {
        return /^[a-zA-Z0-9_%-]+$/.test(filter.entity.comparedRegex);
    }
}
