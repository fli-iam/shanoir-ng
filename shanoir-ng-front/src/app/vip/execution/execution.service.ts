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

@Injectable()
export class ExecutionService {

  executionUrl: string = AppUtils.BACKEND_API_VIP_EXEC_URL;

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
   * Get stderr of a processing
   *
   * @param processingId
   */
  public getStderr(processingId: number ): Observable<string> {

    if (processingId === null || processingId === undefined) {
      throw new Error('Required parameter processingId was null or undefined when calling getStderr.');
    }
    return this.httpClient.get(`${this.executionUrl}/${processingId}/stderr`, {responseType: 'text'});
  }

  /**
   * Get stdout of a processing
   *
   * @param processingId
   *
   */
  public getStdout(processingId: number ): Observable<string> {

    if (processingId === null || processingId === undefined) {
      throw new Error('Required parameter processingId was null or undefined when calling getStdout.');
    }
    return this.httpClient.get(`${this.executionUrl}/${processingId}/stdout`, {responseType: 'text'});
  }

}
