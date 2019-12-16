import { Component, Input, Output, OnInit, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { FormGroup, FormControl }                 from '@angular/forms';
import { Router as AngularRouter } from '@angular/router';
import { ToolService }    from '../tool.service';
import { ParameterControlService }    from './parameter-control.service';
import { ParameterGroup }    from './parameter-group/parameter-group';
import { ParameterGroupComponent }    from './parameter-group/parameter-group.component';
import { Parameter }     from './parameter/parameter';
import { Router } from '../../breadcrumbs/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { Subscription } from 'rxjs/Subscription';
import { startWith, tap, delay } from 'rxjs/operators';

@Component({
  selector: 'invocation-gui',
  templateUrl: './invocation-gui.component.html',
  styleUrls: ['./invocation-gui.component.css'],
  providers: [ ParameterControlService ]
})
export class InvocationGuiComponent implements OnInit {
  
  @ViewChildren(ParameterGroupComponent) parameterGroupComponents:QueryList<ParameterGroupComponent>;

  @Output() invocationChanged = new EventEmitter<any>();
  parameterGroups: { required: Map<string, ParameterGroup>, optional: Map<string, ParameterGroup> } = null;
  form: FormGroup = null;

  private selectGroupChange: Subscription = null;
  private formChange: Subscription = null;

  constructor(private toolService: ToolService, 
              private pcs: ParameterControlService, 
              private router: Router, 
              private angularRouter: AngularRouter, 
              private breadcrumbsService: BreadcrumbsService) {
    
    this.selectGroupChange = this.pcs.selectGroupChange.subscribe((group: any)=> {
      this.setExclusiveGroup(group.groupId, group.parameterId);
    });
  }

  ngOnInit() {
  }

  ngAfterViewInit() {
    this.parameterGroupComponents.changes.pipe(delay(0)).subscribe((r) => { this.initializeInvocation(); });
  }

  initializeInvocation() {
    if(this.form != null && this.parameterGroupComponents.length > 0) {

      let invocation = this.toolService.data.invocation;
      if(invocation != null) {
        this.pcs.setFormFromInvocation(invocation, this.form);
        // let invocation = this.pcs.generateInvocation(this.form);
        // this.invocationChanged.emit(invocation);
      }
      if(this.formChange == null) {
        this.formChange = this.form.valueChanges.subscribe( (value)=> this.onChange(value) );
      }
    }
  }

  @Input()
  set descriptor(descriptor: any) {
    this.form = this.pcs.createFormGroupFromDescriptor(descriptor);
    this.parameterGroups = this.pcs.parameterGroups;
    // At this point this.parameterGroupComponents should be empty, so initializeInvocation() will do nothing
    // it will be called in ngAfterViewInit()
    // Anyway, make sure initializeInvocation() is called even if ngAfterViewInit() is called before set descriptor()
    this.initializeInvocation();
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

  onSelectData(parameter: Parameter<any>) {
    let invocation = this.pcs.generateInvocation(this.form);
    
    if(!parameter.list) {
      invocation[parameter.id] = '';
    } else if(invocation[parameter.id] == null) {
      invocation[parameter.id] = [];
    }
    
    this.toolService.saveSession({ invocation: invocation, currentParameterId: parameter.id, currentParameterIsList: parameter.list });

    this.breadcrumbsService.replace = false;
    // this.breadcrumbsService.nameStep('Boutiques');
    this.router.navigate(['boutiques/dataset/list'], {replaceUrl: false });
  }

  setInvocation(invocation: any) {
    this.pcs.setFormFromInvocation(invocation, this.form);
  }

  setExclusiveGroup(groupId: string, selectedParameterId: string) {
    for(let parameterGroupComponent of this.parameterGroupComponents.toArray()) {
      if(parameterGroupComponent.parameterGroup.id == groupId) {
        parameterGroupComponent.selectedParameterId = selectedParameterId;
      }
    }
  }
}

