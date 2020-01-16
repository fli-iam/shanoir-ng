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
import { Component, forwardRef, Input, HostListener } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';


@Component({
    selector: 'auto-ajust-input',
    templateUrl: 'auto-ajust-input.component.html',
    styleUrls: ['auto-ajust-input.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => AutoAdjustInputComponent),
          multi: true,
        }]   
})

export class AutoAdjustInputComponent implements ControlValueAccessor {
    
    model: any;
    disabled: boolean;
    @Input() placeholder: string;
    onTouch = () => {};
    onChange = (_: any) => {};

    
    writeValue(obj: any): void {
        this.model = obj;
    }    
    
    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouch = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }
    
    @HostListener('focusout', ['$event']) 
    private onFocusOut() {
        this.onTouch();
    }
}