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

import { Component, forwardRef, Input, OnChanges, SimpleChanges } from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";
import { FacetResultPage } from "../../solr/solr.document.model";

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

export class CheckboxListComponent implements ControlValueAccessor, OnChanges{
    onChange = (_: any) => {};
    onTouched = () => {};
    @Input() items: FacetResultPage;
    selectedItems: any[] = [];
    selectAll: boolean = true;
    currentPage: number = 0;
    itemsPersPage: number = 5;
    totalPages: number =  0;
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.items && this.items !== undefined) {
            this.totalPages = Math.ceil(this.items.totalElements / this.itemsPersPage);console.log('totalPages: ', this.totalPages)
            // this.selectUnselectAll(false); // checked all by default
        }
    }

    selectUnselectAll (isChecked: boolean) {
        this.items.content.map(item => { item.checked = isChecked; return item; })
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
        this.selectedItems = this.items.content.filter(item => item.checked).map(item => item.value);
        this.onChange(this.selectedItems);
    }

    setPage(page: number) {
        if (page < 0) return;
        this.currentPage = page;console.log('currentPage: ', this.currentPage)
    }
}