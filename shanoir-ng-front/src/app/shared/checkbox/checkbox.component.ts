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

import { Component, Input, Output, HostListener, HostBinding, EventEmitter, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
    selector: 'checkbox',
    templateUrl: 'checkbox.component.html',
    styleUrls: ['checkbox.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => CheckboxComponent),
            multi: true,
        }
    ],
    imports: [NgIf]
})
export class CheckboxComponent implements ControlValueAccessor { 
    
    @HostBinding('class.on') model: boolean | 'indeterminate' = false;
    @Output() userChange = new EventEmitter();
    private onTouchedCallback = () => { return; };
    private onChangeCallback: (any) => void = () => { return; };
    @Input() @HostBinding('class.disabled') disabled: boolean = false;
    @Input() inverse: boolean = false;
    @Input() mode: "edit" | "view";

    @HostListener('click', []) 
    onClick() {
        if (this.disabled || this.mode == "view") return;
        this.toogle();
    }

    @HostListener('keydown', ['$event']) 
    onKeyPress(event: any) {
        if (this.disabled || this.mode == "view") return;
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
        this.onChangeCallback(this.model);
        this.userChange.emit(this.model);
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

    @HostBinding('class.view') 
    get viewMode() {
        return this.mode == 'view';
    }
        
}