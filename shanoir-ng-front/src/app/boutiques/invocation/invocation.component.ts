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
    // Invocation subject provides new values when invocationValue changes, i.e. when user types in the invocation textarea
    this.invocationSubject.pipe(
      // wait 1 second after each keystroke before considering the term
      debounceTime(1000),

      // ignore new term if same as previous term
      distinctUntilChanged()
    ).subscribe((invocationValue: string)=> { 
      this.updateInvocationFromString(invocationValue);
    });

    // Get the tool descriptor from the server
    this.toolService.getDescriptor(this.toolId).then((descriptor)=> this.descriptor = descriptor; );

    // Get the invocation which was stored in session storage
    this.invocation = this.toolService.data.invocation;

    if(this.invocation == null) {
      // If there was no invocation: generate a default one
      this.toolService.getDefaultInvocation(this.toolId).then((invocation)=> { 
        // If invocation has changed before the default one was retrieved from server: ignore default one
        if(this.toolService.data.invocation) {
          return;
        }
        this.onInvocationChanged(invocation);
        this.invocationGUI.setInvocation(invocation);
      });
    } else {
      // If there was an invocation stored: set invocation value (update textarea)
      this.invocationValue = this.getInvocationValue();
    }
  }

  openInvocation(event: any) {
    // On open invocation: read given file and update invocation
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.updateInvocationFromString(e.target.result);
      this.invocationValue = this.getInvocationValue();
      this.invocationGUI.setInvocation(this.invocation);
    };
    reader.readAsText(event.target.files[0]);
  }

  saveInvocation(event: any) {
    // On save invocation: create invocation blob & save file
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
    // Update and store invocation, emit invocationChanged
    this.invocation = invocation;
    this.storeInvocation();
    this.invocationChanged.emit(this.invocation);
  }

  storeInvocation() {
    this.toolService.saveSession({ invocation: this.invocation});
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
