import { Component, Input, Output, OnInit, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { FormGroup, FormControl }                 from '@angular/forms';
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
              private breadcrumbsService: BreadcrumbsService) {
    
    // An exclusive group can be changed given an invocation (on page reload, or if user gives an invocation file)
    // In this case: select the proper parameter in the exclusive group select options (see parameter-group.component)
    this.selectGroupChange = this.pcs.selectGroupChange.subscribe((group: any)=> {
      this.setExclusiveGroup(group.groupId, group.parameterId);
    });
  }

  ngOnInit() {
  }

  ngAfterViewInit() {
    // Initialize invocation when the parameter group components are added
    // The form is built once with the descriptor, and then is updated with the invocation loaded from the sessionStorage (in tool service)
    // Thus, a delay(0) is necessary for angular to avoid the ExpressionChangedAfterItHasBeenCheckedError
    this.parameterGroupComponents.changes.pipe(delay(0)).subscribe((r) => { this.initializeInvocation(); });
  }

  initializeInvocation() {
    // Make sure both form and parameterGroupComponents are initialized
    if(this.form != null && this.parameterGroupComponents.length > 0) {

      // Get invocation from tool service (loaded from session storage)
      let invocation = this.toolService.data.invocation;
      if(invocation != null) {
        // Set the form from the loaded invocation
        this.pcs.setFormFromInvocation(invocation, this.form);
        // let invocation = this.pcs.generateInvocation(this.form);
        // this.invocationChanged.emit(invocation);
      }

      if(this.formChange == null) {
        // Subscribe to value changes
        this.formChange = this.form.valueChanges.subscribe( (value)=> this.onChange(value) );
      }
    }
  }

  @Input()
  set descriptor(descriptor: any) {
    // Create the form groups from the descriptor json file
    this.form = this.pcs.createFormGroupFromDescriptor(descriptor);
    this.parameterGroups = this.pcs.parameterGroups;
    // At this point this.parameterGroupComponents should be empty, so initializeInvocation() will do nothing
    // it will be called in ngAfterViewInit()
    // Anyway, make sure initializeInvocation() is called even if ngAfterViewInit() is called before set descriptor()
    this.initializeInvocation();
  }

  onToggleOptionalParameters(eventTarget: any) {
    // On toggle optional parameters group checkbox:
    // Enable & mark as dirty / Disable & mark as pristine group
    // Update value and validity of each group members
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
    // On form changes: generate invocation and emit invocationChanged event for parent components (invocation)
    let invocation = this.pcs.generateInvocation(this.form);
    this.invocationChanged.emit(invocation);
  }

  onSelectData(parameter: Parameter<any>) {
    // On select data (user clicks the SelectData button):
    // Generate and store invocation, currentParameterId and currentParameterIsList
    // to retrieve them later when going back on the boutiques tool page
    let invocation = this.pcs.generateInvocation(this.form);
    
    // Reset the given parameter
    if(!parameter.list) {
      invocation[parameter.id] = '';
    } else if(invocation[parameter.id] == null) {
      invocation[parameter.id] = [];
    }
    
    // Save invocation, currentParameterId & currentParameterIsList in session
    this.toolService.saveSession({ invocation: invocation, currentParameterId: parameter.id, currentParameterIsList: parameter.list });

    // Navigate to dataset list
    this.breadcrumbsService.replace = false;
    this.router.navigate(['boutiques/dataset/list'], {replaceUrl: false });
  }

  setInvocation(invocation: any) {
    // Set form from invocation
    this.pcs.setFormFromInvocation(invocation, this.form);
  }

  setExclusiveGroup(groupId: string, selectedParameterId: string) {
    // Select the given parameter (selectedParameterId) in the group (groupId)
    for(let parameterGroupComponent of this.parameterGroupComponents.toArray()) {
      if(parameterGroupComponent.parameterGroup.id == groupId) {
        parameterGroupComponent.selectedParameterId = selectedParameterId;
      }
    }
  }
}

