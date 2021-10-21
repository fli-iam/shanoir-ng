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
import { Component, EventEmitter, forwardRef, HostBinding, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Option } from '../shared/select/select.component';

import { Tag } from './tag.model';


export type Mode =  "view" | "edit" | "create";
@Component({
    selector: 'tag-list',
    templateUrl: 'tag.input.component.html',
    styleUrls: ['tag.input.component.css'],
    providers: [
    { 
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TagInputComponent),
      multi: true
    }
]
})

export class TagInputComponent implements ControlValueAccessor, OnChanges {

    tags: Tag[];
    @Input() availableTags: Tag[];
    tagOptions: Option<Tag>[] = [];
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Output() onChange: EventEmitter<Tag[]> = new EventEmitter<Tag[]>();

    writeValue(obj: any): void {
        this.tags = obj;
        if (this.tags && this.availableTags) {
            this.updateOptions();
        }
    }
    
    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.availableTags && this.tags) {
            this.updateOptions();
        }
    }
    
    onTagAdd(tag: Tag) {
        if (this.tags.findIndex(element => element.id == tag.id) == -1) {
            this.tags.push(tag);
            this.tagOptions = this.tagOptions.filter(opt => opt.value.id != tag.id);
            this.onChangeCallback(this.tags);
            this.onChange.emit(this.tags);
            this.onTouchedCallback();
        }
    }

    public deleteTag(tag: Tag) {
        this.tags.splice(this.tags.indexOf(tag), 1);
        let option: Option<Tag> = new Option(tag, tag.name);
        option.color = tag.color;
        this.tagOptions.push(option);
        this.tagOptions.sort(this.sortTags);
        this.onChangeCallback(this.tags);
        this.onChange.emit(this.tags);
        this.onTouchedCallback();
    }

    private updateOptions() {
        this.tagOptions = this.availableTags.reduce((options, tag) => {
            if (!this.tags.find(ssTag => ssTag.id == tag.id)) {
                let option: Option<Tag> = new Option(tag, tag.name);
                option.color = tag.color;
                options.push(option);
            }
            return options;
        }, []);
        this.tagOptions.sort(this.sortTags);
    }

    private sortTags(o1, o2): number {
        return o1.value.id - o2.value.id;
    }

    getFontColor(colorInp: string): boolean {
        var color = (colorInp.charAt(0) === '#') ? colorInp.substring(1, 7) : colorInp;
        var r = parseInt(color.substring(0, 2), 16); // hexToR
        var g = parseInt(color.substring(2, 4), 16); // hexToG
        var b = parseInt(color.substring(4, 6), 16); // hexToB
        return (((r * 0.299) + (g * 0.587) + (b * 0.114)) < 186);
  }
}