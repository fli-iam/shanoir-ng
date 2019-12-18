import { Component, OnInit, Input, Output, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { FormControl, FormGroup }        from '@angular/forms';
import { Parameter }     from '../parameter/parameter';
import { ParameterComponent }     from '../parameter/parameter.component';
import { ParameterGroup }     from './parameter-group';

@Component({
  selector: 'parameter-group',
  templateUrl: './parameter-group.component.html',
  styleUrls: ['./parameter-group.component.css']
})
export class ParameterGroupComponent implements OnInit {

  @Input() parameterGroup: ParameterGroup;
  @Input() formGroup: FormGroup;
  @Output() selectDataClicked = new EventEmitter<Parameter<any>>();
  @ViewChildren(ParameterComponent) parameterComponents !: QueryList<ParameterComponent>;

  selectedParameterId: string = null;

  constructor() { }

  ngOnInit() {
  }

  onSelectData(parameter: Parameter<any>) {
    this.selectDataClicked.emit(parameter);
  }

  onParameterSelectionChange($event: Event) {
    // this.parameterComponents will contain the only parameterComponent on screen:
    //  - the previously selected parameterComponent: mark it pristine and untouched and propagate changes
    this.parameterComponents.forEach((parameterComponent, parameterIndex)=> {
      let activeControl = this.formGroup.get(parameterComponent.parameter.id) as FormControl;
      activeControl.markAsPristine();
      activeControl.markAsUntouched();
      activeControl.updateValueAndValidity();
    })

    //  - after one cycle: the newly selected parameterComponent: make it true (or its previous value if not a flag), mark it dirty and propagate changes
    setTimeout(()=> {
      this.parameterComponents.forEach((parameterComponent, parameterIndex)=> {
        let activeControl = this.formGroup.get(parameterComponent.parameter.id) as FormControl;
        if(parameterComponent.parameter.type == 'Flag') {
          activeControl.setValue(true);
          parameterComponent.parameterInput.nativeElement.checked = true;
        } else {
          activeControl.setValue(activeControl.value);
        }
        activeControl.markAsDirty();
        activeControl.updateValueAndValidity();
      })
    }, 0)
  }
}
