import { Component, Input } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router } from '../../breadcrumbs/router';
import { Router as AngularRouter } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { InvocationGuiComponent } from './invocation-gui.component';
import { ToolService as FakeToolService }    from '../testing/tool.service';
import { ToolService }    from '../tool.service';
import { BreadcrumbsService as FakeBreadcrumbsService } from '../testing/breadcrumbs.service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { ParameterControlService }    from './parameter-control.service';
import { ParameterGroupComponent }    from './parameter-group/parameter-group.component';
import { ParameterGroup }    from './parameter-group/parameter-group';
      
import { ReactiveFormsModule, FormGroup, FormControl } from '@angular/forms';
import { ReplaceSpacePipe } from '../../utils/pipes';
import { fakeDescriptor, idPrefix, namePrefix, descriptionPrefix, valuePrefix } from './fake-descriptor';

@Component({ selector: 'parameter-group', template: '', providers: [{ provide: ParameterGroupComponent, useClass: ParameterGroupStubComponent }] })
class ParameterGroupStubComponent {
  @Input() parameterGroup: any = null
  @Input() formGroup: any = null
}

describe('InvocationGuiComponent', () => {
  let component: InvocationGuiComponent;
  let fixture: ComponentFixture<InvocationGuiComponent>;
  let routerStub;
  beforeEach(async(() => {
    routerStub = {
      navigate: jasmine.createSpy('navigate'),
    };
    TestBed.configureTestingModule({
      providers: [ 
        ParameterControlService,
        { provide: Router, useValue: routerStub } ,
        { provide: AngularRouter, useValue: AngularRouter } ,
        { provide: ToolService, useClass: FakeToolService }, 
        { provide: BreadcrumbsService, useClass: FakeBreadcrumbsService }
      ],
      declarations: [ InvocationGuiComponent, ParameterGroupStubComponent, ReplaceSpacePipe ],
      imports: [ ReactiveFormsModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InvocationGuiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be empty if form is null', () => {
    expect(component.form).toBeNull();
    expect(fixture.nativeElement.children.length).toEqual(0);
  });

  it('should contain invocation GUI when given a form and parameterGroups', () => {
    
    let service = new ParameterControlService();
    component.parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };

    component.form = service.createFormGroupFromDescriptor(fakeDescriptor);
    component.parameterGroups = service.parameterGroups;

    // Check that GUI is present
    fixture.detectChanges();

    const parameterGroupComponents = fixture.debugElement.queryAll(By.directive(ParameterGroupComponent));

    expect(parameterGroupComponents.length).toEqual(2);
    expect(parameterGroupComponents[0].componentInstance.formGroup).toBe((component.form.controls.required as FormGroup).controls['0' + idPrefix + 0]);
    expect(parameterGroupComponents[1].componentInstance.formGroup).toBe((component.form.controls.optional as FormGroup).controls['1' + idPrefix + 1]);

    expect(parameterGroupComponents[0].componentInstance.parameterGroup).toBe(component.parameterGroups.required.get('0' + idPrefix + 0));
    expect(parameterGroupComponents[1].componentInstance.parameterGroup).toBe(component.parameterGroups.optional.get('1' + idPrefix + 1));

    // // Also test child elements, although this should be retested in parameter and parameter-group specs 

    // const parameter0DebugElement = fixture.debugElement.query(By.css('input[type="text"]'));
    // const parameter1DebugElement = fixture.debugElement.query(By.css('input[type="number"]'));

    // const parameterSelectDebugElement = fixture.debugElement.query(By.css('select'));
    // const parameter2UndefinedDebugElement = fixture.debugElement.query(By.css('input[type="text"]'));
    // // const parameter21DebugElement = fixture.debugElement.query(By.css('.invocation-gui-parameter.parameter-file button.select-data'));
    // // const parameter22DebugElement = fixture.debugElement.query(By.css('.invocation-gui-parameter.parameter-file button.unset'));
    // // const parameter3DebugElement = fixture.debugElement.query(By.css('input[type="checkbox"]'));

    // expect(parameter0DebugElement).toBeDefined();
    // expect(parameter1DebugElement).toBeDefined();

    // expect(parameterSelectDebugElement).toBeDefined();
    // expect(parameter2UndefinedDebugElement == null).toBeTruthy();

    // parameterSelectDebugElement.nativeElement.value = idPrefix + 2
    // parameterSelectDebugElement.nativeElement.dispatchEvent(new Event('change'));

    // expect(parameter21DebugElement.nativeElement.textContent).toBe('Select data');
    // expect(parameter22DebugElement.nativeElement.textContent).toBe('Unset');
    // expect(parameter3DebugElement).toBeDefined();

    // let requiredFormGroups = formGroup.controls['required'] as FormGroup;

    // let requiredFormGroup0 = requiredFormGroups.controls['0' + idPrefix + 0] as FormGroup;
    // expect(requiredFormGroup0).toBeDefined();
    // expect(requiredFormGroup0.controls[idPrefix + 0]).toBeDefined();
    // expect(requiredFormGroup0.controls[idPrefix + 1]).toBeDefined();
    // expect(requiredFormGroup0.controls[idPrefix + 0].value).toBe(valuePrefix + 0);
    // expect(requiredFormGroup0.controls[idPrefix + 1].value).toBe(valuePrefix + 1);

    // let parameterGroup0 = parameterGroups['required'].get('0' + idPrefix + 0);
    // expect(parameterGroup0).toBeDefined();
    // expect(parameterGroup0.parameters[0].type).toBe('String');
    // expect(parameterGroup0.parameters[0].inputType).toBe('text');
    // expect(parameterGroup0.parameters[0].value).toBe(valuePrefix + 0);
    // expect(parameterGroup0.parameters[1].type).toBe('Number');
    // expect(parameterGroup0.parameters[1].inputType).toBe('number');
    // expect(parameterGroup0.parameters[1].value).toBe(valuePrefix + 1);

    // // group 1:
    // let optionalFormGroups = formGroup.controls['optional'] as FormGroup;

    // let optionalFormGroup1 = optionalFormGroups.controls['1' + idPrefix + 1] as FormGroup;
    // expect(optionalFormGroup1).toBeDefined();
    // expect(optionalFormGroup1.controls[idPrefix + 2]).toBeDefined();
    // expect(optionalFormGroup1.controls[idPrefix + 3]).toBeDefined();
    // expect(optionalFormGroup1.controls[idPrefix + 2].value).toBe(valuePrefix + 2);
    // expect(optionalFormGroup1.controls[idPrefix + 3].value).toBe(valuePrefix + 3);

    // let parameterGroup1 = parameterGroups['optional'].get('1' + idPrefix + 1);
    // expect(parameterGroup1).toBeDefined();
    // expect(parameterGroup1.optional).toBe(true);
    // expect(parameterGroup1.exclusive).toBe(true);
    // expect(parameterGroup1.parameters[0].type).toBe('File');
    // expect(parameterGroup1.parameters[0].inputType).toBe('text');
    // expect(parameterGroup1.parameters[0].value).toBe(valuePrefix + 2);
    // expect(parameterGroup1.parameters[1].type).toBe('Flag');
    // expect(parameterGroup1.parameters[1].inputType).toBe('checkbox');
    // expect(parameterGroup1.parameters[1].value).toBe(valuePrefix + 3);

    // Check that when modifying GUI, onChange is called

  });

});
