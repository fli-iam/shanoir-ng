import { Component, OnInit, Input } from '@angular/core';
import { RxStompService } from '@stomp/ng2-stompjs';
import { ToolService } from '../tool.service';
import { ToolInfo } from '../tool.model';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';

@Component({
  selector: 'execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {
  
  @Input() tool: ToolInfo;
  generatedCommand: string = null
  invocation: any = null
  output: string = null
  // private topicSubscription: Subscription;

  constructor(private toolService: ToolService/*, private rxStompService: RxStompService*/) { }

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
    if(this.tool == null) {
      console.log('Please select a tool first.') // TODO: display a real message
      return;
    }
    this.invocation = invocation;
    this.toolService.generateCommand(this.tool.id, this.invocation).then((generatedCommand)=> this.generatedCommand = generatedCommand);
  }

  onExecuteTool() {
    if(this.tool == null) {
      console.log('Please select a tool first.') // TODO: display a real message
      return;
    }
    if(this.invocation == null || this.invocation == '') {
      console.log('Invocation is empty.') // TODO: display a real message
      return;
    }
    this.toolService.execute(this.tool.id, this.invocation).then((output)=> this.output = output);
  }
}
