import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, Validator, AbstractControl, FormControl } from '@angular/forms';

@Component({
    selector: 'datepicker',
    template: `
        <my-date-picker 
            [options]="options" 
            [ngModel]="ngModel"
            (ngModelChange)="onModelChange($event)">
        </my-date-picker>
    `,
    styles: ['my-date-picker { position: absolute; }'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => DatepickerComponent),
          multi: true,
        }]   
})

export class DatepickerComponent implements ControlValueAccessor {
    
    @Input() ngModel: Date;
    @Output() ngModelChange = new EventEmitter();

    private options: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '21px',
        width: '160px'
    };

    constructor() {

    }

    onModelChange(event) {
        let newDate: Date = null;
        if (event) {
            newDate = new Date(event.date.year, event.date.month - 1, event.date.day, 0, 0, 0, 0);
        }
        this.ngModelChange.emit(newDate);
    }

    writeValue(obj: any): void {
        this.ngModel = obj;
    }
    
    registerOnChange(fn: any): void {
    }

    registerOnTouched(fn: any): void {
    }

}