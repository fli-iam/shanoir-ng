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
import { Component, EventEmitter, forwardRef, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { slideDown } from '../../shared/animations/animations';


@Component({
    selector: 'solr-text-search-mode',
    templateUrl: 'solr.text-search-mode.component.html',
    styleUrls: ['solr.text-search.component.css'],
    animations: [slideDown],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => SolrTextSearchModeComponent),
          multi: true,
        }]  
})

export class SolrTextSearchModeComponent implements ControlValueAccessor {

    showInfo: boolean = false;
    @Output() onChange: EventEmitter<boolean> = new EventEmitter();
    expertMode: boolean = false;
    protected propagateChange = (_: any) => {};
    protected propagateTouched = () => {};

    onExpertModeChange() {
        this.propagateChange(this.expertMode);    
    }

    onExpertModeUserChange() {
        this.onExpertModeChange();
        this.onChange.emit(this.expertMode);
    }

    writeValue(value: boolean): void {
        this.expertMode = value;
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }
}