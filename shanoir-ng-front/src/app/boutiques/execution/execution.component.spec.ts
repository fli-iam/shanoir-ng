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

class RxStompStubService {
  watch() {
    return new Observable();
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
      providers: [ { provide: ToolService, useClass: FakeToolService }, { provide: RxStompService, useClass: RxStompStubService } ]
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

  it('should display the execution output', () => {
    const generatedOutputTextArea: HTMLTextAreaElement = fixture.nativeElement.querySelector('#output');
    expect(generatedOutputTextArea.textContent).toBe('');
    expect(component.output).toBe(null);
    component.output = 'test output';
    fixture.detectChanges();
    expect(generatedOutputTextArea.textContent).toBe(component.output);
  });

  it('should display the generated command when invocation changes', fakeAsync(() => {
    expect(component.generatedCommand).toBe(null, 'should be null');
    expect(fixture.nativeElement.querySelector('#generatedCommand').textContent).toBe('', 'should be empty');
    
    const invocation = 'fake invocation';
    component.tool = new FakeToolInfo() as ToolInfo;
    component.onInvocationChanged(invocation);

    tick();
    fixture.detectChanges();

    const expectedCommand = getToolService().getFakeCommand(invocation);
    expect(component.generatedCommand).toBe(expectedCommand, 'show generated command');
    expect(fixture.nativeElement.querySelector('#generatedCommand').textContent).toBe(expectedCommand, 'show generated command');
  }));

  it('should execute and display the command output when clicking the execute button', fakeAsync(() => {
    expect(component.output).toBe(null, 'should be null');
    expect(fixture.nativeElement.querySelector('#output').textContent).toBe('', 'should be empty');

    const invocation = 'fake invocation';
    component.tool = new FakeToolInfo() as ToolInfo
    component.invocation = invocation;
    click(fixture.nativeElement.querySelector('#execute'));

    tick();
    fixture.detectChanges();

    const expectedOutput = getToolService().getFakeOutput(invocation);
    expect(component.output).toBe(expectedOutput, 'show resulting output');
    expect(fixture.nativeElement.querySelector('#output').textContent).toBe(expectedOutput, 'show resulting output');
  }));

});
