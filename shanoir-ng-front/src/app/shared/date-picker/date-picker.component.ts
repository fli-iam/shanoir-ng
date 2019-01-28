import { Component, forwardRef } from '@angular/core';
import { AbstractControl, ControlValueAccessor, NG_VALUE_ACCESSOR, ValidationErrors, NgControl } from '@angular/forms';
import { IMyOptions } from 'mydatepicker';

@Component({
    selector: 'datepicker',
    template: `
        <my-date-picker 
            [options]="options" 
            [ngModel]="convertedDate"
            (ngModelChange)="onModelChange($event)"
            (inputFieldChanged)="onInputFieldChanged($event)"
            (inputFocusBlur)="onTouch()">
        </my-date-picker>
    `,
    styles: [
        ':host() { display: inline-block; height: 19px; }'
    ],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => DatepickerComponent),
          multi: true,
        }]   
})

export class DatepickerComponent implements ControlValueAccessor {

    private inputFieldContent: string;
    private lastInputFieldContent: string;
    private convertedDate: Object;
    private onTouch: () => void;
    private onChange: (value) => void;
    private ngControl: NgControl;

    private options: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '21px',
        width: '160px',
        indicateInvalidDate: false
    };

    constructor() {

    }

    private onInputFieldChanged(event) {
        this.lastInputFieldContent = this.inputFieldContent;
        this.inputFieldContent = event.value;
    }

    onModelChange(event) {
        setTimeout(() => {
            if (this.inputFieldContent == this.lastInputFieldContent) return;
            if (event && event.epoc) {
                this.onChange(new Date(event.epoc * 1000));
            } else if (this.inputFieldContent) {
                this.onChange('invalid');
            } else {
                this.onChange(null);
            }
            this.onTouch();
        })
    }

    writeValue(value: any): void {
        if (value) {
            this.convertedDate = {jsdate: new Date(value)};
        } else {
            this.convertedDate = null; 
        }
    }
    
    registerOnChange(fn: (_: any) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouch = fn;
    }
    
    public static validator = (control: AbstractControl): ValidationErrors | null => {
        if (control.value == 'invalid') {
            return { format: true}
        }
        return null;
    }
}