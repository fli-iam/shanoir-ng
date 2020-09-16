import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpUrlEncodingCodec } from '@angular/common/http';
import { ToolInfo } from './tool.model';
import { getTestTools } from './test-tools';
import { asyncData } from '.';
import { BoutiquesData } from '../tool.service';

@Injectable({
  providedIn: 'root'
})
export class ToolService {

  data: BoutiquesData = { sessionId: Date.now(), isProcessing: false };

  constructor() {}

  tools = getTestTools();

  search(query: string): Promise<ToolInfo[]> {

    if(query.length == 0) {
      return this.getAll();
    } else {
      let results = [];
      let queryWords = query.split(' ');
      for(let word of queryWords) {
        for(let tool of this.tools) {
          if(tool.name.indexOf(word) >= 0) {
            results.push(tool)
          }
        }
      }
      return asyncData(results).toPromise();
    }
  }

  getFakeDescriptor() {
    return { name: 'fake descriptor', description: 'fake description', tags: 'fake tags'};
  }

  getFakeInvocation() {
    return 'fake invocation';
  }

  getFakeCommand(invocation: string) {
    return 'fake command from ' + invocation;
  }

  getFakeOutput(invocation: string) {
    return 'fake output from ' + invocation;
  }

  getFakeExecutionOutput() {
    return { input: ['fake execution output'], error: [], finished: true };
  }

  getAll(): Promise<ToolInfo[]> {
    return asyncData(this.tools).toPromise();
  }

  getDescriptor(toolId: number): Promise<any> {
    return asyncData(this.getFakeDescriptor()).toPromise();
  }

  getDefaultInvocation(toolId: number): Promise<string> {
    return asyncData(this.getFakeInvocation()).toPromise();
  }

  generateCommand(toolId: number, invocation: any): Promise<string> {
    return asyncData(this.getFakeCommand(invocation)).toPromise();
  }

  execute(toolId: number, invocation: any): Promise<string> {
    return asyncData(this.getFakeOutput(invocation)).toPromise();
  }

  cancelExecution(toolId: string): Promise<string> {
    return asyncData('fake cancel success').toPromise();
  }

  getExecutionOutput(toolId: string): Promise<any> {
    return asyncData(this.getFakeExecutionOutput()).toPromise();
  }

  downloadOutput(toolId: string): void {
    asyncData('fake output').subscribe(response => this.downloadIntoBrowser(response));
  }

  private downloadIntoBrowser(response: any){
      
  }

  saveSession(data: BoutiquesData) {
    this.data = { ...this.data, ...data };
    sessionStorage.setItem('boutiques', JSON.stringify(this.data));
  }

  loadSession() {
    let data = JSON.parse(sessionStorage.getItem('boutiques'));
    if(data) {
      this.data = data;
    }
    return this.data;
  }
}
