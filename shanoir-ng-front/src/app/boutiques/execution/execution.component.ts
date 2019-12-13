import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { RxStompService } from '@stomp/ng2-stompjs';
import { ToolService } from '../tool.service';
import { ToolInfo } from '../tool.model';
import { Message } from '@stomp/stompjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { Observable, Subject, Subscription } from 'rxjs';
import { default as AnsiUp } from 'ansi_up';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

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
  sessionId: number = Date.now();
  
  private invocationSubject = new Subject<string>();

  static readonly ansi_up = new AnsiUp();
  // private topicSubscription: Subscription;

  constructor(private toolService: ToolService, 
              private breadcrumbsService: BreadcrumbsService, 
              private sanitizer: DomSanitizer,
              private msgBoxService: MsgBoxService) { }

  ngOnInit() {
    // this.topicSubscription = this.rxStompService.watch('/message/messages').subscribe((message: Message) => {
    //   console.log(message.body);
    //   this.output += message.body + "\n";
    // });

    if(this.breadcrumbsService.currentStep.data.boutiquesExecution != null) {
      this.sessionId = this.breadcrumbsService.currentStep.data.boutiquesExecution;
      this.setGetOutputInterval();
    }

    this.invocationSubject.pipe(
      // wait 0.5 second after each keystroke before considering the term
      debounceTime(500),

      // ignore new term if same as previous term
      distinctUntilChanged()
    ).subscribe((invocation: any)=> { 
      this.invocation = invocation;
      this.toolService.generateCommand(this.toolId, this.invocation).then((generatedCommand)=> { 
        this.generatedCommand = generatedCommand;
      });

    });
  }

  ngOnDestroy() {
    // this.topicSubscription.unsubscribe();
  }

  onInvocationChanged(invocation: any) {
    if(this.toolId == null) {
      this.msgBoxService.log('warn', 'No tool selected.');
      return;
    }

    this.invocationSubject.next(invocation);
  }

  executeToolButton() {
    return this.processFinished == null || this.processFinished ? "Execute tool" : "Processing...";
  }

  onExecuteTool() {
    if(this.processFinished != null && !this.processFinished) {
      this.msgBoxService.log('warn', 'Process is not finished yet.');
      return;
    }
    if(this.toolId == null) {
      this.msgBoxService.log('warn', 'No tool selected.');
      return;
    }
    if(this.invocation == null || this.invocation == '') {
      this.msgBoxService.log('warn', 'Invocation is empty.');
      return;
    }
    this.outputLines = [];
    this.processFinished = false;
    this.breadcrumbsService.currentStep.data.boutiquesExecutionId = this.sessionId;
    this.breadcrumbsService.saveSession();
    this.toolService.execute(this.toolId, this.invocation, this.sessionId).then((output)=> {
      // this.output = output;
      if(output.startsWith('Error')) {
        this.msgBoxService.log('warn', output);
        return;
      }
      this.setGetOutputInterval();
    });
  }

  setGetOutputInterval() {
    this.executionIntervalId = setInterval(()=> this.getOutput(), 500);
  }

  getOutput() {
    this.toolService.getExecutionOutput(this.toolId, this.sessionId).then((output:any)=> {
      if(output != null && output.input != null) {
        for(let input of output.input) {
          this.outputLines.push(this.sanitizer.bypassSecurityTrustHtml(ExecutionComponent.ansi_up.ansi_to_html(input)));
        }
      }
      if(output != null && output.error != null) {
        for(let error of output.error) {
          this.outputLines.push(this.sanitizer.bypassSecurityTrustHtml(ExecutionComponent.ansi_up.ansi_to_html(error)));
        }
      }
      if(output == null || output.finished) {
        clearInterval(this.executionIntervalId);
        this.processFinished = true;
      }
    });
  }

  onDownloadResults(link: string) {
    this.toolService.downloadOutput(this.toolId, this.sessionId);
  }
}
