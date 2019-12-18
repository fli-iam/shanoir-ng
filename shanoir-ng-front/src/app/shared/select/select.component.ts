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
    SimpleChanges,
    ViewChild,
    PipeTransform,
    NgZone,
    ChangeDetectionStrategy,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subscription } from 'rxjs';

import { GlobalService } from '../services/global.service';


@Component({
    selector: 'select-box',
    templateUrl: 'select.component.html',
    styleUrls: ['select.component.css'],
    //changeDetection: ChangeDetectionStrategy.,
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => SelectBoxComponent),
          multi: true,
        }]   
})

export class SelectBoxComponent implements ControlValueAccessor, OnDestroy, OnChanges {

    @Output() change = new EventEmitter();
    @Input() options: Option<any>[];
    @Input() optionArr: any[];
    @Input() optionBuilder: { list: any[], labelField: string, getLabel: (any) => string };
    @Input() pipe: PipeTransform;
    private displayedOptions: Option<any>[] = [];
    @ViewChild('input') textInput: ElementRef;
    @ViewChild('hiddenOption') hiddenOption: ElementRef;
    private _selectedOptionIndex: number;
    private focusedOptionIndex: number;
    private _firstScrollOptionIndex: number;
    private filteredOptions: FilteredOptions;
    private globalClickSubscription: Subscription;
    private optionChangeSubscription: Subscription;
    private way: 'up' | 'down' = 'down';
    private hideToComputeHeight: boolean = false;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    private inputText: string;
    private _searchText: string;
    @Input() disabled: boolean = false;
    @Input() placeholder: string;
    private maxWidth: number;

    @Input() viewDisabled: boolean;
    @Input() viewHidden: boolean;
    @Input() newDisabled: boolean;
    @Input() newHidden: boolean;
    @Input() addDisabled: boolean;
    @Input() addHidden: boolean;
    @Output() onViewClick = new EventEmitter();
    @Output() onNewClick = new EventEmitter();
    @Output() onAddClick = new EventEmitter();

    readonly LIST_LENGTH: number = 16;

    constructor(
            private element: ElementRef, 
            private globalService: GlobalService,
            private ngZone: NgZone) {}

    ngOnDestroy() {
        this.unsubscribeToGlobalClick();
        if (this.optionChangeSubscription) this.optionChangeSubscription.unsubscribe();
        if (this.scrollButtonInterval) clearInterval(this.scrollButtonInterval);
    }

    ngOnChanges(changes: SimpleChanges) {
        this.searchText = null;
        if (changes.options && this.options && this.selectedOption) {
            this.selectedOptionIndex = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, this.selectedOption.value));
        } else if (changes.optionArr && this.optionArr) {
            this.options = [];
            this.optionArr.forEach(item => {
                let label: string = '';
                if (this.pipe) label = this.pipe.transform(item);
                else if (typeof(item) == 'string') label = item;
                else if (item.label) label = item.label;
                else if (item.name) label = item.name;
                this.options.push(new Option<any>(item, label));
            });
            if (this.selectedOption)
                    this.selectedOptionIndex = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, this.selectedOption.value));
        } else if (changes.optionBuilder && this.optionBuilder) {
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
                if (this.selectedOption)
                    this.selectedOptionIndex = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, this.selectedOption.value));
            }
        }
    }

    writeValue(obj: any): void {
        this.searchText = null;
        if (this.options && this.selectedOption) {
            this.selectedOptionIndex = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, this.selectedOption.value));
        }
    }

    private get selectedOption(): Option<any> {
        return this.options ? this.options[this.selectedOptionIndex] : undefined;
    }

    private get selectedOptionIndex(): number {
        return this._selectedOptionIndex;
    }
    
    private set selectedOptionIndex(index: number) {
        if (index != this._selectedOptionIndex) {
            this._selectedOptionIndex = index;
            if (this.selectedOption) {
                this.inputText = this.selectedOption.label;
                this.onChangeCallback(this.selectedOption.value);
                this.change.emit(this.selectedOption.value);
            }
        }
    }
    
    private hasSelectedOption(): boolean {
        return this.selectedOptionIndex != undefined && this.selectedOptionIndex != null;
    }
    
    private unSelectOption() {
        if (this.selectedOptionIndex) {
            this.selectedOptionIndex = null;
        }
        this.firstScrollOptionIndex = 0;
        this.focusedOptionIndex = null;
    }

    private computeMinWidth() {
        let maxOption: Option<any>;
        if (this.displayableOptions && this.displayableOptions.length > 0 && this.hiddenOption) {
            this.displayableOptions.forEach(opt => {
                if (!maxOption || opt.label.length > maxOption.label.length) {
                    maxOption = opt;
                }
            })
            this.hiddenOption.nativeElement.innerText = maxOption.label;
            this.maxWidth = this.hiddenOption.nativeElement.offsetWidth;
            this.hiddenOption.nativeElement.innerText = '';
        }
    }

    private set searchText(text: string) {
        this.focusedOptionIndex = null;
        if (!text) {
            this._searchText = null;
            this.filteredOptions = null;
        } else {
            this._searchText = text.trim().toLowerCase();
            if (this.searchText.length <= 0) {
                this.filteredOptions = null;
            } else if (!this.filteredOptions || this.filteredOptions.searchText != this.searchText) {
                this.filteredOptions = new FilteredOptions(this.searchText, this.options);
            }
        }
        this.computeMinWidth();
    }

    private get searchText(): string {
        return this._searchText;
    }
    
    private computeDisplayedOptions() {
        if (this.firstScrollOptionIndex == undefined || this.firstScrollOptionIndex == null || !this.options) {
            this.displayedOptions = [];
            return;
        }
        this.displayedOptions = this.displayableOptions.slice(this.firstScrollOptionIndex, this.firstScrollOptionIndex + this.LIST_LENGTH);
    }
    
    private onUserSelectedOption(option: Option<any>) {
        this.searchText = null;
        this.selectedOptionIndex = this.options.findIndex(eachOpt => this.valuesEqual(eachOpt.value, option.value));
        this.close();
    }

    private isOptionSelected(option: Option<any>) {
        return this.hasSelectedOption() && this.valuesEqual(this.selectedOption.value, option.value);
    }

    private valuesEqual(value1, value2) {
        return value1 == value2 || (
            value1 && value2 && value1.id && value2.id && value1.id == value2.id
        );
    }

    private open() {
        if (this.isOpen()) return;
        if (this.options) {
            this.subscribeToGlobalClick();
            this.scrollToSelected();
            this.chooseOpeningWay();
        }  
    }

    private close () {
        this.unsubscribeToGlobalClick();
        this.firstScrollOptionIndex = null;
        this.focusedOptionIndex = null;
    }
    
    private isOpen(): boolean {
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
        if (this.scrollable) {
            this.firstScrollOptionIndex = this.selectedOptionIndex ? this.selectedOptionIndex : 0;
        } else {
            this.firstScrollOptionIndex = 0;
        }
    }

    chooseOpeningWay() {
        this.hideToComputeHeight = true;
        this.way = 'down'
        setTimeout(() => {
            let listElt = this.element.nativeElement.querySelector('.list');
            if (!listElt) return;
            let bottom = listElt.getBoundingClientRect().bottom;
            let docHeight: number = document.body.clientHeight;
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
    private onKeyPress(event: any) {
        if ('ArrowDown' == event.key) {
            if (this.isOpen()) {
                this.scrollDownByOne();
            } else {
                if (this.hasSelectedOption && this.selectedOptionIndex < this.options.length) {
                    this.onUserSelectedOption(this.options[this.selectedOptionIndex + 1]);
                } else {
                    this.onUserSelectedOption(this.options[0]);
                }
            }
            event.preventDefault();
        } else if ('ArrowUp' == event.key) {
            if (this.isOpen()) {
                this.scrollUpByOne();
            } else {
                if (this.hasSelectedOption && this.selectedOptionIndex > 0) {
                    this.onUserSelectedOption(this.options[this.selectedOptionIndex - 1]);
                } else {
                    this.onUserSelectedOption(this.options[0]);
                }
            }
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
                    this.onUserSelectedOption(this.displayedOptions[this.focusedOptionIndex]);
                }
            }
            event.preventDefault();
        } else if (' ' == event.key) {
            if (!this.isOpen()) this.open();
        }  else if (event.keyCode >= 65 && event.keyCode <= 90) {
            if (this.textInput.nativeElement != document.activeElement) {
                this.inputText = '';
                this.textInput.nativeElement.focus();
            }
        }       
    }

    private scrollDownByOne() {
        if (this.focusedOptionIndex == null) {
            this.focusedOptionIndex = 0;
            return;
        }
        //let nextIndex: number = this.displayableOptions.slice(this.focusedOptionIndex).findIndex()
        if (this.focusedOptionIndex + this.firstScrollOptionIndex + 1 < this.displayableOptions.length) {
            if (this.scrollable && this.focusedOptionIndex + 1 == this.LIST_LENGTH) {
                this.firstScrollOptionIndex ++;
            } else {
                this.focusedOptionIndex ++;
            }
        }
    }

    private scrollUpByOne() {
        if (this.focusedOptionIndex == null) {
            this.focusedOptionIndex = 0;
            return;
        }
        if (this.focusedOptionIndex + this.firstScrollOptionIndex > 0) {
            if (this.scrollable && this.focusedOptionIndex == 0) {
                this.firstScrollOptionIndex --;
            } else {
                this.focusedOptionIndex --;
            }
        }
    }

    private scrollButtonInterval;
    private scrollButtonTime: number;

    scrollButtonOn(way: 'down' | 'up') {
        let now: number = Date.now();
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
    private dragging: boolean = false;
    onDragStart(event, data) {
        this.dragging = true;
        let img = new Image();
        img.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=';
        event.dataTransfer.setDragImage(img, 0, 0);
        this.dragStartOffsetY = event.clientY - event.srcElement.getBoundingClientRect().top;
    }

    private lastDropY: number = 0;
    private lastScroll: number = 0;
    allowDrop(event) {
        let dropY: number = event.clientY;
        if (dropY == this.lastDropY || (Date.now() - this.lastScroll) < 30) return;
        this.lastDropY = dropY;
        this.lastScroll = Date.now();
        let listElt = this.element.nativeElement.querySelector('.list');
        let listTop: number = listElt.getBoundingClientRect().top + 10;
        let listHeight: number = listElt.getBoundingClientRect().height - 20;
        let relativeDropY = Math.max(0, dropY - listTop - this.dragStartOffsetY);
        
        this.firstScrollOptionIndex = 
        Math.min(
            Math.round((this.displayableOptions.length - this.LIST_LENGTH) * (relativeDropY / (listHeight - 50))), 
            this.displayableOptions.length - this.LIST_LENGTH
            );
            
        //console.log(listHeight, relativeDropY, this.firstScrollOptionIndex)
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

    private onTypeText(text: string) {
        this.searchText = text;
        this.unSelectOption();
        this.open();
    }

    private onInputFocus() {
        this.textInput.nativeElement.select();
    }    
    
    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    @HostListener('focusout', ['$event']) 
    private onFocusOut(event: FocusEvent) {
        if (!this.element.nativeElement.contains(event.relatedTarget)) {
            this.close();
            this.onTouchedCallback();
        } 
    }

    @HostBinding('attr.tabindex')
    private get tabindex(): number {
        return this.disabled ? undefined : 0;
    } 

    private clickView(): void {
        if(!this.viewDisabled && this.selectedOption) this.onViewClick.emit(this.selectedOption.value);
    }

    private clickNew(): void {
        if(!this.newDisabled) this.onNewClick.emit();
    }

    private clickAdd(): void {
        if(!this.addDisabled && this.selectedOption) this.onAddClick.emit(this.selectedOption.value);
    }

    setDisabledState(isDisabled: boolean) {
        this.disabled = isDisabled;
    }

    public get length(): number {
        let opt: Option<any>[] = this.displayableOptions;
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

    disabled: boolean = false;
      
    constructor(
        public value: T,
        public label: string,
        public section?: string) {}
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