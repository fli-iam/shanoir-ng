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
    AfterContentInit,
    Component,
    ComponentFactoryResolver,
    ContentChildren,
    ElementRef,
    EventEmitter,
    forwardRef,
    HostBinding,
    HostListener,
    Input,
    OnDestroy,
    Output,
    QueryList,
    Renderer2,
    ViewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subscription } from 'rxjs';

import { GlobalService } from '../services/global.service';
import { SelectOptionComponent } from './select.option.component';


@Component({
    selector: 'select-box',
    templateUrl: 'select.component.html',
    styleUrls: ['select.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => SelectBoxComponent),
          multi: true,
        }]   
})

export class SelectBoxComponent implements ControlValueAccessor, OnDestroy, AfterContentInit {

    private model: any = null;
    @Output() change = new EventEmitter();
    @ContentChildren(forwardRef(() => SelectOptionComponent)) private options: QueryList<SelectOptionComponent>;
    @ContentChildren('optionSection') private sections: QueryList<ElementRef>;
    @ViewChild('input') textInput: ElementRef;
    private _selectedOption: SelectOptionComponent;
    private openState: boolean = false;
    private globalClickSubscription: Subscription;
    private optionChangeSubscription: Subscription;
    private way: 'up' | 'down' = 'down';
    private hideToComputeHeight: boolean = false;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    private inputText: string;
    @Input() disabled: boolean = false;
    @Input() placeholder: string;
    
    @Input() viewDisabled: boolean;
    @Input() viewHidden: boolean;
    @Input() newDisabled: boolean;
    @Input() newHidden: boolean;
    @Input() addDisabled: boolean;
    @Input() addHidden: boolean;
    @Output() onViewClick = new EventEmitter();
    @Output() onNewClick = new EventEmitter();
    @Output() onAddClick = new EventEmitter();

    constructor(
            private element: ElementRef, 
            private globalService: GlobalService,
            private renderer: Renderer2,
            private componentFactoryResolver: ComponentFactoryResolver) {}

    ngOnDestroy() {
        this.unsubscribeToGlobalClick();
        if (this.optionChangeSubscription) this.optionChangeSubscription.unsubscribe();
    }

    ngAfterContentInit() {
        this.options.forEach((option) => {
            option.parent = this;
            if (this.valuesEqual(option.value, this.model)) this.selectedOption = option;
        });
        this.optionChangeSubscription = this.options.changes.subscribe(() => {
            this.inputText = null;
            let founded: boolean = false;
            this.options.forEach((option) => {
                option.parent = this;
                if (this.valuesEqual(option.value, this.model)) {
                    this.selectedOption = option;
                    founded = true;
                    return;
                }
            });
            if (!founded) this.selectedOption = null;
        })
    }

    private get selectedOption(): SelectOptionComponent {
        return this._selectedOption;
    }

    private set selectedOption(option: SelectOptionComponent) {
        this.model = option ? option.value : null;
        if (this._selectedOption) this._selectedOption.selected = false;
        this._selectedOption = option;
        if (this._selectedOption) {
            this._selectedOption.selected = true;
            this.inputText = this.selectedOption.label;
        }
    }
    
    public onSelectedOptionChange(option: SelectOptionComponent) {
        if (!this.optionsEqual(option, this.selectedOption)) {
            this.selectedOption = option;
            if (!option) this.inputText = this.selectedOption.label;
            this.onChangeCallback(option.value);
            this.change.emit(option.value);
        }
        this.open = false;
        this.options.forEach(option => {
            option.hidden = false;
        });
        this.sections.forEach(section => {
            section.nativeElement.classList.remove('hidden');
        });
    }

    private optionsEqual(option1: SelectOptionComponent, option2: SelectOptionComponent) {
        return option1 == option2
            || (option1 && option2 && this.valuesEqual(option1.value, option2.value));
    }

    private valuesEqual(value1, value2) {
        return value1 == value2 || (
            value1 && value2 && value1.id && value2.id && value1.id == value2.id
        );
    }
    
    public onOptionOver(option: SelectOptionComponent) {
        let focusIndex = this.getFocusIndex();
        if (focusIndex != -1) {
            this.options.toArray()[focusIndex].focus = false;
        }
        option.focus = true;
    }

    private set open(open: boolean) {
        if(this.open == open) return;
        if (open && !this.openState && this.options.length > 0) { //open
            this.subscribeToGlobalClick();
            this.openState = true;
        } else if (!open && this.openState) { //close
            this.unsubscribeToGlobalClick();
            this.options.forEach(option => option.focus = false);
            this.openState = false;
        }
        if (this.openState) {
            this.chooseOpeningWay();
            this.scrollToSelectedOption();
        }
    }

    private get open(): boolean {
        return this.openState;
    }

    private scrollToSelectedOption() {
        setTimeout(() => {
            if (!this.selectedOption) return;
            let top: number = this.selectedOption.elt.nativeElement.offsetTop;
            let listElt = this.element.nativeElement.querySelector('.list');
            if (listElt) listElt.scrollTop = top;
        });  
    }

    private scrollToFocusedOption() {
        let focusIndex = this.getFocusIndex();
        if (focusIndex == -1) return;
        let focusedOption = this.options.toArray()[focusIndex];
        setTimeout(() => {
            let listElt = this.element.nativeElement.querySelector('.list');
            if (!listElt) return;
            let top: number = focusedOption.elt.nativeElement.offsetTop;
            let height: number = focusedOption.elt.nativeElement.offsetHeight;
            let frameTop: number = listElt.scrollTop;
            let frameHeight: number = listElt.offsetHeight;
            if (top < frameTop) {
                listElt.scrollTop = top;
            } else if (top > frameTop + frameHeight - height) {
                listElt.scrollTop = top - frameHeight + height;
            }
        });  
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
                this.open = false;
            }
        });
    }

    private unsubscribeToGlobalClick() {
        if (this.globalClickSubscription) this.globalClickSubscription.unsubscribe();
    }

    @HostListener('keydown', ['$event']) 
    private onKeyPress(event: any) {
        if ('ArrowDown' == event.key) {
            let optArr = this.options.toArray();
            let focusIndex = this.getFocusIndex();
            let nextFocusIndex = this.nextOptionIndex(focusIndex);
            if (nextFocusIndex != -1) {
                if (optArr[focusIndex]) optArr[focusIndex].focus = false;
                optArr[nextFocusIndex].focus = true;
                if (!this.open) this.onSelectedOptionChange(optArr[nextFocusIndex]);
                else this.scrollToFocusedOption();
            }
            event.preventDefault();
        } else if ('ArrowUp' == event.key) {
            let optArr = this.options.toArray();
            let focusIndex = this.getFocusIndex();
            if (focusIndex == -1) focusIndex = optArr.length - 1;
            let prevFocusIndex = this.prevOptionIndex(focusIndex);
            if (prevFocusIndex != -1 ) {
                if (optArr[focusIndex]) optArr[focusIndex].focus = false;
                optArr[prevFocusIndex].focus = true;
                if (!this.open) this.onSelectedOptionChange(optArr[prevFocusIndex]);
                else this.scrollToFocusedOption();
            }
            event.preventDefault();
        } else if ('Enter' == event.key) {
            if (!this.open && !this.disabled) {
                this.open = true;
            } else {
                let focusIndex = this.getFocusIndex();
                if (focusIndex != -1) {
                    this.onSelectedOptionChange(this.options.toArray()[focusIndex]);
                }
            }
            event.preventDefault();
        } 
        else if (event.keyCode >= 65 && event.keyCode <= 90) {
            if (this.textInput.nativeElement != document.activeElement) {
                this.inputText = '';
                this.textInput.nativeElement.focus();
            }
        }       
    }

    private nextOptionIndex(currentIndex: number): number {
        if (currentIndex >= this.options.length - 1) return -1;
        let optArr = this.options.toArray();
        for (let i = currentIndex + 1; i < optArr.length; i++) {
            if (!optArr[i].hidden && !optArr[i].disabled) return i;
        }
        return -1;
    }

    private prevOptionIndex(currentIndex: number): number {
        if (currentIndex <= 0) return -1;
        let optArr = this.options.toArray();
        for (let i = currentIndex - 1; i >= 0; i--) {
            if (!optArr[i].hidden && !optArr[i].disabled) return i;
        }
        return -1;
    }

    private onTypeText(text: string) {
        this.open = true;
        if (this.selectedOption) {
            this.selectedOption = null;
            this.onChangeCallback(null);
            this.change.emit(null);
        }
        if (text && text.length > 0) {
            text = text.trim().toLowerCase();
            this.options.forEach(option => {
                option.hidden = !option.label.toLowerCase().includes(text);
            });
        } else {
            this.options.forEach(option => {
                option.hidden = false;
            });
        }
        this.sections.forEach(section => {
            section.nativeElement.classList.remove('hidden');
            let hidden: boolean = true;
            for (let optionElt of section.nativeElement.getElementsByTagName('select-option')) {
                let option: SelectOptionComponent = this.options.find(opt => opt.elt.nativeElement == optionElt);
                if (!option.hidden) {
                    hidden = false;
                    break;
                }
            }
            if (hidden) section.nativeElement.classList.add('hidden');
        });
    }

    private onInputFocus() {
        this.textInput.nativeElement.select();
    }    

    private getFocusIndex(): number {
        let foundedIndex = -1;
        let selectedIndex = -1;
            this.options.forEach((option, index) => {
                if (option.focus) {
                    foundedIndex = index;
                    return;
                }
                if (option.selected && !option.disabled && !option.hidden) selectedIndex = index;
            });
        if (foundedIndex == -1 && selectedIndex != -1) return selectedIndex;
        return foundedIndex;
    }

    writeValue(obj: any): void {
        this.model = obj;
        this.inputText = null;
        if (this.options) {
            this.options.forEach((option) => {
                option.parent = this;
                if (this.valuesEqual(option.value, this.model)) this.selectedOption = option;
            });
        }
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
            this.open = false; 
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
}