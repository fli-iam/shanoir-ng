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

import { Component, forwardRef, Input, OnChanges, SimpleChanges, ChangeDetectorRef, OnInit } from "@angular/core";
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

export class CheckboxListComponent implements ControlValueAccessor, OnChanges, OnInit {
    onChange = (_: any) => {};
    searchBarContent: string;
    onTouched = () => {};
    @Input() items: FacetResultPage;
    initialItems: FacetResultPage;
    displayedItems: FacetResultPage;
    selectedItems: any[] = [];
    selectAll: boolean = true;
    currentPage: number = 0;
    itemsPersPage: number = 10;
    totalPages: number =  0;
   
   constructor(
      private cdr: ChangeDetectorRef,
    ) {
    }

    ngOnInit() {
        this.initialItems = JSON.parse(JSON.stringify(this.items))
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.items && this.displayedItems !== undefined) {
            this.searchBarContent = "";
            this.search();
            this.totalPages = Math.ceil(this.displayedItems.totalElements / this.itemsPersPage);
            // this.selectUnselectAll(false); // checked all by default
        }
    }

    search() {
        this.displayedItems = this.initialItems
        if (this.searchBarContent && this.displayedItems) {
            this.displayedItems.content = this.displayedItems.content.filter(element => element.value.indexOf(this.searchBarContent) == 0)
        }
        this.cdr.detectChanges();
    }

    selectUnselectAll (isChecked: boolean) {
        this.searchBarContent = "";
        this.search();
        this.displayedItems.content.map(item => { item.checked = isChecked; return item; })
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
        this.searchBarContent = "";
        this.search();
        this.selectedItems = this.displayedItems.content.filter(item => item.checked).map(item => item.value);
        this.onChange(this.selectedItems);
    }

    setPage(page: number) {
        if (page < 0) return;
        this.currentPage = page;
    }
}