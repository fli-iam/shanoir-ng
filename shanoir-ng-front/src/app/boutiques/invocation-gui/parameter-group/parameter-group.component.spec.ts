import { Component, Input } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { ParameterGroupComponent } from './parameter-group.component';
import { ParameterGroup } from './parameter-group';
import { Parameter } from '../parameter/parameter';
import { ParameterComponent } from '../parameter/parameter.component';
import { FormsModule, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { ParameterControlService }    from '../parameter-control.service';
import { ReplaceSpacePipe } from '../../../utils/pipes';
import { fakeDescriptor, idPrefix, namePrefix, descriptionPrefix, valuePrefix } from '../fake-descriptor';

@Component({ selector: 'parameter', template: '', providers: [{ provide: ParameterComponent, useClass: ParameterStubComponent }] })
class ParameterStubComponent {
  @Input() parameter: Parameter<any> = new Parameter<any>();
  @Input() formGroup: any = null
}

describe('ParameterGroupComponent', () => {
  let component: ParameterGroupComponent;
  let fixture: ComponentFixture<ParameterGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ParameterGroupComponent, ParameterStubComponent ],
      imports: [ FormsModule, ReactiveFormsModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParameterGroupComponent);
    component = fixture.componentInstance;
    component.parameterGroup = new ParameterGroup({ id: 'fake_group', name: 'fake group', description: 'fake group description', members: [] });
    component.parameterGroup.members = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create all parameters when given a non exclusive formGroup and parameterGroup', () => {

    let service = new ParameterControlService();
    let parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };
    let formGroups = service.createFormGroupFromDescriptor(fakeDescriptor, parameterGroups);
    
    let group0Id = '0'+idPrefix+'0';
    component.formGroup = (formGroups.controls['required'] as FormGroup).controls[group0Id] as FormGroup;
    component.parameterGroup = parameterGroups.required.get(group0Id);

    // Check that GUI is present
    fixture.detectChanges();

    const parameterComponents = fixture.debugElement.queryAll(By.directive(ParameterComponent));
    expect(parameterComponents.length).toEqual(2);

    expect(parameterComponents[0].componentInstance.parameter).toBe(component.parameterGroup.parameters[0]);
    expect(parameterComponents[1].componentInstance.parameter).toBe(component.parameterGroup.parameters[1]);

    expect(parameterComponents[0].componentInstance.formGroup).toBe(component.formGroup);
    expect(parameterComponents[1].componentInstance.formGroup).toBe(component.formGroup);

    // There should be no select
    const selectDebugElement = fixture.debugElement.query(By.css('select'));
    expect(selectDebugElement).toBeNull();
  });

  it('should create one parameter when given an exclusive formGroup and parameterGroup', () => {

    let service = new ParameterControlService();
    let parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };
    let formGroups = service.createFormGroupFromDescriptor(fakeDescriptor, parameterGroups);
    
    let group1Id = '1'+idPrefix+'1';
    component.formGroup = (formGroups.controls['optional'] as FormGroup).controls[group1Id] as FormGroup;
    component.parameterGroup = parameterGroups.optional.get(group1Id);

    // Check that GUI is present
    fixture.detectChanges();

    const parameterComponents1 = fixture.debugElement.queryAll(By.directive(ParameterComponent));
    expect(parameterComponents1.length).toEqual(0);

    component.selectedParameterId = idPrefix + 2;
    fixture.detectChanges();

    const parameterComponents2 = fixture.debugElement.queryAll(By.directive(ParameterComponent));
    expect(parameterComponents2.length).toEqual(1);

    expect(parameterComponents2[0].componentInstance.parameter).toBe(component.parameterGroup.parameters[0]);

    component.selectedParameterId = idPrefix + 3;
    fixture.detectChanges();

    const parameterComponents3 = fixture.debugElement.queryAll(By.directive(ParameterComponent));
    expect(parameterComponents3.length).toEqual(1);

    expect(parameterComponents3[0].componentInstance.parameter).toBe(component.parameterGroup.parameters[1]);
    expect(parameterComponents3[0].componentInstance.formGroup).toBe(component.formGroup);

    // There should be no select
    const selectDebugElement = fixture.debugElement.query(By.css('select'));
    expect(selectDebugElement).not.toBeNull();
    const optionDebugElements = fixture.debugElement.queryAll(By.css('option'));
    expect(optionDebugElements.length).toEqual(4);
  });

});
