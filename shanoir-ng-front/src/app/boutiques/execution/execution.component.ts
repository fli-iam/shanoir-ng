import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { RxStompService } from '@stomp/ng2-stompjs';
import { ToolService } from '../tool.service';
import { ToolInfo } from '../tool.model';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { default as AnsiUp } from 'ansi_up';



@Component({
  selector: 'execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

  @Input() toolId: string;
  generatedCommand: string = null
  invocation: any = null
  // output: string = null
  outputLines: SafeHtml[] = [];
  executionIntervalId = null;
  processFinished: boolean = null;
  
  static readonly ansi_up = new AnsiUp();
  // private topicSubscription: Subscription;

  constructor(private toolService: ToolService, private sanitizer: DomSanitizer/*, private rxStompService: RxStompService*/) { }

  ngOnInit() {
    // this.topicSubscription = this.rxStompService.watch('/message/messages').subscribe((message: Message) => {
    //   console.log(message.body);
    //   this.output += message.body + "\n";
    // });
  }

  ngOnDestroy() {
    // this.topicSubscription.unsubscribe();
  }

  onInvocationChanged(invocation: any) {
    if(this.toolId == null) {
      console.log('Please select a tool first.') // TODO: display a real message
      return;
    }
    this.invocation = invocation;
    this.toolService.generateCommand(this.toolId, this.invocation).then((generatedCommand)=> { 
      this.generatedCommand = generatedCommand;
    });
  }

  executeToolButton() {
    return this.processFinished == null || this.processFinished ? "Execute tool" : "Processing..."
  }

  onExecuteTool() {
    if(this.processFinished != null && !this.processFinished) {
      console.log('Process is not finished yet.') // TODO: display a real message
      return;
    }
    if(this.toolId == null) {
      console.log('Please select a tool first.') // TODO: display a real message
      return;
    }
    if(this.invocation == null || this.invocation == '') {
      console.log('Invocation is empty.') // TODO: display a real message
      return;
    }
    this.outputLines = [];
    this.processFinished = false;
    this.toolService.execute(this.toolId, this.invocation).then((output)=> {
      // this.output = output;
      this.executionIntervalId = setInterval(()=> this.getOutput(), 500);
    });
  }

  getOutput() {
    this.toolService.getExecutionOutput(this.toolId).then((output:any)=> {
      if(output.input != null) {
        for(let input of output.input) {
          this.outputLines.push(this.sanitizer.bypassSecurityTrustHtml(ExecutionComponent.ansi_up.ansi_to_html(input)));
        }
      }
      if(output.error != null) {
        for(let error of output.error) {
          this.outputLines.push(this.sanitizer.bypassSecurityTrustHtml(ExecutionComponent.ansi_up.ansi_to_html(error)));
        }
      }
      if(output.finished) {
        clearInterval(this.executionIntervalId);
        this.processFinished = true;
      }
    });
  }

  onDownloadResults(link: string) {
    this.toolService.downloadOutput(this.toolId);
  }
}
