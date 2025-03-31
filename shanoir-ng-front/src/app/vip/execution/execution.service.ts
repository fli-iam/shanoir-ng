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
import { HttpClient } from "@angular/common/http";
import {Observable} from "rxjs";
import * as AppUtils from "../../utils/app.utils";
import {ExecutionCandidateDto} from "../models/execution-candidate.dto";
import {IdName} from "../../shared/models/id-name.model";
import {ExecutionTemplate} from "../models/execution-template";

@Injectable()
export class ExecutionService {

  executionUrl: String = AppUtils.BACKEND_API_VIP_EXEC_URL;

  constructor(protected httpClient: HttpClient) {}


  /**
   * Initialize an execution
   * The successful response must contain the execution identifier. If the status “Initializing” is returned, playExecution must be called to start the execution.
   * @param body Execution
   */
  public createExecution(execution: ExecutionCandidateDto ): Promise<IdName> {

    if (execution === null || execution === undefined) {
      throw new Error('Required parameter execution was null or undefined when calling createExecution.');
    }
    return this.httpClient.post<IdName>(`${this.executionUrl}/`,execution).toPromise();
  }

    /**
     * Get all automatic executions linked to a study
     * @param study_id the study id we want the automatic executions from
     */
    public getAutomaticExecutions(study_id: number): Promise<ExecutionTemplate[]> {
        return this.httpClient.get<ExecutionTemplate[]>(`${this.executionUrl}/automatic/` + study_id).toPromise();
    }

  /**
   * Get stderr of an execution
   *
   * @param executionIdentifier
   */
  public getStderr(executionIdentifier: string ): Observable<string> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling getStderr.');
    }
    return this.httpClient.get(`${this.executionUrl}/${executionIdentifier}/stderr`, {responseType: 'text'});
  }

  /**
   * Get stdout of an execution
   *
   * @param executionIdentifier
   *
   */
  public getStdout(executionIdentifier: string ): Observable<string> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling getStdout.');
    }
    return this.httpClient.get(`${this.executionUrl}/${executionIdentifier}/stdout`, {responseType: 'text'});
  }

}
