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
import { Component, ElementRef, EventEmitter, forwardRef, HostListener, Input, OnChanges, Output, PipeTransform, SimpleChanges } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import {arraysEqual, isDarkColor, objectsEqual} from '../../utils/app.utils';
import { Option } from '../select/select.component';


@Component({
    selector: 'multi-select',
    templateUrl: 'multi-select.component.html',
    styleUrls: ['multi-select.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => MultiSelectComponent),
            multi: true,
        }
    ],
    standalone: false
})

export class MultiSelectComponent implements ControlValueAccessor, OnChanges {

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


    constructor(
        private element: ElementRef
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes.optionArr && this.optionArr && !arraysEqual(changes.optionArr?.currentValue, changes.optionArr?.previousValue)) {
            this.options = [];
            this.optionArr.forEach(item => {
                let label: string = this.getLabel(item);
                let newOption: Option<any> = new Option<any>(item, label);
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
            selected.disabled = true;
            if (selected) this.selectedOptions.push(selected);
        });
    }

    onSelectOption(e: any) {
        let option: Option<{id: number}> = this.options?.find(opt => objectsEqual(opt?.value, e));
        this.modelArray.push(option.value);
        this.selectedOptions.push(option);
        option.disabled = true;
        this.options = [...this.options];
        this.onChangeCallback(this.modelArray);
    }

    onRemoveOption(option: Option<any>, index: number) {
        this.modelArray.splice(index, 1);
        this.selectedOptions.splice(index, 1);
        option.disabled = false;
        this.onChangeCallback(this.modelArray);
    }

    @HostListener('focusout', ['$event'])
    onFocusOut(event: FocusEvent) {
        if (!this.element.nativeElement.contains(event.relatedTarget)) {
            this.onTouchedCallback();
            this.touch.emit();
        }
    }

    getFontColor(colorInp: string): boolean {
      return isDarkColor(colorInp);
    }
}
