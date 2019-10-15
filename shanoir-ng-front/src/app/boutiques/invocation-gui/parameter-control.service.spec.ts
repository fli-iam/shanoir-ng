import { TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ParameterControlService } from './parameter-control.service';
import { Parameter, ParameterDescription } from './parameter/parameter';
import { ParameterGroup, ParameterGroupDescription } from './parameter-group/parameter-group';
import { Descriptor } from './descriptor';
import { fakeDescriptor, idPrefix, namePrefix, descriptionPrefix, valuePrefix } from './fake-descriptor';

describe('ParameterControlService', () => {
  let service: ParameterControlService = null;

  beforeEach(() => {
    service = new ParameterControlService();
  });

  it('should create FromGroups from parameters', ()=> {
    let parameters: Parameter<any>[] = [];
    
    let idPrefix = 'fake id ';
    let valuePrefix = 'fake value ';

    for(let i of [0, 1]) {
      let parameterDescription = new ParameterDescription();
      parameterDescription.id = idPrefix + i;
      parameterDescription.value = valuePrefix + i;
      parameters.push(new Parameter<any>(parameterDescription));
    }

    let formGroups = service.toFormGroup(parameters);

    for(let i of [0, 1]) {
      expect(formGroups.get(idPrefix + i).value).toBe(valuePrefix + i);
    }
  });

  it('should create form groups from descriptors', ()=> {
    let parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };
    let formGroup = service.createFormGroupFromDescriptor(fakeDescriptor, parameterGroups);
    expect(formGroup.controls['required']).toBeDefined();
    
    // group 0:
    let requiredFormGroups = formGroup.controls['required'] as FormGroup;

    let requiredFormGroup0 = requiredFormGroups.controls['0' + idPrefix + 0] as FormGroup;
    expect(requiredFormGroup0).toBeDefined();
    expect(requiredFormGroup0.controls[idPrefix + 0]).toBeDefined();
    expect(requiredFormGroup0.controls[idPrefix + 1]).toBeDefined();
    expect(requiredFormGroup0.controls[idPrefix + 0].value).toBe(valuePrefix + 0);
    expect(requiredFormGroup0.controls[idPrefix + 1].value).toBe(valuePrefix + 1);

    let parameterGroup0 = parameterGroups['required'].get('0' + idPrefix + 0);
    expect(parameterGroup0).toBeDefined();
    expect(parameterGroup0.parameters[0].type).toBe('String');
    expect(parameterGroup0.parameters[0].getInputType()).toBe('text');
    expect(parameterGroup0.parameters[0].value).toBe(valuePrefix + 0);
    expect(parameterGroup0.parameters[1].type).toBe('Number');
    expect(parameterGroup0.parameters[1].getInputType()).toBe('number');
    expect(parameterGroup0.parameters[1].value).toBe(valuePrefix + 1);

    // group 1:
    let optionalFormGroups = formGroup.controls['optional'] as FormGroup;

    let optionalFormGroup1 = optionalFormGroups.controls['1' + idPrefix + 1] as FormGroup;
    expect(optionalFormGroup1).toBeDefined();
    expect(optionalFormGroup1.controls[idPrefix + 2]).toBeDefined();
    expect(optionalFormGroup1.controls[idPrefix + 3]).toBeDefined();
    expect(optionalFormGroup1.controls[idPrefix + 2].value).toBe(valuePrefix + 2);
    expect(optionalFormGroup1.controls[idPrefix + 3].value).toBe(valuePrefix + 3);

    let parameterGroup1 = parameterGroups['optional'].get('1' + idPrefix + 1);
    expect(parameterGroup1).toBeDefined();
    expect(parameterGroup1.optional).toBe(true);
    expect(parameterGroup1.exclusive).toBe(true);
    expect(parameterGroup1.parameters[0].type).toBe('File');
    expect(parameterGroup1.parameters[0].getInputType()).toBe('text');
    expect(parameterGroup1.parameters[0].value).toBe(valuePrefix + 2);
    expect(parameterGroup1.parameters[1].type).toBe('Flag');
    expect(parameterGroup1.parameters[1].getInputType()).toBe('checkbox');
    expect(parameterGroup1.parameters[1].value).toBe(valuePrefix + 3);
  });
  
  it('should generate an empty invocation from form groups if form is pristine', ()=> {
    let parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };
    let formGroup = service.createFormGroupFromDescriptor(fakeDescriptor, parameterGroups);
    let invocation = service.generateInvocation(formGroup);
    expect(invocation).toEqual({});
  });
  
  it('should generate the proper invocation from form groups when form is changed', ()=> {
    let parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };
    let formGroup = service.createFormGroupFromDescriptor(fakeDescriptor, parameterGroups);
    let requiredFormGroups = formGroup.controls['required'] as FormGroup;
    let requiredFormGroup0 = requiredFormGroups.controls['0' + idPrefix + 0] as FormGroup;
    requiredFormGroup0.controls[idPrefix + 0].markAsDirty();
    requiredFormGroup0.controls[idPrefix + 0].setValue('new fake value 0');
    requiredFormGroup0.controls[idPrefix + 1].markAsDirty();
    requiredFormGroup0.controls[idPrefix + 1].setValue(1);

    let optionalFormGroups = formGroup.controls['optional'] as FormGroup;
    let optionalFormGroup1 = optionalFormGroups.controls['1' + idPrefix + 1] as FormGroup;
    optionalFormGroup1.controls[idPrefix + 2].markAsDirty();
    optionalFormGroup1.controls[idPrefix + 2].setValue('new fake value 2');
    optionalFormGroup1.controls[idPrefix + 3].markAsDirty();
    optionalFormGroup1.controls[idPrefix + 3].setValue(true);

    let invocation = service.generateInvocation(formGroup);

    expect(invocation[idPrefix + 0]).toBe('new fake value 0');
    expect(invocation[idPrefix + 1]).toBe(1);

    expect(invocation[idPrefix + 2]).toBe('new fake value 2');
    expect(invocation[idPrefix + 3]).toBe(true);
  });

});


