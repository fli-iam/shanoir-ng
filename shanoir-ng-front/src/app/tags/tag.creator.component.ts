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
import { Component, EventEmitter, forwardRef, Input, Output, ViewChild } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { AbstractInput } from '../shared/form/input.abstract';
import { isDarkColor } from '../utils/app.utils';
import { Tag } from './tag.model';


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
    ],
    standalone: false
})

export class TagCreatorComponent extends AbstractInput<Tag[]> {
    @ViewChild('input', { static: false }) input: any;
    @Input() tagsInUse: Tag[];
    @Input() mode: Mode;
    @Output() userChange: EventEmitter<any> = new EventEmitter();
    selectedColor: string;
    text: string = null;
    addTagVisible: boolean = false;
    message: string = "";
    displayedTags: Set<{tag: Tag, darkFont: boolean}>;
    newTagDarkFont: boolean;

    constructor(private dialogService: ConfirmDialogService) {
        super();
        this.selectedColor = '#' + Math.floor(Math.random()*16777215).toString(16); // random color
        this.onColorChange();
    }

    focus() {
        setTimeout(() => this.input.nativeElement.focus());
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
                this.selectedColor = '#' + Math.floor(Math.random()*16777215).toString(16).padStart(6, '0'); // random color
                this.addTagVisible = false;
                this.displayedTags.add({tag: newTag, darkFont: isDarkColor(newTag.color)});
                this.propagateChange(this.model);
                this.userChange.emit(this.model);
            }
        }
    }

    public deleteTag(tag: {tag: Tag, darkFont: boolean}) {
        if (this.tagUsed(tag.tag)) {
            this.dialogService.inform('Cannot delete!', 'Sorry, this tag is currently linked to one or more subject(s) in this study.');
            return;
        } else {
            this.model.splice(this.model.indexOf(tag.tag), 1);
            this.displayedTags.delete(tag)
            this.propagateChange(this.model);
            this.userChange.emit(this.model);
        }
    }

    private tagUsed(tag: Tag) {
        return !!this.tagsInUse?.find(ssTag => ssTag.equals(tag));
    }

    writeValue(obj: any): void {
        super.writeValue(obj);
        this.displayedTags = new Set();
        if (this.model) {
            (this.model as Tag[]).forEach(tag =>
                this.displayedTags.add({tag: tag, darkFont: isDarkColor(tag.color)})
            );
        }
    }

    onColorChange() {
        this.newTagDarkFont = isDarkColor(this.selectedColor);
    }
}
