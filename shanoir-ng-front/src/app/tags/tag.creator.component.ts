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

import { Component, Input, forwardRef, OnChanges, SimpleChanges } from '@angular/core'; // First, import Input
import { FormGroup } from '@angular/forms';

import { Tag } from "./tag.model";
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { Study } from '../studies/shared/study.model';
import { AbstractInput } from '../shared/form/input.abstract';

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
    @Input() study: Study[];
    @Input() mode: Mode;
    selectedColor: string = null;
    text: string = null;
    addTagVisible: boolean = false;

    colors: string[] = [
        "#FF0000", "#FFFFFF",
        "#00FFFF", "#C0C0C0",
        "#0000FF", "#808080",
        "#00008B", "#000000",
        "#ADD8E6", "#FFA500",
        "#800080", "#A52A2A",
        "#FFFF00", "#800000",
        "#00FF00", "#008000",
        "#FF00FF", "#808000",
        "#FFC0CB", "#7FFD4"
    ]
    
    public toogle() {
        this.addTagVisible = !this.addTagVisible;
    }

    public addTag() {
        let newTag = new Tag();
        newTag.color = this.selectedColor;
        newTag.name = this.text;
        this.model.push(newTag);
        this.text = null;
        this.selectedColor = null;
        this.toogle();
        this.propagateChange(this.model);
    }

    public deleteTag(tag: Tag) {
        this.model.splice(this.model.indexOf(tag), 1);
        this.propagateChange(this.model);
    }

    ngOnChanges(changes: SimpleChanges): void {
        // Remove color from available list ?
    }

    getFontColor(colorInp: string): boolean {
          var color = (colorInp.charAt(0) === '#') ? colorInp.substring(1, 7) : colorInp;
          var r = parseInt(color.substring(0, 2), 16); // hexToR
          var g = parseInt(color.substring(2, 4), 16); // hexToG
          var b = parseInt(color.substring(4, 6), 16); // hexToB
          return (((r * 0.299) + (g * 0.587) + (b * 0.114)) < 186);
    }

    onChange() {
        this.propagateChange(this.model);
    }

    onTouch() {
        this.propagateTouched();
    }
}