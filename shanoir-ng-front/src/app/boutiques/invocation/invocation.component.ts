import { Component, OnInit, Input, Output, EventEmitter, ElementRef, ViewChild } from '@angular/core';
import { ToolDescriptorInfoComponent } from '../tool-descriptor-info/tool-descriptor-info.component';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { InvocationGuiComponent } from '../invocation-gui/invocation-gui.component';
import { Observable, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { saveAs } from 'file-saver';

@Component({
  selector: 'invocation',
  templateUrl: './invocation.component.html',
  styleUrls: ['./invocation.component.css']
})
export class InvocationComponent implements OnInit {
  
  @Input() toolId: string;
  
  @Output() invocationChanged = new EventEmitter<any>();

  @ViewChild(InvocationGuiComponent, { static: false }) invocationGUI: InvocationGuiComponent;


  descriptor: any = null
  
  invocation: any = null
  invocationValue: string = null
  invocationChangedTimeoutID: any = null

  private invocationSubject = new Subject<string>();

  constructor(private toolService: ToolService, private breadcrumbsService: BreadcrumbsService) {
  }

  ngOnInit(): void {
    this.invocationSubject.pipe(
      // wait 1 second after each keystroke before considering the term
      debounceTime(1000),

      // ignore new term if same as previous term
      distinctUntilChanged()
    ).subscribe((invocationValue: string)=> { 
      this.updateInvocationFromString(invocationValue);
    });

    this.toolService.getDescriptor(this.toolId).then((descriptor)=> this.descriptor = descriptor);
    
    if(this.breadcrumbsService.currentStep.data.boutiquesInvocation == null) {
      this.toolService.getInvocation(this.toolId).then((invocation)=> { 
        this.onInvocationChanged(invocation);
        this.invocationGUI.setInvocation(this.invocation);
      });
    }
  }

  openInvocation(event: any) {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.updateInvocationFromString(e.target.result);
      this.invocationValue = this.getInvocationValue();
      this.invocationGUI.setInvocation(this.invocation);
    };
    reader.readAsText(event.target.files[0]);
  }

  saveInvocation(event: any) {
    let blob = new Blob([this.getInvocationValue()], {type: "text/plain;charset=utf-8"});
    saveAs(blob, "invocation.json");
  }

  updateInvocationFromString(invocationValue: string) {
    try{
      this.updateInvocation(JSON.parse(invocationValue));
    } catch(e) {
      console.log('error occored while you were typing the JSON');
      console.log(e);
    };
  }

  updateInvocation(invocation: string) {
    this.invocation = invocation;
    this.storeInvocation();
    this.invocationChanged.emit(this.invocation);
  }

  storeInvocation() {
    // Set the boutiques data in all boutiques steps (there might be multiple boutiques steps in navigation history) 
    for(let step of this.breadcrumbsService.steps) {
      if(step.data.boutiques) {
        step.data.boutiquesInvocation = this.invocation;
      }
    }
    this.breadcrumbsService.saveSession();
  }

  onInvocationChanged(invocation) {
    this.updateInvocation(invocation);
    this.invocationValue = this.getInvocationValue();
  }

  invocationValueChanged(event: any) {
    this.invocationSubject.next(event.target.value);
  }

  getInvocationValue() {
    return this.invocation ? JSON.stringify(this.invocation, null, 2) : '';
  }

}
