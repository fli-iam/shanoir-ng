import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { Parameter, ParameterDescription, StringParameter, FileParameter, NumberParameter } from './parameter/parameter';
import { ParameterGroup, ParameterGroupDescription } from './parameter-group/parameter-group';
import { Descriptor, OutputFile } from './descriptor';
import { Subject } from 'rxjs/Subject';

type ParameterGroups = {
  required: Map<string, ParameterGroup>
  optional: Map<string, ParameterGroup>
};

// @Injectable({ providedIn: 'root' })
@Injectable()
export class ParameterControlService {

  idToParameter: Map<string, Parameter<any>> = new Map();
  parameterGroups: { required: Map<string, ParameterGroup>, optional: Map<string, ParameterGroup> } = null;

  readonly defaultGroupId = 'boutiques_gui_default_group';
  private selectGroupSource = new Subject<void>();
  public selectGroupChange = this.selectGroupSource.asObservable();

  constructor() { }

  toFormGroup(parameters: Parameter<any>[] ) {
    // Create from groups from parameters
    let group: any = {};
    parameters.forEach(parameter => {
      group[parameter.id] = new FormControl(parameter.value || '');
    });
    return new FormGroup(group);
  }

  createIdToParameterDescriptionMap(inputs: ParameterDescription<any>[], ungroupedParameterIds: Set<string>, idToParameterDescription: Map<string, any>) {
    // Add all inputs in idToParameterDescription and ungroupedParameterIds
    for(let input of inputs) {
      idToParameterDescription.set(input.id, input);
      ungroupedParameterIds.add(input.id);
    }
  }

  createGroupList(groups: ParameterGroupDescription[], ungroupedParameterIds: Set<string>) {
    if(!groups) {
      return [];
    }
    // Create group list and remove grouped inputs from ungroupedParameterIds
    let groupList = [];
    for(let group of groups) {
      for(let member of group.members) {
        ungroupedParameterIds.delete(member);
      }
      groupList.push(group);
    }
    return groupList;
  }

  createDefaultGroups(groups: ParameterGroupDescription[], ungroupedParameterIds: Set<string>, idToParameterDescription: Map<string, any>) {

    // Create default groups for parameters not belonging to a group
    let defaultGroupMembers = { 'required': [], 'optional': [] };

    for(let parameterId of ungroupedParameterIds) {
      let parameter = idToParameterDescription.get(parameterId);
      defaultGroupMembers[parameter.optional ? 'optional' : 'required'].push(parameterId);
    }

    // Add lonely parameters to the proper default group (that we just created) 
    for(let groupName of ['optional', 'required']) {
      if(defaultGroupMembers[groupName].length > 0) {
        groups.unshift({ id: this.defaultGroupId + '_' + groupName, name: '', description: '', members: defaultGroupMembers[groupName], optional: groupName == 'optional' });
      }
    }

    return defaultGroupMembers;
  }

  createParameter(parameterId: string, parameterGroup: ParameterGroup, idToParameterDescription: Map<string, any>) {
    // Create parameter from description, add it to parameterGroup
    let parameterDescription = idToParameterDescription.get(parameterId);
    if(parameterDescription == null) {
      console.error('Missing group member in inputs.');
      return;
    }
    let parameterBase = new Parameter(parameterDescription);
    this.idToParameter.set(parameterId, parameterBase);
    parameterGroup.parameters.push(parameterBase);
    
    if(!parameterDescription.optional) {
      parameterGroup.optional = false;
    }
  }

  createParameterGroup(group: ParameterGroupDescription, groupIdPrefix: number, idToParameterDescription: Map<string, any>, formGroups: { required: any, optional: any }, parameterGroups: ParameterGroups) {
    // Create parameter group and create its members
    let groupCopy = { ...group, id: groupIdPrefix + group.id }; // Copy the group description to avoid changing descriptor (which could be reused in the futur and must remain unchanged)
    let parameterGroup = new ParameterGroup(groupCopy);
    for(let parameterId of groupCopy.members) {
      this.createParameter(parameterId, parameterGroup, idToParameterDescription);
    }
    let parameterGroupName = parameterGroup.optional ? 'optional' : 'required';
    parameterGroups[parameterGroupName].set(groupCopy.id, parameterGroup);
    formGroups[parameterGroupName][groupCopy.id] = this.toFormGroup(parameterGroup.parameters);
  }


  createFormGroups(groups: ParameterGroupDescription[], idToParameterDescription: Map<string, any>, parameterGroups: ParameterGroups) {
    // For each group: 
    //  - create a ParameterGroup, then for each member:
    //      - create a Parameter and add it to the ParameterGroup member and populate idToParameter map
    //  - create the corresponding FormGroups

    let formGroups = { 'required': {}, 'optional': {} };

    let groupIdPrefix = 0;
    for(let group of groups) {
      this.createParameterGroup(group, groupIdPrefix++, idToParameterDescription, formGroups, parameterGroups);
    }

    return formGroups;
  }

  createFormGroupFromDescriptor(descriptor: Descriptor) {

    this.parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };

    if(descriptor == null) {
      return null;
    }

    this.idToParameter.clear();

    // Create ungrouped parameter map: all inputs will be added, then grouped inputs will be removed ; ungrouped input will remain
    let ungroupedParameterIds = new Set<string>();
    // Create id to parameter description map (later used to create id to Parameter map)
    let idToParameterDescription = new Map<string, any>();
    this.createIdToParameterDescriptionMap(descriptor.inputs, ungroupedParameterIds, idToParameterDescription);

    // Create group list
    let groups = this.createGroupList(descriptor.groups, ungroupedParameterIds);
    
    // Create default groups for parameters not belonging to a group
    let defaultGroupMembers = this.createDefaultGroups(groups, ungroupedParameterIds, idToParameterDescription);

    let formGroups: any = this.createFormGroups(groups, idToParameterDescription, this.parameterGroups);
    
    // Convert the formGroups object to actual FormGroups
    formGroups['required'] = new FormGroup(formGroups['required']);
    formGroups['optional'] = new FormGroup(formGroups['optional']);

    return new FormGroup(formGroups);
  }

  generateInvocation(form: FormGroup) {
    // Generate invocation from form: extract / parse values from each dirty form values
    let invocation = {}
    for(let superGroupName of ['required', 'optional']) {
      for(let groupId in form.value[superGroupName]) {
        let group = form.value[superGroupName][groupId]
        let formGroup: FormGroup = (form.controls[superGroupName] as FormGroup).controls[groupId] as FormGroup
        for(let parameterId in group) {
          let parameterValue = group[parameterId];
          let parameter: Parameter<any> = this.idToParameter.get(parameterId);
          let dirty = (formGroup.controls[parameterId] as FormControl).dirty
          if(dirty) {
            invocation[parameterId] = parameter.parseValue(parameterValue);
          }
        }
      }
    }
    return invocation;
  }

  setFormFromInvocation(invocation: any, form: FormGroup) {
    // Set form from invocation: set from control values from invocation parameters
    // Mark form values as pristine or dirty depending on invocation
    // Only emit change event once in the end
    // Also handle exclusive groups: select the proper parameter in the group
    for(let superGroupName of ['required', 'optional']) {
      for(let groupId in form.value[superGroupName]) {
        let parameterGroup: ParameterGroup = this.parameterGroups[superGroupName].get(groupId);
        let group = form.value[superGroupName][groupId]
        let formGroup: FormGroup = (form.controls[superGroupName] as FormGroup).controls[groupId] as FormGroup
        for(let parameterId in group) {
          let parameter: Parameter<any> = this.idToParameter.get(parameterId);
          let formControl = (formGroup.controls[parameterId] as FormControl);
          let value = invocation[parameterId];
          if(value != null) {
            parameter.value = value;
            formControl.setValue(parameter.getValue(invocation[parameterId]), { emitEvent: false });
            formControl.markAsDirty();
          } else {
            formControl.markAsPristine();
            parameter.value = null;
            formControl.setValue(null, { emitEvent: false });
          }
          // If parameter is part of an exclusive group: select the corresponding option in the group
          if(value != null && parameterGroup.exclusive) {
            let group: any = { groupId: parameterGroup.id, parameterId: parameterId };
            this.selectGroupSource.next(group);
          }
        }
      }
    }
    form.updateValueAndValidity({ onlySelf: false, emitEvent: true });
    return invocation;
  }
}