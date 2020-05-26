import { tick, async, fakeAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';

import { ToolService as FakeToolService } from '../testing/tool.service';
import { ToolInfo as FakeToolInfo } from '../testing/tool.model';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';
import { click } from '../testing';
import { By } from '@angular/platform-browser';
import { ExecutionComponent } from './execution.component';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Observable } from 'rxjs/Observable';
import { BreadcrumbsService as FakeBreadcrumbsService } from '../testing/breadcrumbs.service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

class RxStompStubService {
  watch() {
    return new Observable();
  }
}

class FakeMsgBoxService {
  log(message) {
    console.log(message);
  }
}

describe('ExecutionComponent', () => {
  let component: ExecutionComponent;
  let fixture: ComponentFixture<ExecutionComponent>;

  let getToolService = ()=> {
    return <any>component['toolService'] as FakeToolService;
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExecutionComponent ],
      providers: [ 
      { provide: ToolService, useClass: FakeToolService }, 
      { provide: RxStompService, useClass: RxStompStubService }, 
      { provide: BreadcrumbsService, useClass: FakeBreadcrumbsService }, 
      { provide: MsgBoxService, useClass: FakeMsgBoxService }, DomSanitizer ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the generated command', () => {
    const generatedCommandTextArea: HTMLTextAreaElement = fixture.nativeElement.querySelector('#generatedCommand');
    expect(generatedCommandTextArea.textContent).toBe('');
    expect(component.generatedCommand).toBe(null);
    component.generatedCommand = 'test command';
    fixture.detectChanges();
    expect(generatedCommandTextArea.textContent).toBe(component.generatedCommand);
  });

  it('should display the generated command when invocation changes', fakeAsync(() => {
    fixture.detectChanges();

    expect(component.generatedCommand).toBe(null, 'should be null');
    expect(fixture.nativeElement.querySelector('#generatedCommand').textContent).toBe('', 'should be empty');
    
    const invocation = 'fake invocation';
    component.toolId = 'fake tool id';
    component.onInvocationChanged(invocation);

    tick(1000);
    fixture.detectChanges();

    const expectedCommand = getToolService().getFakeCommand(invocation);
    expect(component.generatedCommand).toBe(expectedCommand, 'show generated command');
    expect(fixture.nativeElement.querySelector('#generatedCommand').textContent).toBe(expectedCommand, 'show generated command');
  }));

  it('should execute and display the command output when clicking the execute button', fakeAsync(() => {
    expect(component.outputLines).toEqual([], 'should be empty');

    const invocation = 'fake invocation';
    component.toolId = 'fake tool id';
    component.invocation = invocation;
    // click(fixture.nativeElement.querySelector('#execute'));

    // tick();
    // fixture.detectChanges();

    // const expectedOutput = getToolService().getFakeExecutionOutput();
    // expect(component.outputLines[0]).toBe(expectedOutput.input[0], 'show resulting output');
  }));

});
