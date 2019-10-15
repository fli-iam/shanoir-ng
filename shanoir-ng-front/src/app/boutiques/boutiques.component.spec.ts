import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { TestBed, ComponentFixture, async } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { click } from './testing';

import { BoutiquesComponent } from './boutiques.component';
import { SearchToolsComponent } from './search-tools/search-tools.component';
import { InvocationComponent } from './invocation/invocation.component';
import { ExecutionComponent} from './execution/execution.component';
import { ToolInfo } from './testing/tool.model';

import { By } from '@angular/platform-browser';

@Component({ selector: 'search-tools', template: '', providers: [{ provide: SearchToolsComponent, useClass: SearchToolsStubComponent }] })
class SearchToolsStubComponent {
  @Output() toolSelected = new EventEmitter<ToolInfo>();
}

@Component({ selector: 'invocation', template: '', providers: [{ provide: InvocationComponent, useClass: InvocationStubComponent }] })
class InvocationStubComponent {
  onToolSelected() {}
}

@Component({ selector: 'execution', template: '', providers: [{ provide: ExecutionComponent, useClass: ExecutionStubComponent }] })
class ExecutionStubComponent {
  onToolSelected() {}
}

describe('AppComponent', () => {
  
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [
        BoutiquesComponent,
        SearchToolsStubComponent,
        InvocationStubComponent,
        ExecutionStubComponent
      ],
    }).compileComponents();
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(BoutiquesComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should call onToolSelected on InvocationComponent and ExecutionComponent when SearchToolsComponent emits toolSelected', () => {
    const fixture = TestBed.createComponent(BoutiquesComponent);
    
    const app: BoutiquesComponent = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    
    spyOn(app['executionComponent'], 'onToolSelected');
    spyOn(app['invocationComponent'], 'onToolSelected');

    const searchToolsDebugElement = fixture.debugElement.query(By.directive(SearchToolsComponent));
    const searchToolsComponent = searchToolsDebugElement.componentInstance;
    
    searchToolsComponent.toolSelected.subscribe(toolInfo => {
      expect(toolInfo.name).toBe('fakeTool');
      expect(app.executionComponent.onToolSelected).toHaveBeenCalledTimes(1);
      expect(app.invocationComponent.onToolSelected).toHaveBeenCalledTimes(1);
    });

    searchToolsComponent.toolSelected.emit(new ToolInfo());
  });

});
