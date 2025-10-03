/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { Component, forwardRef, Input } from '@angular/core';
import { AbstractControl, ControlValueAccessor, NG_VALUE_ACCESSOR, ValidationErrors } from '@angular/forms';


@Component({
    selector: 'datepicker',
    template: `
            <input type="date" 
                [class.empty]="!this.dateString || this.dateString == ''"
                [disabled]="disabled"
                [ngModel]="dateString"
                (ngModelChange)="onModelChange($event)"
                (focusout)="onTouch()"/>
    `,
    styles: [
        ':host { display: inline-block; height: 19px; }',
        ':host:has(input:focus) { border-bottom: 2px solid var(--color-a); }',
        'input { border: none !important; }',
        '.empty { color: var(--greyer); }'
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => DatepickerComponent),
            multi: true,
        }
    ],
    standalone: false
})
export class DatepickerComponent implements ControlValueAccessor {

    dateString: string = '';
    onTouch: () => void;
    private onChange: (value) => void;
    @Input() disabled: boolean = false;

    onModelChange(dateStr: string) {
        if (this.dateString == dateStr) return;
        this.dateString = dateStr;
        if (dateStr == '') this.onChange('invalid');
        else this.onChange(new Date(dateStr));
        this.onTouch();
    }

    writeValue(value: Date): void {
        if (value) {
            this.dateString = this.toDateString(value);
        } else {
            this.dateString = null;
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
            return { format: true }
        }
        return null;
    }

    public static inFutureValidator = (control: AbstractControl): ValidationErrors | null => {
        if (control.value != 'invalid') {
            let date: Date = control.value;
            if (date && date.getTime && date.getTime() < Date.now()) return { future: true }
        }
        return null;
    }

    private toDateString(date: Date): string {
        return date.getFullYear()
        + '-' 
        + ('0' + (date.getMonth() + 1)).slice(-2)
        + '-' 
        + ('0' + date.getDate()).slice(-2);
   }
}