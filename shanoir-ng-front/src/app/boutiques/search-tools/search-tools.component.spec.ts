import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';

import { SearchToolsComponent } from './search-tools.component';
import { ToolListComponent } from '../tool-list/tool-list.component';
import { ToolService as FakeToolService } from '../testing/tool.service';
import { ToolInfo as FakeToolInfo } from '../testing/tool.model';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';
import { By } from '@angular/platform-browser';

@Component({ selector: 'tool-list', template: '', providers: [{ provide: ToolListComponent, useClass: ToolListStubComponent }] })
class ToolListStubComponent {
  @Output() toolSelected = new EventEmitter<ToolInfo>();
}

describe('SearchToolsComponent', () => {
  let component: SearchToolsComponent;
  let fixture: ComponentFixture<SearchToolsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchToolsComponent, ToolListStubComponent ],
      providers: [ { provide: ToolService, useClass: FakeToolService } ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchToolsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set selectedTool and call onToolSelected when tool-list emits toolSelected', () => {
    
    const toolListDebugElement = fixture.debugElement.query(By.directive(ToolListComponent));
    const toolListComponent = toolListDebugElement.componentInstance;

    const fakeToolInfo = new FakeToolInfo();

    spyOn(component.toolSelected, 'emit');

    toolListComponent.toolSelected.subscribe(toolInfo => {
      expect(toolInfo.name).toBe('fakeTool');
      expect(component.selectedTool).toBe(fakeToolInfo as ToolInfo);
      expect(component.toolSelected.emit).toHaveBeenCalledWith(toolInfo);
    });

    toolListComponent.toolSelected.emit(fakeToolInfo);
  });


});
