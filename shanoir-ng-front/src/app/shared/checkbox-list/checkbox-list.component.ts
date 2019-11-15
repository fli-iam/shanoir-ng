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

import { Component, forwardRef, Input, OnInit } from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";

@Component({
    selector: 'checkbox-list',
    templateUrl: 'checkbox-list.component.html',
    styleUrls: ['checkbox-list.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => CheckboxListComponent),
          multi: true,
        }]   
})

export class CheckboxListComponent implements ControlValueAccessor, OnInit{
    onChange = (_: any) => {};
    onTouched = () => {};
    @Input() items: any[];
    selectedItems: any[] = [];
    selectAll: boolean = true;

    ngOnInit() {
        this.selectUnselectAll(true); // checked all by default
    }

    selectUnselectAll (isChecked: boolean) {
        this.items.map(item => { item.checked = isChecked; return item; })
        this.onToggle();
    }
    
    writeValue(obj: any): void {
        this.selectedItems = obj;
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }
    
    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    onToggle() {
        this.selectedItems = this.items.filter(item => item.checked).map(item => item.value);
        this.onChange(this.selectedItems);
        console.log("this.selectedItems: ", this.selectedItems);
    }

}