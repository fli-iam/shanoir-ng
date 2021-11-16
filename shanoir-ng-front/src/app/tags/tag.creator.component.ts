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

import { Component, Input, forwardRef, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core'; // First, import Input

import { Tag } from "./tag.model";
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { Study } from '../studies/shared/study.model';
import { AbstractInput } from '../shared/form/input.abstract';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';

export type Mode =  "view" | "edit" | "create";
@Component({
    selector: 'tag-creator',
    templateUrl: 'tag.creator.component.html',
    styleUrls: ['tag.creator.component.css'],
    providers: [
    { 
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TagCreatorComponent),
      multi: true
    }
]
})

export class TagCreatorComponent extends AbstractInput implements OnChanges {
    @Input() study: Study;
    @Input() mode: Mode;
    @Output() onChange: EventEmitter<any> = new EventEmitter();
    selectedColor: string;
    text: string = null;
    addTagVisible: boolean = false;
    displayedTags: Set<{tag: Tag, used: boolean, darkFont: boolean}>;
    newTagDarkFont: boolean;

    constructor(private dialogService: ConfirmDialogService) {
        super();
        this.selectedColor = '#' + Math.floor(Math.random()*16777215).toString(16); // random color
        this.onColorChange();
    }

    public addTag() {
        if (this.text != null && this.selectedColor != null) {
            let newTag = new Tag();
            newTag.color = this.selectedColor;
            newTag.name = this.text;
            if (this.model.find(tag => (tag as Tag).equals(newTag))) {
                this.dialogService.error('Error', 'A tag with this color and name already exist !');
            } else {
                this.model.push(newTag);
                this.text = null;
                this.selectedColor = '#' + Math.floor(Math.random()*16777215).toString(16); // random color
                this.addTagVisible = false;
                this.displayedTags.add({tag: newTag, used: this.tagUsed(newTag), darkFont: this.getFontColor(newTag.color)});
                this.propagateChange(this.model);
                this.onChange.emit(this.model);
            }
        }
    }

    public deleteTag(tag: {tag: Tag, used: boolean, darkFont: boolean}) {
        if (tag.used) {
            this.dialogService.inform('Cannot delete!', 'Sorry, this tag is currently linked to one or more subject(s) in this study.');
            return;
        } else {
            this.model.splice(this.model.indexOf(tag.tag), 1);
            this.displayedTags.delete(tag)
            this.propagateChange(this.model);
            this.onChange.emit(this.model);
        }
    }

    private tagUsed(tag: Tag) {
        for (let subjectStudy of this.study.subjectStudyList) {
            if (subjectStudy.tags.findIndex(element => element.equals(tag)) != -1) {
                return true;
            }
        }
        return false;
    }

    writeValue(obj: any): void {
        super.writeValue(obj);
        this.displayedTags = new Set();
        if (this.model) {
            (this.model as Tag[]).forEach(tag => 
                this.displayedTags.add({tag: tag, used: this.tagUsed(tag), darkFont: this.getFontColor(tag.color)})
            );
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        // ?
    }

    private getFontColor(colorInp: string): boolean {
          var color = (colorInp.charAt(0) === '#') ? colorInp.substring(1, 7) : colorInp;
          var r = parseInt(color.substring(0, 2), 16); // hexToR
          var g = parseInt(color.substring(2, 4), 16); // hexToG
          var b = parseInt(color.substring(4, 6), 16); // hexToB
          return (((r * 0.299) + (g * 0.587) + (b * 0.114)) < 145);
    }

    onColorChange() {
        this.newTagDarkFont = this.getFontColor(this.selectedColor);
    }
}