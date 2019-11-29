import { Component, OnInit, Input, Output, EventEmitter, ElementRef, ViewChild } from '@angular/core';
import { ToolDescriptorInfoComponent } from '../tool-descriptor-info/tool-descriptor-info.component';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';
import { InvocationGuiComponent } from '../invocation-gui/invocation-gui.component';
import { Observable, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Component({
  selector: 'invocation',
  templateUrl: './invocation.component.html',
  styleUrls: ['./invocation.component.css']
})
export class InvocationComponent implements OnInit {
  
  @Input() tool: ToolInfo;
  
  @Output() invocationChanged = new EventEmitter<any>();

  @ViewChild(InvocationGuiComponent, { static: false }) invocationGUI: InvocationGuiComponent;

  descriptor: any = null
  
  invocation: any = null
  invocationChangedTimeoutID: any = null

  private invocationSubject = new Subject<string>();

  constructor(private toolService: ToolService) {
  }

  ngOnInit(): void {
    this.invocationSubject.pipe(
      // wait 1 second after each keystroke before considering the term
      debounceTime(1000),

      // ignore new term if same as previous term
      distinctUntilChanged()
    ).subscribe((invocationValue: string)=> this.updateInvocation(invocationValue));

    this.toolService.getDescriptor(this.tool.id).then((descriptor)=> this.descriptor = descriptor);
    this.toolService.getInvocation(this.tool.id).then((invocation)=> {
      this.invocation = invocation;
      this.invocationChanged.emit(this.invocation);
    });
  }

  updateInvocation(invocationValue: string) {
    try{
      this.invocation = JSON.parse(invocationValue);
      this.invocationChanged.emit(this.invocation);
    } catch(e) {
      console.log('error occored while you were typing the JSON');
      console.log(e);
    };
  }

  onInvocationChanged(invocation) {
    this.invocation = invocation;
    this.invocationChanged.emit(this.invocation);
    this.invocationValue = this.getInvocationValue();
  }

  getInvocationValue() {
    return this.invocation ? JSON.stringify(this.invocation, null, 2) : '';
  }

  get invocationValue() {
    return this.getInvocationValue();
  }

  set invocationValue(v) {
    this.invocationSubject.next(v);
  }

  testInvocation() {
    this.invocationGUI.testInvocation(this.invocation);
  }
}
