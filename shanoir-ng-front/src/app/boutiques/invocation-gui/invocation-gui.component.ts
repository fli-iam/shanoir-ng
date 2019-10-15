import { Component, Input, Output, OnInit, EventEmitter } from '@angular/core';
import { FormGroup, FormControl }                 from '@angular/forms';
import { ParameterControlService }    from './parameter-control.service';
import { ParameterGroup }    from './parameter-group/parameter-group';

@Component({
  selector: 'invocation-gui',
  templateUrl: './invocation-gui.component.html',
  styleUrls: ['./invocation-gui.component.css'],
  providers: [ ParameterControlService ]
})
export class InvocationGuiComponent implements OnInit {
  
  @Output() invocationChanged = new EventEmitter<any>();
  parameterGroups: { required: Map<string, ParameterGroup>, optional: Map<string, ParameterGroup> } = null;
  form: FormGroup = null;

  constructor(private pcs: ParameterControlService) { }

  ngOnInit() {
  }

  @Input()
  set descriptor(descriptor: any) {
    this.parameterGroups = {
      required: new Map<string, ParameterGroup>(),
      optional: new Map<string, ParameterGroup>()
    };
    this.form = this.pcs.createFormGroupFromDescriptor(descriptor, this.parameterGroups);
    if(this.form != null) {
      this.form.valueChanges.subscribe( (value)=> this.onChange(value) );
    }
  }

  onToggleOptionalParameters(eventTarget: any) {
    let optionalFormGroup: FormGroup = this.form.controls.optional as FormGroup;
    if(!eventTarget.checked) {
      optionalFormGroup.disable();
      optionalFormGroup.markAsPristine();
    } else {
      optionalFormGroup.enable();

      for(let formGroupId in optionalFormGroup.controls) {
        let formGroup: FormGroup = optionalFormGroup.controls[formGroupId] as FormGroup;
        for(let formControlId in formGroup.controls) {
          let formControl: FormControl = formGroup.controls[formControlId] as FormControl;
          if(formControl.value != null && formControl.value != "") {
            formControl.markAsDirty();
            formControl.updateValueAndValidity();
          }
        }
      }

    }

    
      
  }

  onChange(value: any) {
    let invocation = this.pcs.generateInvocation(this.form);
    this.invocationChanged.emit(invocation);
  }
}
