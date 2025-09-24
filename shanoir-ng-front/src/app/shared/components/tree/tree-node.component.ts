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
    ChangeDetectorRef,
    Component,
    EventEmitter,
    forwardRef,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import { QualityTag } from 'src/app/study-cards/shared/quality-card.model';

import { CheckboxComponent } from '../../checkbox/checkbox.component';
import { Tag } from '../../../tags/tag.model';
import { isDarkColor } from '../../../utils/app.utils';

export const CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR: any = {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TreeNodeComponent),
    multi: true
};

@Component({
    selector: 'node',
    templateUrl: 'tree-node.component.html',
    styleUrls: ['tree-node.component.css'],
    providers: [CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR],
    standalone: false
})

export class TreeNodeComponent implements ControlValueAccessor, OnChanges {

    @Input() label: string;
    @Input() pictoUrl: string;
    @Input() awesome: string;
    @Input() deploy: boolean;
    @Input() hasBox: boolean;
    @Input() editable: boolean = false;
    @Input() tooltip: string;
    @Input() hasChildren: boolean | 'unknown' = 'unknown';
    @Input() clickable: boolean;
    @Input() buttonPicto: string;
    @Input() dataLoading: boolean = false;
    @Input() title: string;
    @Input() tags: Tag[];
    @Input() qualityTag: QualityTag;
    @Input() route: string;
    @Input() downloadable: boolean = true;
    public isOpen: boolean = false;
    @Input() opened: boolean = false;
    private neverOpened: boolean = true;
    @Output() openedChange: EventEmitter<boolean> = new EventEmitter();
    public checked: boolean | 'indeterminate';
    @ViewChild('box') boxElt: CheckboxComponent;
    @Output() labelChange = new EventEmitter();
    @Output() labelClick = new EventEmitter();
    @Output() chkbxChange = new EventEmitter();
    @Output() firstOpen = new EventEmitter();
    @Output() buttonClick = new EventEmitter();
    private onTouchedCallback: () => void = () => { return; };
    private onChangeCallback: (_: any) => void = () => { return; };

    constructor(private cdr: ChangeDetectorRef) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.opened) {
            if (!this.opened && this.isOpen) {
                setTimeout(() => {
                    this.close();
                });
            } else if (this.opened && !this.isOpen) {
                setTimeout(() => {
                    this.open();
                });
            }
        }
    }

    ngAfterViewInit() {
        if (this.deploy) {
            this.deployAll();
        }
    }

    public deployAll() {
        this.open();
        this.cdr.detectChanges();
    }

    public isClickable(): boolean {
        if (this.clickable != undefined) return this.clickable;
        else if (this.labelClick.observers.length > 0) {
            return true;
        }
        return false;
    }

    public open() {
        this.dataLoading = false;
        this.isOpen = true;
        this.openedChange.emit(this.isOpen);
        if (this.hasChildren == 'unknown' || this.neverOpened) {
            this.neverOpened = false;
            this.firstOpen.emit(this);
        }
    }

    public close() {
        this.isOpen = false;
        this.openedChange.emit(this.isOpen);
    }

    public toggle() {
        if (this.isOpen) this.close();
        else {
            this.open();
        }
    }

    get value(): boolean | 'indeterminate' {
        return this.checked;
    };

    set value(value: boolean | 'indeterminate') {
        if (value !== this.checked) {
            this.checked = value;
            this.onChangeCallback(value);
        }
    }

    getFontColor(colorInp: string): boolean {
        return isDarkColor(colorInp);
    }

    //From ControlValueAccessor interface
    writeValue(value: any) {
        if (value !== this.checked) {
            this.checked = value;
            //this.chkbxChange.emit(value);
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
