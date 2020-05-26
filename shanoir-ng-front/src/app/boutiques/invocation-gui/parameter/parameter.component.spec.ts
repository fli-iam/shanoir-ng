import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Parameter } from './parameter';

import { FormsModule, FormGroup, ReactiveFormsModule, FormBuilder, FormControl, Validators  } from '@angular/forms';
import { ParameterComponent } from './parameter.component';

import { Component, Input } from '@angular/core';
import { By } from '@angular/platform-browser';

import { ParameterGroupComponent } from '../parameter-group/parameter-group.component';
import { ParameterGroup } from '../parameter-group/parameter-group';

import { ParameterControlService }    from '../parameter-control.service';
import { ReplaceSpacePipe } from '../../../utils/pipes';
import { fakeDescriptor, idPrefix, namePrefix, descriptionPrefix, valuePrefix } from '../fake-descriptor';

describe('ParameterComponent', () => {
  let component: ParameterComponent;
  let fixture: ComponentFixture<ParameterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ParameterComponent ],
      imports: [ FormsModule, ReactiveFormsModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParameterComponent);
    component = fixture.componentInstance;
    let fakeId = 'fake_parameter_1';
    component.parameter = new Parameter({ id: fakeId, name: 'fake parameter', description: 'fake parameter descrition', optional: true, type: 'Flag', value: true, 'value-key': 'fake value-key' });
    let group = {};
    group[fakeId] = new FormControl(component.parameter.value);
    component.formGroup = new FormGroup(group);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create the proper gui when given a parameter and formGroup', () => {

    let service = new ParameterControlService();
    
    let formGroups = service.createFormGroupFromDescriptor(fakeDescriptor);
    let parameterGroups = service.parameterGroups;

    // Required group:

    let group0Id = '0'+idPrefix+'0';
    component.formGroup = (formGroups.controls['required'] as FormGroup).controls[group0Id] as FormGroup;

    // Parameter 0
    component.parameter = parameterGroups.required.get(group0Id).parameters[0];

    fixture.detectChanges();

    const nameDebugElement0 = fixture.debugElement.query(By.css('label'));
    expect(nameDebugElement0.nativeElement.textContent).toEqual(component.parameter.name);

    const inputDebugElement0 = fixture.debugElement.query(By.css('input'));
    expect(inputDebugElement0.nativeElement.type).toEqual('text');

    const selectDataDebugElement0 = fixture.debugElement.query(By.css('button.select-data'));
    expect(selectDataDebugElement0).toBeNull();

    const unsetDebugElement0 = fixture.debugElement.query(By.css('button.unset'));
    expect(unsetDebugElement0).toBeNull();

    // Parameter 1
    component.parameter = parameterGroups.required.get(group0Id).parameters[1];

    fixture.detectChanges();

    const nameDebugElement1 = fixture.debugElement.query(By.css('label'));
    expect(nameDebugElement1.nativeElement.textContent).toEqual(component.parameter.name);

    const inputDebugElement1 = fixture.debugElement.query(By.css('input'));
    expect(inputDebugElement1.nativeElement.type).toEqual('number');
    expect(inputDebugElement1.nativeElement.min).toEqual('0');
    expect(inputDebugElement1.nativeElement.max).toEqual('9');
    
    const selectDataDebugElement1 = fixture.debugElement.query(By.css('button.select-data'));
    expect(selectDataDebugElement1).toBeNull();

    const unsetDebugElement1 = fixture.debugElement.query(By.css('button.unset'));
    expect(unsetDebugElement1).toBeNull();

    // Optional group:

    let group1Id = '1'+idPrefix+'1';
    component.formGroup = (formGroups.controls['optional'] as FormGroup).controls[group1Id] as FormGroup;

    // Parameter 2
    component.parameter = parameterGroups.optional.get(group1Id).parameters[0];

    fixture.detectChanges();

    const nameDebugElement2 = fixture.debugElement.query(By.css('label'));
    expect(nameDebugElement2.nativeElement.textContent).toEqual(component.parameter.name);

    const inputDebugElement2 = fixture.debugElement.query(By.css('input'));
    expect(inputDebugElement2.nativeElement.type).toEqual('text');
    
    const selectDataDebugElement2 = fixture.debugElement.query(By.css('button.select-data'));
    expect(selectDataDebugElement2).not.toBeNull();

    const unsetDebugElement2 = fixture.debugElement.query(By.css('button.unset'));
    expect(unsetDebugElement2).not.toBeNull();

    // Parameter 3
    component.parameter = parameterGroups.optional.get(group1Id).parameters[1];

    fixture.detectChanges();

    const nameDebugElement3 = fixture.debugElement.query(By.css('label'));
    expect(nameDebugElement3.nativeElement.textContent).toEqual(component.parameter.name);

    const inputDebugElement3 = fixture.debugElement.query(By.css('input'));
    expect(inputDebugElement3.nativeElement.type).toEqual('checkbox');
    
    const selectDataDebugElement3 = fixture.debugElement.query(By.css('button.select-data'));
    expect(selectDataDebugElement3).toBeNull();

    const unsetDebugElement3 = fixture.debugElement.query(By.css('button.unset'));
    expect(unsetDebugElement3).not.toBeNull();

    // Parameter 4
    component.parameter = parameterGroups.optional.get(group1Id).parameters[2];

    fixture.detectChanges();

    const nameDebugElement4 = fixture.debugElement.query(By.css('label'));
    expect(nameDebugElement4.nativeElement.textContent).toEqual(component.parameter.name);

    const inputDebugElement4 = fixture.debugElement.query(By.css('input'));
    expect(inputDebugElement4.nativeElement.type).toEqual('number');
    
    const selectDataDebugElement4 = fixture.debugElement.query(By.css('button.select-data'));
    expect(selectDataDebugElement4).toBeNull();

    const unsetDebugElement4 = fixture.debugElement.query(By.css('button.unset'));
    expect(unsetDebugElement4).not.toBeNull();

    expect(inputDebugElement4.nativeElement.min).toEqual('0');
    expect(inputDebugElement4.nativeElement.max).toEqual('' + (10 - Parameter.epsilon));
    
    // Parameter 5
    component.parameter = parameterGroups.optional.get(group1Id).parameters[3];

    fixture.detectChanges();

    const nameDebugElement5 = fixture.debugElement.query(By.css('label'));
    expect(nameDebugElement5.nativeElement.textContent).toEqual(component.parameter.name);

    const inputDebugElement5 = fixture.debugElement.query(By.css('input'));
    expect(inputDebugElement5.nativeElement.type).toEqual('text');
    
    const selectDataDebugElement5 = fixture.debugElement.query(By.css('button.select-data'));
    expect(selectDataDebugElement5).toBeNull();

    const unsetDebugElement5 = fixture.debugElement.query(By.css('button.unset'));
    expect(unsetDebugElement5).not.toBeNull();
  });

});
