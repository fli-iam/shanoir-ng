import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import * as AppUtils from "../../utils/app.utils";
import {Execution} from "../models/execution";
import {Descriptor} from "../models/descriptor";
import {Path} from "../models/path";
import {Pipeline} from "../models/pipeline";
import {PlatformProperties} from "../models/platformProperties";

@Injectable()
export class CarminClientService {

  basePath: String = AppUtils.CARMIN_BASE_URL;

  constructor(protected httpClient: HttpClient) {}

  /**
   * Get the number of the user's executions
   *
   * @param studyIdentifier
   *
   */
  public countExecutions(studyIdentifier?: string): Observable<number> {

    let queryParameters = new HttpParams();
    queryParameters = queryParameters.set('studyIdentifier', <any>studyIdentifier);
    return this.httpClient.get<number>(`${this.basePath}/executions/count`,
      {
        params: queryParameters
      }
    );
  }

  /**
   * Initialize an execution
   * The successful response must contain the execution identifier. If the status “Initializing” is returned, playExecution must be called to start the execution.
   * @param body Execution
   */
  public createExecution(execution: Execution ): Observable<Execution> {

    if (execution === null || execution === undefined) {
      throw new Error('Required parameter execution was null or undefined when calling createExecution.');
    }
    return this.httpClient.post<Execution>(`${this.basePath}/executions`,execution);
  }

  /**
   * Delete an execution
   * This will kill the underlying processes (if possible) and free all resources associated with this execution (if deleteFiles parameter is present and true)
   * @param executionIdentifier
   * @param deleteFiles Also delete all the associated resources (especially the output files).
   */
  public deleteExecution(executionIdentifier: string, deleteFiles?: boolean): Observable<any> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling deleteExecution.');
    }

    let queryParameters = new HttpParams();
    if (deleteFiles !== undefined && deleteFiles !== null) {
      queryParameters = queryParameters.set('deleteFiles', <any>deleteFiles);
    }
    return this.httpClient.delete<any>(`${this.basePath}/executions/${executionIdentifier}`,
      {
        params: queryParameters
      }
    );
  }

  /**
   * Returns the Boutiques descriptor of the pipeline, if available.
   *
   * @param pipelineIdentifier
   *
   */
  public getBoutiquesDescriptor(pipelineIdentifier: string): Observable<Descriptor> {

    if (pipelineIdentifier === null || pipelineIdentifier === undefined) {
      throw new Error('Required parameter pipelineIdentifier was null or undefined when calling getBoutiquesDescriptor.');
    }
    return this.httpClient.get<Descriptor>(`${this.basePath}/pipelines/${pipelineIdentifier}/boutiquesdescriptor`);
  }

  /**
   * Get information about an execution
   *
   * @param executionIdentifier
   *
   */
  public getExecution(executionIdentifier: string): Observable<Execution> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling getExecution.');
    }
    return this.httpClient.get(`${this.basePath}/executions/${executionIdentifier}`);
  }

  /**
   * Get the result files of the execution
   *
   * @param executionIdentifier
   *
   */
  public getExecutionResults(executionIdentifier: string ): Observable<Array<Path>> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling getExecutionResults.');
    }
    return this.httpClient.get<Array<Path>>(`${this.basePath}/executions/${executionIdentifier}/results`);
  }

  /**
   * Show the definition of a pipeline
   *
   * @param pipelineIdentifier
   *
   */
  public getPipeline(pipelineIdentifier: string ): Observable<Pipeline> {

    if (pipelineIdentifier === null || pipelineIdentifier === undefined) {
      throw new Error('Required parameter pipelineIdentifier was null or undefined when calling getPipeline.');
    }
    return this.httpClient.get<Pipeline>(`${this.basePath}/pipelines/${pipelineIdentifier}`);
  }

  /**
   * Return information about the platform
   *
   */
  public getPlatformProperties(): Observable<PlatformProperties> {
    return this.httpClient.get<PlatformProperties>(`${this.basePath}/platform`);
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
    return this.httpClient.get<string>(`${this.basePath}/executions/${executionIdentifier}/stderr`);
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
    return this.httpClient.get<string>(`${this.basePath}/executions/${executionIdentifier}/stdout`);
  }

  /**
   * Kill an execution
   *
   * @param executionIdentifier
   */
  public killExecution(executionIdentifier: string): Observable<any> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling killExecution.');
    }

    return this.httpClient.put<any>(`${this.basePath}/executions/${executionIdentifier}/kill`,{});
  }

  /**
   * List some executions.
   * List all execution Ids in the platform which are ordered in decreasing submission time. All the executions that were launched by the user must be returned. It is up to the platform to return executions that the user did not launch. Return the executions with indexes ranging from offset to offset+limit-1. When studyIdentifier is present, all the executions that the user launched in the study must be returned.
   * @param studyIdentifier When present, all the executions the user launched for this study must be returned
   * @param offset Index of the first execution to be returned. Defaults to 0.
   * @param limit Maximum number of exeuctions to be returned. Defaults to the \&quot;defaultLimitListExecutions\&quot; property in the getPlatformProperties method if present, to 500 otherwise.
   */
  public listExecutions(studyIdentifier?: string, offset?: string, limit?: string ): Observable<Array<Execution>> {

    let queryParameters = new HttpParams();
    if (studyIdentifier !== undefined && studyIdentifier !== null) {
      queryParameters = queryParameters.set('studyIdentifier', studyIdentifier);
    }
    if (offset !== undefined && offset !== null) {
      queryParameters = queryParameters.set('offset', offset);
    }
    if (limit !== undefined && limit !== null) {
      queryParameters = queryParameters.set('limit', limit);
    }

    return this.httpClient.get<Array<Execution>>(`${this.basePath}/executions`,
      {
        params: queryParameters
      }
    );
  }

  /**
  * List pipelines
  * All the pipelines that the user can execute must be returned. It is up to the platform to return pipelines that the user cannot execute. When studyIdentifier is present, all the pipelines that the user can execute in the study must be returned. In this case, execution rights denote the rights to execute the pipeline in the study.
  * @param studyIdentifier
  * @param property A pipeline property to filter the returned pipelines. It must listed in the \&quot;supportedPipelineProperties\&quot; of the getPlatformProperties method. All the returned pipelines must have this property set. Use also the \&quot;propertyValue\&quot; to filter on this property value.
  * @param propertyValue A property value on which to filter the returned pipelines. The \&quot;property\&quot; parameter must also be present. All the returned pipelines must have this property equal to the value given in this parameter.
   *
  */

  public listPipelines(studyIdentifier?: string, property?: string, propertyValue?: string ): Observable<Array<Pipeline>> {

    let queryParameters = new HttpParams();
    if (studyIdentifier !== undefined && studyIdentifier !== null) {
      queryParameters = queryParameters.set('studyIdentifier', <any>studyIdentifier);
    }
    if (property !== undefined && property !== null) {
      queryParameters = queryParameters.set('property', <any>property);
    }
    if (propertyValue !== undefined && propertyValue !== null) {
      queryParameters = queryParameters.set('propertyValue', <any>propertyValue);
    }

    return this.httpClient.get<Array<Pipeline>>(`${this.basePath}/pipelines`,
      {
        params: queryParameters
      }
    );
  }

  /**
   * Play an execution
   *
   * @param executionIdentifier
   *
   */
  public playExecution(executionIdentifier: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling playExecution.');
    }
    return this.httpClient.put<any>(`${this.basePath}/executions/${encodeURIComponent(String(executionIdentifier))}/play`, {});
  }

  /**
   * Modify an execution.
   * Only the name and the timeout of the execution can be modified. Changes to the identifier or the status will raise errors. Changes to the other properties will be ignored.
   * @param execution
   * @param executionIdentifier
   *
   */
  public updateExecution(execution: Execution, executionIdentifier: string ): Observable<any> {

    if (execution === null || execution === undefined) {
      throw new Error('Required parameter body was null or undefined when calling updateExecution.');
    }

    if (executionIdentifier === null || executionIdentifier === undefined) {
      throw new Error('Required parameter executionIdentifier was null or undefined when calling updateExecution.');
    }

    return this.httpClient.put<any>(`${this.basePath}/executions/${encodeURIComponent(String(executionIdentifier))}`,execution);
  }


}
