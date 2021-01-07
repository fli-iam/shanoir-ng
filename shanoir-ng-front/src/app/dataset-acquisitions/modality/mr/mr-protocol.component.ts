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
import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { MrProtocol } from './mr-protocol.model';


@Component({
    selector: 'mr-protocol',
    templateUrl: 'mr-protocol.component.html',
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => MrProtocolComponent),
          multi: true,
        }]   
})
export class MrProtocolComponent implements ControlValueAccessor {
    
    public protocol: MrProtocol;
    @Input() private mode: Mode;
    protected disabled: boolean = false;
    protected propagateChange = (_: any) => {};
    protected propagateTouched = () => {};
    
    writeValue(obj: any): void {
        this.protocol = obj;
    }
    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }
    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }
    setDisabledState?(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    onChange() {
        this.propagateChange(this.protocol);
    }

}