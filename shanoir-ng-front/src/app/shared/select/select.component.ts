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
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    forwardRef,
    HostBinding,
    HostListener,
    Input,
    OnChanges,
    OnDestroy,
    Output,
    PipeTransform,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { findLastIndex, arraysEqual, objectsEqual } from '../../utils/app.utils';
import { GlobalService } from '../services/global.service';
import { NgIf, NgFor } from '@angular/common';
import { RouterLink } from '@angular/router';


@Component({
    selector: 'select-box',
    templateUrl: 'select.component.html',
    styleUrls: ['select.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SelectBoxComponent),
            multi: true,
        }
    ],
    imports: [NgIf, FormsModule, RouterLink, NgFor]
})

export class SelectBoxComponent implements ControlValueAccessor, OnDestroy, OnChanges, AfterViewInit {

    @Output() userChange = new EventEmitter();
    @Output() selectOption = new EventEmitter();
    @Output() deSelectOption = new EventEmitter();
    @Output() userClear = new EventEmitter();
    @Input() options: Option<any>[];
    @Input() optionArr: any[];
    @Input() optionBuilder: { list: any[], labelField: string, getLabel: (any) => string };
    @Input() pipe: PipeTransform;
    @Input() validateChange: (any) => Promise<boolean>;
    public displayedOptions: Option<any>[] = [];
    @ViewChild('input', { static: false }) textInput: ElementRef;
    @ViewChild('hiddenOption', { static: false }) hiddenOption: ElementRef;
    inputValue: any;
    private _selectedOptionIndex: number = null;
    public focusedOptionIndex: number;
    private _firstScrollOptionIndex: number;
    private filteredOptions: FilteredOptions;
    private globalClickSubscription: Subscription;
    private optionChangeSubscription: Subscription;
    public way: 'up' | 'down' = 'down';
    public hideToComputeHeight: boolean = false;
    private onTouchedCallback = () => { return; };
    @Output() touch = new EventEmitter();
    private onChangeCallback: (any) => void = () => { return; };
    public inputText: string;
    private _searchText: string = null;
    @Input() disabled: boolean = false;
    @HostBinding('class.read-only') @Input() readOnly: boolean = false;
    @Input() placeholder: string;
    public maxWidth: number;
    public noResult: boolean;
    @Input() clear: boolean = true;
    @Input() search: boolean = true;
    @Input() selectionColor: boolean = false;

    @Input() viewDisabled: boolean;
    @Input() viewHidden: boolean;
    @Input() newDisabled: boolean;
    @Input() newHidden: boolean;
    @Input() addDisabled: boolean;
    @Input() addHidden: boolean;
    @Input() viewRoute: string;
    @Input() newRoute: string;
    @Output() viewClick = new EventEmitter();
    @Output() newClick = new EventEmitter();
    @Output() addClick: EventEmitter<any> = new EventEmitter();
    @HostBinding('class.compact') @Input() compactMode: boolean = false;

    readonly LIST_LENGTH: number = 16;

    constructor(
            private element: ElementRef, 
            private globalService: GlobalService) {}

    ngAfterViewInit(): void {
        this.computeMinWidth();
    }

    ngOnDestroy() {
        this.unsubscribeToGlobalClick();
        if (this.optionChangeSubscription) this.optionChangeSubscription.unsubscribe();
        if (this.scrollButtonInterval) clearInterval(this.scrollButtonInterval);
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.options && !arraysEqual(changes.options?.currentValue, changes.options?.previousValue)) {
            if (!this.options) this.options = [];
            this.searchText = null;
            this.computeDisplayedOptions();
            this.initSelectedOption();
            this.computeMinWidth();
        } else if (changes.optionArr && !arraysEqual(changes.optionArr?.currentValue, changes.optionArr?.previousValue)) {
            if (!this.optionArr) this.optionArr = [];
            this.searchText = null;
            this.options = [];
            this.optionArr.forEach(item => {
                const label: string = this.getLabel(item);
                const newOption: Option<any> = new Option<any>(item, label);
                if (item.color) newOption.color = item.color;
                if (item.backgroundColor) newOption.color = item.backgroundColr;
                this.options.push(newOption);
            });
            this.initSelectedOption();
            this.computeMinWidth();
        } else if (changes.optionBuilder && this.optionBuilder && changes.optionBuilder.currentValue != changes.optionBuilder.previousValue ) {
            this.searchText = null;
            if (this.optionBuilder.list) {
                this.options = [];
                this.optionBuilder.list.forEach(item => {
                    let label: string = '';
                    if (this.optionBuilder.getLabel) {
                        label = this.optionBuilder.getLabel(item);
                    }
                    else if (this.optionBuilder.labelField) {
                        label = item[this.optionBuilder.labelField];
                    } 
                    this.options.push(new Option<any>(item, label));
                });
                this.initSelectedOption();
                this.computeMinWidth();
            }
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

    private initSelectedOption() {
        if (this.selectedOption || this.inputValue) {
            let index: number;
            if (this.selectedOption) {
                index = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, this.selectedOption.value));
            } else {
                index = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, this.inputValue));
                this.inputValue = null;
            }
            if (index == -1) {
                this._selectedOptionIndex = null;
            } else {
                this._selectedOptionIndex = index;
                this.inputText = this.options[index].label;
            }
        } else {
            this._selectedOptionIndex = null;
        }
    }

    writeValue(value: any): void {
        this.searchText = null;
        this.inputValue = value;
        if (value && this.options?.length > 0) {
            const index = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, value))
            if (index > -1) {
                this.selectedOptionIndex = index;
            } else {
                this.selectedOptionIndex = null;
            }
        } else {
            this.selectedOptionIndex = null;
        }
        this.inputText = this.selectedOption?.label;
    }

    public get selectedOption(): Option<any> {
        return this.options 
            && this.selectedOptionIndex != null 
            && this.selectedOptionIndex != undefined
            && this.options[this.selectedOptionIndex] ? this.options[this.selectedOptionIndex] : (
                this.inputValue ? new Option(this.inputValue, this.getLabel(this.inputValue)) : null);
    }

    private get selectedOptionIndex(): number {
        return this._selectedOptionIndex;
    }
    
    private set selectedOptionIndex(index: number) {
        const previousOption: Option<any> = this.selectedOption;
        if (index == -1) index = null;
        if (index != this._selectedOptionIndex) {
            this._selectedOptionIndex = index;
            if (this.selectedOption) {
                this.inputText = this.selectedOption.label;
                this.onChangeCallback(this.selectedOption.value);
                this.selectOption.emit(this.selectedOption);
            } else {
                this.inputText = null;
                this.onChangeCallback(null);
                this.selectOption.emit(null);
            }
            if (previousOption) {
                this.deSelectOption.emit(previousOption);
            }
        }
    }
    
    private hasSelectedOption(): boolean {
        return this.selectedOptionIndex != undefined && this.selectedOptionIndex != null;
    }
    
    private unSelectOption() {
        if (this.selectedOptionIndex != undefined && this.selectedOptionIndex != null) {
            this.selectedOptionIndex = null;
        }
        this.firstScrollOptionIndex = 0;
        this.focusedOptionIndex = null;
        this.inputValue = null;
    }

    private computeMinWidth() {
        let maxOption: Option<any>;
        let maxWidth: number = 0;
        if (this.displayableOptions && (this.displayableOptions.length > 0) && this.hiddenOption) {
            this.displayableOptions.forEach(opt => {
                if (!maxOption || !maxOption.label || !opt.label || opt.label.length > maxOption.label.length - (maxOption.label.length * 0.1)) {
                    this.hiddenOption.nativeElement.innerText = opt.label;
                    const width: number = this.hiddenOption.nativeElement.offsetWidth;
                    if (width > maxWidth) {
                        maxWidth = width;
                        maxOption = opt;
                    }
                }
            })
            this.hiddenOption.nativeElement.innerText = maxOption?.label;
            this.maxWidth = maxWidth;
            this.hiddenOption.nativeElement.innerText = '';
        }
    }

    private set searchText(text: string) {
        if (text == this._searchText) return;
        this.focusedOptionIndex = null;
        if (!text) {
            this._searchText = null;
            this.filteredOptions = null;
        } else {
            const trimmed = text.trim().toLowerCase();
            if (trimmed == this._searchText) return;
            this._searchText = trimmed;
            if (this.searchText.length <= 0) {
                this.filteredOptions = null;
            } else if (!this.filteredOptions || this.filteredOptions.searchText != this.searchText) {
                this.filteredOptions = new FilteredOptions(this.searchText, this.options);
            }
        }
        this.computeMinWidth();
        this.computeDisplayedOptions();
    }

    private get searchText(): string {
        return this._searchText;
    }
    
    private computeDisplayedOptions() {
        if (this.firstScrollOptionIndex == undefined || this.firstScrollOptionIndex == null || !this.options) {
            this.displayedOptions = [];
            this.noResult = false;
            return;
        }
        this.displayedOptions = this.displayableOptions.slice(this.firstScrollOptionIndex, this.firstScrollOptionIndex + this.LIST_LENGTH);
        this.displayedOptions = this.displayedOptions.sort((a, b) => {
            if (a.section == b.section) return 0;
            else return ((a.section || 0) > (b.section || 0)) ? -1 : 1;
        });
        this.noResult = this.displayedOptions.length == 0;
    }
    
    public onUserSelectedOption(option: Option<any>, event) {
        event.stopPropagation();
        if (option.disabled) return;
        const index: number = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, option.value));
        this.onUserSelectedOptionIndex(index);
    }
    
    public onUserSelectedOptionIndex(index: number) {
        const promise: Promise<boolean> = this.validateChange ? this.validateChange(this.selectedOption?.value) : Promise.resolve(true);
        promise.then(valid => {
            if (valid) {
                this.searchText = null;
                this.element.nativeElement.focus();
                this.selectedOptionIndex = index;
                this.close();
                this.onChangeCallback(this.selectedOption ? this.selectedOption.value : null);
                this.userChange.emit(this.selectedOption ? this.selectedOption.value : null);
                this.selectOption.emit(this.selectedOption);
            }
        });
    }

    public isOptionSelected(option: Option<any>) {
        return this.hasSelectedOption() && this.valuesEqual(this.selectedOption.value, option.value);
    }

    private valuesEqual(value1, value2) {
        return objectsEqual(value1, value2);
    }

    public open() {
        if (this.isOpen()) return;
        if (this.options) {
            this.subscribeToGlobalClick();
            this.scrollToSelected();
            this.chooseOpeningWay();
        }  
    }

    public close() {
        this.unsubscribeToGlobalClick();
        this.firstScrollOptionIndex = null;
        this.focusedOptionIndex = null;
    }
    
    public isOpen(): boolean {
        return this.firstScrollOptionIndex != undefined && this.firstScrollOptionIndex != null;
    }

    private set firstScrollOptionIndex(index: number) {
        this._firstScrollOptionIndex = index;
        this.computeDisplayedOptions();
    }

    private get firstScrollOptionIndex(): number {
        return this._firstScrollOptionIndex;
    }
    
    private scrollToSelected() {
        if (this.scrollable && this.selectedOptionIndex) {
            if (this.selectedOptionIndex < this.displayableOptions.length - this.LIST_LENGTH) {
                this.firstScrollOptionIndex = this.selectedOptionIndex;
            } else  {
                this.firstScrollOptionIndex = this.displayableOptions.length - this.LIST_LENGTH;
            }
        } else {
            this.firstScrollOptionIndex = 0;
        }
    }

    chooseOpeningWay() {
        this.hideToComputeHeight = true;
        this.way = 'down'
        setTimeout(() => {
            const listElt = this.element.nativeElement.querySelector('.list');
            if (!listElt) return;
            const bottom = listElt.getBoundingClientRect().bottom;
            const docHeight: number = document.body.clientHeight;
            if (bottom > docHeight) this.way = 'up';
            this.hideToComputeHeight = false;
        });
    }

    private subscribeToGlobalClick() {
        this.globalClickSubscription = this.globalService.onGlobalClick.subscribe((clickEvent: Event) => {
            if (!this.element.nativeElement.contains(clickEvent.target)) {
                this.close();
            }
        });
    }

    private unsubscribeToGlobalClick() {
        if (this.globalClickSubscription) this.globalClickSubscription.unsubscribe();
    }

    onWheel(event) {
        if (event.wheelDelta < 0 && (this.firstScrollOptionIndex < this.length - this.LIST_LENGTH)) {
            this.firstScrollOptionIndex ++;
        }
        if (event.wheelDelta > 0 && this.firstScrollOptionIndex > 0) {
            this.firstScrollOptionIndex --;
        }
        event.preventDefault();
      }

    @HostListener('keydown', ['$event']) 
    public onKeyPress(event: any) {
        if (this.readOnly) return;
        if ('ArrowDown' == event.key) {
            this.scrollDownByOne();
            event.preventDefault();
        } else if ('ArrowUp' == event.key) {
            this.scrollUpByOne();
            event.preventDefault();
        } else if ('PageUp' == event.key) {
            if (this.isOpen()) this.pageUp();
        } else if ('PageDown' == event.key) {
            if (this.isOpen()) this.pageDown();     
        } else if ('Enter' == event.key) {
            if (!this.isOpen() && !this.disabled) {
                this.open();
            } else {
                if (this.focusedOptionIndex != null) {
                    this.onUserSelectedOption(this.displayedOptions[this.focusedOptionIndex], event);
                }
            }
            event.preventDefault();
        } else if (' ' == event.key) {
            if (!this.isOpen()) this.open();
        }  else if (this.search && event.keyCode >= 65 && event.keyCode <= 90 && this.textInput) {
            if (this.textInput.nativeElement != document.activeElement) {
                this.inputText = null;
                this.textInput.nativeElement.focus();
            }
        }       
    }

    private scrollDownByOne() {
        if (this.isOpen()) {
            const nextIndexStart: number = (this.focusedOptionIndex != null && this.focusedOptionIndex != undefined) ? (this.focusedOptionIndex + this.firstScrollOptionIndex + 1) : 0;
            const nextIndex: number = this.displayableOptions.slice(nextIndexStart).findIndex(opt => {return !opt.disabled});
            if (nextIndex == -1) return;
            const nbSteps: number = (this.focusedOptionIndex != null && this.focusedOptionIndex != undefined) ? nextIndex + 1 : 0;
            if (this.scrollable && this.focusedOptionIndex + nbSteps >= this.LIST_LENGTH) {
                this.firstScrollOptionIndex += nbSteps;
                this.focusedOptionIndex = this.LIST_LENGTH - 1;
            } else {
                this.focusedOptionIndex += nbSteps;
            }
        } else {
            const nextIndexStart: number = (this.selectedOptionIndex != null && this.selectedOptionIndex != undefined) ? this.selectedOptionIndex + 1 : 0;
            const nextIndex: number = this.displayableOptions.slice(nextIndexStart).findIndex(opt => {return !opt.disabled});
            if (nextIndex == -1) return;
            else this.onUserSelectedOptionIndex(nextIndexStart + nextIndex);
        }
    }

    private scrollUpByOne() {
        if (this.isOpen()) {
            const nextIndexStart: number = (this.focusedOptionIndex != null && this.focusedOptionIndex != undefined) ? (this.focusedOptionIndex + this.firstScrollOptionIndex - 1) : 0;
            const nextIndex: number = findLastIndex(this.displayableOptions.slice(0, nextIndexStart + 1), opt => {return !opt.disabled});
            if (nextIndex == -1) return;
            const nbSteps: number = (this.focusedOptionIndex != null && this.focusedOptionIndex != undefined) ? this.focusedOptionIndex + this.firstScrollOptionIndex - nextIndex : 0;
            if (this.scrollable && this.focusedOptionIndex - nbSteps < 0) {
                this.firstScrollOptionIndex -= nbSteps;
                this.focusedOptionIndex = 0;
            } else {
                this.focusedOptionIndex -= nbSteps;
            }
        } else {
            const nextIndexStart: number = (this.selectedOptionIndex != null && this.selectedOptionIndex != undefined) ? this.selectedOptionIndex - 1 : 0;
            const nextIndex: number = findLastIndex(this.displayableOptions.slice(0, nextIndexStart + 1), opt => {return !opt.disabled});
            if (nextIndex == -1) return;
            else this.onUserSelectedOptionIndex(nextIndex);
        }
    }

    private scrollButtonInterval;
    private scrollButtonTime: number;

    scrollButtonOn(way: 'down' | 'up') {
        const now: number = Date.now();
        this.scrollButtonTime = now;
        if (way == 'down') {
            if (this.firstScrollOptionIndex < this.displayableOptions.length - this.LIST_LENGTH) {
                this.firstScrollOptionIndex ++;
            }
        } else if (way == 'up') {
            if (this.firstScrollOptionIndex > 0) {
                this.firstScrollOptionIndex --;
            }
        }
        setTimeout(() => {
            if (this.scrollButtonTime == now) {
                this.scrollButtonInterval = setInterval(() => {
                    if (way == 'down') {
                        if (this.firstScrollOptionIndex < this.displayableOptions.length - this.LIST_LENGTH) {
                            this.firstScrollOptionIndex ++;
                        }
                    } else if (way == 'up') {
                        if (this.firstScrollOptionIndex > 0) {
                            this.firstScrollOptionIndex --;
                        }
                    }
                }, 50);
            }
        }, 500)
    }

    scrollButtonOff() {
        this.scrollButtonTime = null;
        if (this.scrollButtonInterval) clearInterval(this.scrollButtonInterval);
    }

    private dragStartOffsetY: number;
    public dragging: boolean = false;
    onDragStart(event) {
        this.dragging = true;
        const img = new Image();
        img.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=';
        event.dataTransfer.setDragImage(img, 0, 0);
        this.dragStartOffsetY = event.clientY - event.srcElement.getBoundingClientRect().top;
    }

    private lastDropY: number = 0;
    private lastScroll: number = 0;
    allowDrop(event) {
        const dropY: number = event.clientY;
        if (dropY == this.lastDropY || (Date.now() - this.lastScroll) < 30) return;
        this.lastDropY = dropY;
        this.lastScroll = Date.now();
        const listElt = this.element.nativeElement.querySelector('.list');
        const listTop: number = listElt.getBoundingClientRect().top + 10;
        const listHeight: number = listElt.getBoundingClientRect().height - 20;
        const relativeDropY = Math.max(0, dropY - listTop - this.dragStartOffsetY);
        
        this.firstScrollOptionIndex = 
        Math.min(
            Math.round((this.displayableOptions.length - this.LIST_LENGTH) * (relativeDropY / (listHeight - 50))), 
            this.displayableOptions.length - this.LIST_LENGTH
            );
    }

    onScrollZoneClick(event) {
        if (event.srcElement.classList.contains('scrollzone')) {
            if (event.clientY < this.liftHeight + event.srcElement.getBoundingClientRect().top) {
                this.pageUp();
            } else {
                this.pageDown();
            }
        }
    }

    private pageUp() {
        this.focusedOptionIndex = null;
        if (this.firstScrollOptionIndex - this.LIST_LENGTH >= 0) {
            this.firstScrollOptionIndex -= this.LIST_LENGTH;
        } else {
            this.firstScrollOptionIndex = 0;
        }
    }

    private pageDown() {
        this.focusedOptionIndex = null;
        if (this.firstScrollOptionIndex + 2 * this.LIST_LENGTH <= this.displayableOptions.length) {
            this.firstScrollOptionIndex += this.LIST_LENGTH;
        } else {
            this.firstScrollOptionIndex = this.displayableOptions.length - this.LIST_LENGTH;
        }
    }

    onClear() {
        this.userClear.emit(this.selectedOption);
        this.onTypeText(null);
    }

    public onTypeText(text: string) {
        this.unSelectOption();
        this.onChangeCallback(null);
        this.userChange.emit(null);
        this.inputText = text;
        this.searchText = text;
        this.open();
    }

    public onInputFocus() {
        this.textInput?.nativeElement.select();
    }    
    
    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    @HostListener('focusout', ['$event']) 
    onFocusOut(event: FocusEvent) {
        if (!this.element.nativeElement.contains(event.relatedTarget) && !this.dragging) {
            this.close();
            this.onTouchedCallback();
            this.touch.emit();
        } 
    }

    @HostBinding('attr.tabindex')
    get tabindex(): number {
        return this.disabled ? undefined : 0;
    } 

    clickView(): void {
        if(!this.viewDisabled && this.selectedOption) this.viewClick.emit(this.selectedOption.value);
    }

    clickNew(): void {
        if(!this.newDisabled) this.newClick.emit();
    }

    clickAdd(): void {
        if(!this.addDisabled && this.selectedOption && !this.selectedOption.disabled) this.addClick.emit(this.selectedOption.value);
    }

    setDisabledState(isDisabled: boolean) {
        this.disabled = isDisabled;
    }

    public get length(): number {
        const opt: Option<any>[] = this.displayableOptions;
        return opt ? opt.length : 0;
    }

    public get scrollable(): boolean {
        return this.length > this.LIST_LENGTH;
    }

    public get liftHeight(): number {
        return this.length > 0 ? 250 * this.firstScrollOptionIndex / (this.length - this.LIST_LENGTH) : 0;
    }

    private get displayableOptions(): Option<any>[] {
        if (this.filteredOptions) return this.filteredOptions.filteredList;
        else if (this.options) return this.options;
        else return null;
    }
}


export class Option<T> {

    compatible: boolean = undefined;
    backgroundColor: string;
      
    constructor(
        public value: T,
        public label: string,
        public section?: string,
        public color?: string,
        public awesome?: string,
        public disabled: boolean = false) {}

    clone(): Option<T> {
        const option: Option<T> = new Option(this.value, this.label, this.section);
        option.disabled = this.disabled;
        option.compatible = this.compatible;
        option.color = this.color;
        option.backgroundColor = this.backgroundColor;
        return option;
    }
}

export class FilteredOptions {

    private _filteredList: Option<any>[];

    constructor( 
            private _searchText: string,
            options: Option<any>[]) {
        this._filteredList = options.filter(option => option.label.toLowerCase().includes(_searchText));
    } 

    public get filteredList(): Option<any>[] {
        return this._filteredList;
    }

    public get searchText(): string {
        return this._searchText;
    }

    public get length(): number {
        return this.filteredList ? this.filteredList.length : 0;
    }

    
}