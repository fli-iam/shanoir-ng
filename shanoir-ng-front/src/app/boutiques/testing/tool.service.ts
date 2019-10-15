import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpUrlEncodingCodec } from '@angular/common/http';
import { ToolInfo } from './tool.model';
import { getTestTools } from './test-tools';
import { asyncData } from '.';

@Injectable({
  providedIn: 'root'
})
export class ToolService {

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

  getAll(): Promise<ToolInfo[]> {
    return asyncData(this.tools).toPromise();
  }

  getDescriptor(toolId: number): Promise<any> {
    return asyncData(this.getFakeDescriptor()).toPromise();
  }

  getInvocation(toolId: number): Promise<string> {
    return asyncData(this.getFakeInvocation()).toPromise();
  }

  generateCommand(toolId: number, invocation: any): Promise<string> {
    return asyncData(this.getFakeCommand(invocation)).toPromise();
  }

  execute(toolId: number, invocation: any): Promise<string> {
    return asyncData(this.getFakeOutput(invocation)).toPromise();
  }
}
