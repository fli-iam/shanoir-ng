import { Component, Input, Output, EventEmitter, ElementRef, ViewChild } from '@angular/core';
import { FormGroup, FormControl }        from '@angular/forms';
import { Parameter }     from './parameter';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'parameter',
  templateUrl: './parameter.component.html',
  styleUrls: ['./parameter.component.css']
})
export class ParameterComponent {

  @Input() parameter: Parameter<any>;
  @Input() formGroup: FormGroup;
  @Output() selectDataClicked = new EventEmitter<Parameter<any>>();
  @ViewChild('parameterInput', { static: false }) parameterInput: ElementRef;

  constructor() { }

  get isValid() { return this.formGroup.controls[this.parameter.id].valid; }

  selectData(event: Event) {
    this.selectDataClicked.emit(this.parameter);
  }

  displaySelectData() {
    return this.parameter.list ? 'Add data' : 'Select data';
  }

  unset(event: Event) {
    // Mark as pristine, set value to null (or false for flags), and uncheck
    let activeControl = this.formGroup.get(this.parameter.id) as FormControl;
    activeControl.markAsPristine();
    activeControl.setValue(this.parameter.type != 'Flag' ? null : false);
    if(this.parameter.type == 'Flag') {
      this.parameterInput.nativeElement.checked = false;
    }
  }

  onChange(eventTarget: any) {
    if(this.parameter.type != 'Flag') {
      return;
    }
    // If parameter is a flag: set form control value
    let activeControl = this.formGroup.get(this.parameter.id) as FormControl
    activeControl.setValue(eventTarget.checked);
  }
}
