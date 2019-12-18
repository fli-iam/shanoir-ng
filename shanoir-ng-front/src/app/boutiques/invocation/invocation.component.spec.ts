import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { tick, TestBed, ComponentFixture, async, fakeAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { click } from '../testing';
import { ToolService as FakeToolService } from '../testing/tool.service';
import { ToolInfo as FakeToolInfo } from '../testing/tool.model';
import { ToolInfo } from '../tool.model';
import { ToolService } from '../tool.service';
import { By } from '@angular/platform-browser';
import { InvocationComponent } from './invocation.component';
import { InvocationGuiComponent } from '../invocation-gui/invocation-gui.component';
import { ToolDescriptorInfoComponent } from '../tool-descriptor-info/tool-descriptor-info.component';
import { BreadcrumbsService as FakeBreadcrumbsService } from '../testing/breadcrumbs.service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';

@Component({ selector: 'invocation-gui', template: '', providers: [{ provide: InvocationGuiComponent, useClass: InvocationGuiStubComponent }] })
class InvocationGuiStubComponent {
  @Input() descriptor: any = null

  setInvocation(invocation: any) { }
}

@Component({ selector: 'tool-descriptor-info', template: '', providers: [{ provide: ToolDescriptorInfoComponent, useClass: ToolDescriptorInfoStubComponent }] })
class ToolDescriptorInfoStubComponent implements OnInit {
  @Input() descriptor: any = null
  ngOnInit() {
  }
}

describe('InvocationComponent', () => {
  let component: InvocationComponent;
  let fixture: ComponentFixture<InvocationComponent>;

  let getToolService = ()=> {
    return <any>component['toolService'] as FakeToolService;
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule ],
      declarations: [ InvocationComponent, ToolDescriptorInfoStubComponent, InvocationGuiStubComponent ],
      providers: [ { provide: ToolService, useClass: FakeToolService }, { provide: BreadcrumbsService, useClass: FakeBreadcrumbsService } ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InvocationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set invocation.descriptor, invocation.invocation when select tool', fakeAsync(() => {    
    const tool = new FakeToolInfo() as ToolInfo;

    component.ngOnInit();
    tick();
    fixture.detectChanges();
    
    const expectedDescriptor = getToolService().getFakeDescriptor();
    const expectedInvocation  = getToolService().getFakeInvocation();

    expect(component.descriptor).toEqual(expectedDescriptor);
    expect(component.invocation).toEqual(expectedInvocation);

    let toolDescriptorInfoComponent = fixture.debugElement.query(By.directive(ToolDescriptorInfoStubComponent)).componentInstance;
    let invocationGuiComponent = fixture.debugElement.query(By.directive(InvocationGuiStubComponent)).componentInstance;

    expect(toolDescriptorInfoComponent.descriptor).toEqual(expectedDescriptor);
    expect(invocationGuiComponent.descriptor).toEqual(expectedDescriptor);
  }));

});
