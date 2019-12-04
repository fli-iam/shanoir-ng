import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpUrlEncodingCodec } from '@angular/common/http';
import { ToolInfo } from './tool.model';
import { EntityService } from '../shared/components/entity/entity.abstract.service';
import { ServiceLocator } from '../utils/locator.service';
import * as AppUtils from '../utils/app.utils';

@Injectable({
  providedIn: 'root'
})
export class ToolService extends EntityService<ToolInfo> {

  API_URL = 'http://localhost:9906/tool';
  // API_URL = 'https://shanoir-ng/boutiques/tool';
  // API_URL = AppUtils.BACKEND_API_BOUTIQUES_TOOL_URL;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private httpClient: HttpClient) {
    super(httpClient)
  }

  getEntityInstance(entity?: ToolInfo): ToolInfo
  {
    return new ToolInfo(this);
  }

  search(query: string): Promise<ToolInfo[]> {

    let queryParameters = new HttpParams({encoder: new HttpUrlEncodingCodec()});
    if (query) {
      queryParameters = queryParameters.append('query', <any>query);
    }

    return this.httpClient.get<ToolInfo[]>(`${this.API_URL}/search`,
      {
        params: queryParameters,
      }
    ).toPromise();
  }

  getAll(): Promise<ToolInfo[]> {
    return this.httpClient.get<Array<ToolInfo>>(`${this.API_URL}/all`).toPromise();
  }

  getDescriptor(toolId: number): Promise<any> {
    return this.httpClient.get<any>(`${this.API_URL}/${encodeURIComponent(toolId)}/descriptor/`).toPromise();
  }

  getInvocation(toolId: number): Promise<string> {
    return this.httpClient.get<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/invocation/`).toPromise();
  }

  generateCommand(toolId: number, invocation: any): Promise<string> {
    let httpOptions = Object.assign( { responseType: 'text' }, this.httpOptions);
    return this.httpClient.post<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/generate-command/`, invocation, httpOptions).toPromise();
  }

  execute(toolId: number, invocation: any): Promise<string> {
    let httpOptions = Object.assign( { responseType: 'text' }, this.httpOptions);
    return this.httpClient.post<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/execute/`, invocation, httpOptions).toPromise();
  }

  getExecutionOutput(toolId: number): Promise<any> {
    return this.httpClient.post<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/output/`).toPromise();
  }

  downloadOutput(toolId: number): Promise<any> {
    return this.httpClient.post<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/download-output/`).toPromise();
  }
}
