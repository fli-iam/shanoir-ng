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
import * as AppUtils from "../../utils/app.utils";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import {ExecutionTemplateParameter} from "../shared/execution-template-parameter.model";

@Injectable()
export class ExecutionTemplateParameterService extends EntityService<ExecutionTemplateParameter> {

    API_URL: string = AppUtils.BACKEND_API_VIP_EXEC_TEMPLATE_PARAM_URL;

    constructor(protected httpClient: HttpClient) {super(httpClient);}

    getEntityInstance(entity: ExecutionTemplateParameter | undefined): ExecutionTemplateParameter {return entity;}

    /**
     * Get all execution template parameters linked to an execution template
     * @param execution_template_id the execution template id we want the execution template parameters from
     */
    public getExecutionTemplateParametersByExecutionTemplate(execution_template_id: number): Promise<ExecutionTemplateParameter[]> {
        //return this.httpClient.get<ExecutionTemplateParameter[]>(this.API_URL + "/byExecutionTemplate/" + execution_template_id).toPromise();
        return new Promise<ExecutionTemplateParameter[]>(null); //TOREMOVE
    }
}
