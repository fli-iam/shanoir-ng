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

import { Input } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';
import { Mode } from '../components/entity/entity.component.abstract';

export abstract class AbstractInput implements ControlValueAccessor {

    @Input() mode: Mode;
    model: any;
    disabled: boolean = false;
    propagateChange = (_: any) => {};
    protected propagateTouched = () => {};
    
    constructor() {}

    writeValue(obj: any): void {
        if (obj) this.model = obj;
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

}