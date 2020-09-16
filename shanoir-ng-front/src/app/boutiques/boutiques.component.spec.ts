import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { TestBed, ComponentFixture, async } from '@angular/core/testing';
// import { Router } from '@angular/router';
import { Router } from '../breadcrumbs/router';
import { RouterTestingModule } from '@angular/router/testing';
import { BreadcrumbsService as FakeBreadcrumbsService } from './testing/breadcrumbs.service';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { FormsModule } from '@angular/forms';
import { click } from './testing';

import { BoutiquesComponent } from './boutiques.component';
import { SearchToolsComponent } from './search-tools/search-tools.component';
import { ToolInfo } from './testing/tool.model';

import { By } from '@angular/platform-browser';

@Component({ selector: 'search-tools', template: '', providers: [{ provide: SearchToolsComponent, useClass: SearchToolsStubComponent }] })
class SearchToolsStubComponent {
  @Output() toolSelected = new EventEmitter<ToolInfo>();
}

describe('BoutiquesComponent', () => {

  let routerStub;

  beforeEach(async(() => {
    routerStub = {
      navigate: jasmine.createSpy('navigate'),
    };
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([]),
      ],
      declarations: [
        BoutiquesComponent,
        SearchToolsStubComponent
      ],
      providers: [
        { provide: Router, useValue: routerStub } ,
        { provide: BreadcrumbsService, useClass: FakeBreadcrumbsService }
      ]
    }).compileComponents();
  }));

  it('should create boutiques', () => {
    const fixture = TestBed.createComponent(BoutiquesComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should navigate to boutiques + tool.id when SearchToolsComponent emits toolSelected', () => {
    const fixture = TestBed.createComponent(BoutiquesComponent);
    
    const app: BoutiquesComponent = fixture.debugElement.componentInstance;
    fixture.detectChanges();
    
    const searchToolsDebugElement = fixture.debugElement.query(By.directive(SearchToolsComponent));
    const searchToolsComponent = searchToolsDebugElement.componentInstance;

    searchToolsComponent.toolSelected.subscribe(toolInfo => {
      expect(toolInfo.name).toBe('fakeTool');
      expect(routerStub.navigate).toHaveBeenCalledWith(['boutiques/' + toolInfo.id]);
    });

    searchToolsComponent.toolSelected.emit(new ToolInfo());
  });

});
