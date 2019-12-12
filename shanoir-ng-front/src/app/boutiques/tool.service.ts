import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse, HttpUrlEncodingCodec } from '@angular/common/http';
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

  getDescriptor(toolId: string): Promise<any> {
    return this.httpClient.get<any>(`${this.API_URL}/${encodeURIComponent(toolId)}/descriptor/`).toPromise();
  }

  getInvocation(toolId: string): Promise<string> {
    return this.httpClient.get<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/invocation/`).toPromise();
  }

  generateCommand(toolId: string, invocation: any): Promise<string> {
    let httpOptions = Object.assign( { responseType: 'text' }, this.httpOptions);
    return this.httpClient.post<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/generate-command/`, invocation, httpOptions).toPromise();
  }

  execute(toolId: string, invocation: any, sessionId: number): Promise<string> {
    let httpOptions = Object.assign( { responseType: 'text' }, this.httpOptions);
    return this.httpClient.post<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/execute/${encodeURIComponent(sessionId)}`, invocation, httpOptions).toPromise();
  }

  getExecutionOutput(toolId: string, sessionId: number): Promise<any> {
    return this.httpClient.get<string>(`${this.API_URL}/${encodeURIComponent(toolId)}/output/${encodeURIComponent(sessionId)}`).toPromise();
  }

  downloadOutput(toolId: string, sessionId: number): void {
    if (!toolId) throw Error('Cannot download a dataset without an id');
    let httpOptions = Object.assign( { observe: 'response', responseType: 'blob' }, this.httpOptions);
    this.httpClient.get<HttpResponse<Blob>>(`${this.API_URL}/${encodeURIComponent(toolId)}/download-output/${encodeURIComponent(sessionId)}`, httpOptions).subscribe(response => this.downloadIntoBrowser(response));
  }

  private getFilename(response: HttpResponse<any>): string {
    const prefix = 'attachment;filename=';
    let contentDispHeader: string = response.headers.get('Content-Disposition');
    return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
  }

  private downloadIntoBrowser(response: HttpResponse<Blob>){
      AppUtils.browserDownloadFile(response.body, this.getFilename(response));
  }
}
