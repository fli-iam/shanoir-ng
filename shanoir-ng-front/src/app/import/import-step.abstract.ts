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

import { Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';


export abstract class AbstractImportStepComponent implements OnChanges {

    @Input() open: boolean;
    @Input() disabled: boolean;
    @Input() lastEnabled: boolean = false;
    @Output() headerClick = new EventEmitter<any>();
    @Output() validityChange = new EventEmitter<boolean>();
    private valid: boolean;

    constructor() {}
    
    ngOnChanges(changes: SimpleChanges) {
        this.updateValidity();
    }

    protected updateValidity() {
        let newValue = this.getValidity();
        if (this.valid != newValue) {
            this.validityChange.emit(newValue);
        }
        this.valid = newValue;
    }

    abstract getValidity(): boolean;

    private onHeaderClick(event: any) {
        if (!this.disabled) this.headerClick.emit(event);
    }

}