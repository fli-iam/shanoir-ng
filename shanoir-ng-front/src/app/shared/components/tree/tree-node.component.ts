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

import {
    Component, Input, Output, ContentChildren, forwardRef, QueryList, ChangeDetectorRef,
    EventEmitter, ViewChild, ElementRef
} from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component'
import { ImagesUrlUtil } from '../../utils/images-url.util';
import { CheckboxComponent } from '../../checkbox/checkbox.component';

const noop = () => {
};

export const CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR: any = {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TreeNodeComponent),
    multi: true
};

@Component({
    selector: 'node',
    templateUrl: 'tree-node.component.html',
    styleUrls: ['tree-node.component.css'],
    providers: [CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR]
})

export class TreeNodeComponent implements ControlValueAccessor {

    @Input() label: string;
    @Input() pictoUrl: string;
    @Input() awesome: string;
    @Input() deploy: boolean;
    @Input() hasBox: boolean;
    @Input() nodeParams: any;
    @Input() editable: boolean = false;
    @Input() tooltip: string;
    @Input() dataRequest: boolean = false;
    @Input() buttonPicto: string = null;
    @ContentChildren(forwardRef(() => TreeNodeComponent)) childNodes: QueryList<any>;
    @ContentChildren(forwardRef(() => DropdownMenuComponent)) menus: QueryList<any>;
    public dataLoading: boolean = false;
    public isOpen: boolean = false;
    public loaded: boolean = false;
    public hasChildren: boolean;
    public checked: boolean | 'indeterminate';
    @ViewChild('box') boxElt: CheckboxComponent;
    @Output() labelChange = new EventEmitter();
    @Output() labelClick = new EventEmitter();
    @Output() chkbxChange = new EventEmitter();
    @Output() openClick = new EventEmitter();
    @Output() buttonClick = new EventEmitter();
    private onTouchedCallback: () => void = noop;
    private onChangeCallback: (_: any) => void = noop;

    constructor(private cdr: ChangeDetectorRef) {

    }

    ngAfterViewInit() {
        if (this.deploy) {
            this.deployAll();
        }
        this.updateChildren();
        this.loaded = true;
        this.cdr.detectChanges();
    }

    public deployAll() {
        this.open();
        this.cdr.detectChanges();
    }

    public isClickable(): boolean {
        if (this.labelClick.observers.length > 0) {
            return true;
        }
        return false;
    }

    public open() {
        this.dataLoading = false;
        this.isOpen = true;
    }

    public close() {
        this.isOpen = false;
    }

    public toggle() {
        if (this.isOpen) this.close();
        else {
            if (!this.hasChildren && this.dataRequest) this.openClick.emit(this);
            this.open();
        }
    }

    public updateChildren(): void {
        this.hasChildren = this.childNodes.toArray().length > 1; // TODO : set to 0 when the bug is fixed https://github.com/angular/angular/issues/10098
        this.childNodes.forEach((child, index) => {
            if (index != 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                child.notifyParent = this.updateSelf;
            }
        });
    }

    get value(): boolean | 'indeterminate' {
        return this.checked;
    };

    set value(value: boolean | 'indeterminate') {
        if (value !== this.checked) {
            this.checked = value;
            this.onChangeCallback(value);
            this.childNodes.forEach((node, index) => {
                if (index != 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                    node.value = value;
                }
            });
            if (this.notifyParent != undefined) this.notifyParent();
        }
    }

    private notifyParent: () => void;

    private updateSelf = () => {
        let allOn: boolean = true;
        let allOff: boolean = true;
        this.childNodes.forEach((child, index) => {
            if (index != 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                if (!child.checked) {
                    allOn = false;
                } else {
                    allOff = false;
                }
            }
        });
        if (allOff) this.setBox(false);
        else if (allOn) this.setBox(true);
        else this.setBox('indeterminate');
    };

    setBox(value: boolean | 'indeterminate') {
        if (this.boxElt) this.boxElt.model = value;
        this.writeValue(value != null && value);
    }

    //From ControlValueAccessor interface
    writeValue(value: any) {
        if (value !== this.checked) {
            this.checked = value;
            if (this.notifyParent != undefined) this.notifyParent();
        }
    }

    //From ControlValueAccessor interface
    registerOnChange(fn: any) {
        this.onChangeCallback = fn;
    }

    //From ControlValueAccessor interface
    registerOnTouched(fn: any) {
        this.onTouchedCallback = fn;
    }
} 