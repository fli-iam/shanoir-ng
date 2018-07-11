import { Component, Input, Output, EventEmitter, forwardRef, OnChanges, SimpleChanges } from '@angular/core';
import { IMyOptions } from 'mydatepicker';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { pad } from '../../utils/app.utils';

@Component({
    selector: 'datepicker',
    template: `
        <my-date-picker 
            [options]="options" 
            [ngModel]="convertedDate"
            (ngModelChange)="onModelChange($event)">
        </my-date-picker>
    `,
    styles: ['my-date-picker { }'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => DatepickerComponent),
          multi: true,
        }]   
})

export class DatepickerComponent implements ControlValueAccessor, OnChanges {
    
    @Input() ngModel: Date = null;
    @Output() ngModelChange = new EventEmitter<Date>();
    private convertedDate: Object;

    private options: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '21px',
        width: '160px'
    };

    constructor() {

    }

    onModelChange(event) {
        if (event) {
            const chosenDate: Date = new Date([event.date.year, pad(event.date.month, 2), pad(event.date.day, 2)].join('-') + 'T00:00:00Z');
            this.ngModelChange.emit(chosenDate);
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['ngModel']) {
            if (this.ngModel) {
                this.convertedDate = {jsdate: new Date(this.ngModel)};
            } else {
                this.convertedDate = null;
            }
        }
    }

    writeValue(obj: any): void {
    }
    
    registerOnChange(fn: any): void {
    }

    registerOnTouched(fn: any): void {
    }

}