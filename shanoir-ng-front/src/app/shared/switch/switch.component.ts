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
    selector: 'toggle-switch',
    templateUrl: 'switch.component.html',
    styleUrls: ['switch.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => ToggleSwitchComponent),
          multi: true,
        }]   
})

export class ToggleSwitchComponent implements ControlValueAccessor { 
    
    @Input() @HostBinding('class.on') ngModel: boolean = null;
    @Output() ngModelChange = new EventEmitter();
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() @HostBinding('class.disabled') disabled: boolean = false;
    @Input() reverse: boolean = false;

    constructor() {}

    @HostListener('click', []) 
    onClick() {
        if (this.disabled) return; 
        this.ngModel = !this.ngModel;
        this.ngModelChange.emit(this.ngModel);
    }

    @HostListener('keydown', ['$event']) 
    onKeyPress(event: any) {
        if (this.disabled) return;
        if (' ' == event.key) {
            this.ngModel = !this.ngModel;
            this.ngModelChange.emit(this.ngModel);
            event.preventDefault();
        } else if ('ArrowLeft' == event.key) {
            if (this.ngModel) {
                this.ngModel = false;
                this.ngModelChange.emit(this.ngModel);    
            }
        } else if ('ArrowRight' == event.key) {
            if (!this.ngModel) {
                this.ngModel = true;
                this.ngModelChange.emit(this.ngModel);    
            }
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['ngModel']) {
            this.onChangeCallback(this.ngModel);
        }
    }
    writeValue(obj: any): void {
        this.ngModel = obj;
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