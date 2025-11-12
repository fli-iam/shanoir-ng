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
import { Component, ElementRef, EventEmitter, forwardRef, HostListener, Input, OnChanges, Output, PipeTransform, SimpleChanges, ViewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import { arraysEqual, objectsEqual } from '../../utils/app.utils';
import { BrowserPaging } from '../components/table/browser-paging.model';
import { ColumnDefinition } from '../components/table/column.definition.type';
import { FilterablePageable, Page } from '../components/table/pageable.model';
import { TableComponent } from '../components/table/table.component';
import { Option, SelectBoxComponent } from '../select/select.component';
import { NgIf } from '@angular/common';


@Component({
    selector: 'multi-select-table',
    templateUrl: 'multi-select-table.component.html',
    styleUrls: ['multi-select-table.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => MultiSelectTableComponent),
            multi: true,
        }
    ],
    imports: [NgIf, SelectBoxComponent, TableComponent]
})

export class MultiSelectTableComponent implements ControlValueAccessor, OnChanges {

    @Output() userChange = new EventEmitter();
    @Input() options: Option<{id: number}>[];
    @Input() optionArr: any[];
    @Input() optionBuilder: { list: any[], labelField: string, getLabel: (any) => string };
    @Input() pipe: PipeTransform;
    @Input() disabled: boolean = false;
    @Input() readOnly: boolean = false;
    @Input() placeholder: string;
    @Output() touch = new EventEmitter();
    private onTouchedCallback = () => { return; };
    private onChangeCallback: (any) => void = () => { return; };
    modelArray: any[];
    selectedOptions: Option<any>[] = [];
    @Input() columnDefs: ColumnDefinition[] = [];
    private browserPaging: BrowserPaging<any>;
    @ViewChild('table', { static: false }) table: TableComponent;


    constructor(
            private element: ElementRef
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes.optionArr && this.optionArr && !arraysEqual(changes.optionArr?.currentValue, changes.optionArr?.previousValue)) {
            this.options = [];
            this.optionArr.forEach(item => {
                const label: string = this.getLabel(item);
                const newOption: Option<any> = new Option<any>(item, label);
                if (item.color) newOption.color = item.color;
                if (item.backgroundColor) newOption.backgroundColor = item.backgroundColor;
                this.options.push(newOption);
            });
            this.updateData();
        } else if (changes.optionBuilder && this.optionBuilder && changes.optionBuilder.currentValue != changes.optionBuilder.previousValue ) {
            if (this.optionBuilder.list) {
                this.options = [];
                this.optionBuilder.list.forEach(item => {
                    let label: string = '';
                    if (this.optionBuilder.getLabel) {
                        label = this.optionBuilder.getLabel(item);
                    } else if (this.optionBuilder.labelField) {
                        label = item[this.optionBuilder.labelField];
                    }
                    this.options.push(new Option<any>(item, label));
                });
            }
            this.updateData();
        } else if (changes.columnDefs && !this.readOnly) {
            this.columnDefs?.push({tip: () => { return "Delete" }, type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.onRemoveItem(item)})
        }
    }

    private getLabel(item: any): string {
        let label: string = '';
        if (this.pipe) label = this.pipe.transform(item);
        else if (typeof(item) == 'string') label = item;
        else if (item.label) label = item.label;
        else if (item.name) label = item.name;
        else if (item.value) label = item.value;
        return label;
    }

    writeValue(obj: any): void {
        this.modelArray = obj;
        this.browserPaging = new BrowserPaging(this.modelArray, this.columnDefs);
        this.table?.refresh();
        this.updateData();
    }

    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    updateData() {
        if (!this.modelArray || !this.options) return;
        this.selectedOptions = [];
        this.modelArray.forEach(modelValue => {
            const selected: Option<any> = this.options.find(option => objectsEqual(option.value, modelValue));
            if (selected) {
                selected.disabled = true;
                this.selectedOptions.push(selected);
            }
        });
    }

    onSelectOption(id: number) {
        const option: Option<{id: number}> = this.options?.find(opt => opt?.value?.id == id);
        this.modelArray.push(option.value);
        this.selectedOptions.push(option);
        option.disabled = true;
        this.options = [...this.options];
        this.browserPaging.setItems(this.modelArray);
        this.table.refresh();
        this.onChangeCallback(this.modelArray);
    }

    onRemoveItem(item: any) {
        this.modelArray = this.modelArray?.filter(one => one.id != item.id);
        const removed: Option<any> = this.options?.find(option => option.value?.id == item.id);
        if (removed) removed.disabled = false;
        this.selectedOptions = this.selectedOptions?.filter(option => option.value?.id != item.id);
        this.browserPaging.setItems(this.modelArray);
        this.table.refresh();
        this.onChangeCallback(this.modelArray);
    }

    getPage(pageable: FilterablePageable): Promise<Page<any>> {
        return Promise.resolve(this.browserPaging.getPage(pageable));
    }

    @HostListener('focusout', ['$event'])
    onFocusOut(event: FocusEvent) {
        if (!this.element.nativeElement.contains(event.relatedTarget)) {
            this.onTouchedCallback();
            this.touch.emit();
        }
    }

}
