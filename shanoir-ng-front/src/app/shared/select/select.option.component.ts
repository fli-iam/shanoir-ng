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

import { Component, ElementRef, Input } from '@angular/core';

import { SelectBoxComponent } from './select.component';

@Component({
    selector: 'select-option',
    template: `
        <div (click)="select()" (mouseenter)="over()" [class.selected]="selected" [class.disabled]="disabled" [class.focus]="focus">
            <ng-content></ng-content>
        </div>
    `,
    styles: [
        ':host() { height: 20px; display:block; border: none; white-space: nowrap; color: var(--dark-grey); }',
        'div { padding: 0 5px; }',
        'div.focus { background-color: var(--grey); }',
        'div.selected { background-color: var(--color-b-light); }',
        'div.disabled { opacity: 0.5; }'
    ]
    
})

export class SelectOptionComponent {
    
    @Input() value: any;
    @Input() disabled: any;
    public parent: SelectBoxComponent;
    public selected: boolean = false;
    public focus: boolean = false;

    constructor(public elt: ElementRef) { }

    select() {
        if (!this.disabled) this.parent.onSelectedOptionChange(this);
    }

    over() {
        if (!this.disabled) this.parent.onOptionOver(this);
    }

}