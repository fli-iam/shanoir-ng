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
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import * as AppUtils from "../../../utils/app.utils";
import {Pipeline} from "../../models/pipeline";

@Injectable()
export class PipelineService {

    pipelineUrl: String = AppUtils.BACKEND_API_VIP_PIPE_URL;

    constructor(protected httpClient: HttpClient) {
    }

    /**
     * Show the definition of a pipeline
     *
     * @param pipelineIdentifier
     */
    public getPipeline(pipelineIdentifier: string): Observable<Pipeline> {

        if (pipelineIdentifier === null || pipelineIdentifier === undefined) {
            throw new Error('Required parameter pipelineIdentifier was null or undefined when calling getPipeline.');
        }
        return this.httpClient.get<Pipeline>(`${this.pipelineUrl}/${pipelineIdentifier}`);
    }

    /**
     * List all the pipelines that the user can execute. It is up to the platform to return pipelines that the user cannot execute. When studyIdentifier is present, all the pipelines that the user can execute in the study must be returned. In this case, execution rights denote the rights to execute the pipeline in the study.*
     *
     * @param studyIdentifier
     * @param property A pipeline property to filter the returned pipelines. It must listed in the \&quot;supportedPipelineProperties\&quot; of the getPlatformProperties method. All the returned pipelines must have this property set. Use also the \&quot;propertyValue\&quot; to filter on this property value.
     * @param propertyValue A property value on which to filter the returned pipelines. The \&quot;property\&quot; parameter must also be present. All the returned pipelines must have this property equal to the value given in this parameter.
     */
    public listPipelines(): Observable<Array<Pipeline>> {
        const httpHeaders: HttpHeaders = new HttpHeaders({
            apikey: 'imo804d70m73d4n54f18uhr5j0',
            rejectUnauthorized: 'false'
        });
        return this.httpClient.get<Array<Pipeline>>(`${this.pipelineUrl}`, { headers: httpHeaders });
    }
}
