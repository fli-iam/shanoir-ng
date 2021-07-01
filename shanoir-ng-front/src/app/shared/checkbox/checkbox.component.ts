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

import { Component, Input, Output, SimpleChanges, HostListener, HostBinding, EventEmitter, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
    selector: 'checkbox',
    templateUrl: 'checkbox.component.html',
    styleUrls: ['checkbox.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => CheckboxComponent),
          multi: true,
        }]   
})
export class CheckboxComponent implements ControlValueAccessor { 
    
    @HostBinding('class.on') model: boolean | 'indeterminate' = false;
    @Output() onChange = new EventEmitter();
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() @HostBinding('class.disabled') disabled: boolean = false;
    @Input() inverse: boolean = false;

    constructor() {}

    @HostListener('click', []) 
    onClick() {
        if (this.disabled) return;
        this.toogle();
    }

    @HostListener('keydown', ['$event']) 
    onKeyPress(event: any) {
        if (this.disabled) return;
        if (' ' == event.key) {
            this.toogle();
            event.preventDefault();
        }
    }

    private toogle() {
        if (this.model == true || this.model == 'indeterminate') {
            this.model = false;
        } else {
            this.model = true;
        }
        this.onChange.emit(this.model);
        this.onChangeCallback(this.model);
        this.onTouchedCallback();
    }
    
    writeValue(obj: any): void {
        this.model = obj;
    }
    
    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    @HostListener('focusout', []) 
    onFocusOut() {
        this.onTouchedCallback();
    }

    @HostBinding('attr.tabindex')
    get tabindex(): number {
        return this.disabled ? undefined : 0;
    } 
}