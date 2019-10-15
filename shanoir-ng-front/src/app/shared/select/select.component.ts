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
    OnChanges,
    OnDestroy,
    Output,
    QueryList,
    Renderer2,
    SimpleChanges,
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

export class SelectBoxComponent implements ControlValueAccessor, OnDestroy, OnChanges, AfterContentInit {

    @Input() ngModel: any = null;
    @Output() ngModelChange = new EventEmitter();
    @Output() change = new EventEmitter();
    @ContentChildren(forwardRef(() => SelectOptionComponent)) private options: QueryList<SelectOptionComponent>;
    @ViewChild('label', {read: ElementRef, static: null }) labelNode: ElementRef;
    private selectedOption: SelectOptionComponent;
    private openState: boolean = false;
    private globalClickSubscription: Subscription;
    private way: 'up' | 'down' = 'down';
    private hideToComputeHeight: boolean = false;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    private lastSearchTime: number = 0;
    private currentSearch: string;
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
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['ngModel']) {
            this.updateSelectedOption();
            if (!changes['ngModel'].firstChange) this.onChangeCallback(this.ngModel);
        }
    }

    ngAfterContentInit() {
        this.options.forEach((option) => {
            option.parent = this;
        });
        this.options.changes.subscribe(() => {
            this.options.forEach((option) => {
                option.parent = this;
            });
            this.updateSelectedOption();
        })
        setTimeout(() => this.updateSelectedOption());
    }

    private updateSelectedOption() {
        this.selectedOption = null;
        if (this.ngModel && this.options) {
            this.options.forEach((option) => {
                if(this.valuesEqual(option.value, this.ngModel)) {
                    this.selectedOption = option;
                    option.selected = true;
                } else {
                    option.selected = false;
                }
            });
        }
        this.updateLabel();
    }
    
    public onSelectedOptionChange(option: SelectOptionComponent) {
        if (!this.optionsEqual(option, this.selectedOption)) {
            this.selectedOption = option;
            this.ngModelChange.emit(option.value);
            this.change.emit(option.value);
            this.updateLabel();
        }
        this.open = false;
    }
    
    private updateLabel() {
        Array.from(this.labelNode.nativeElement.children).forEach(child => {
            this.renderer.removeChild(this.labelNode.nativeElement, child);
        });
        if (this.selectedOption) {
            let cloned = this.selectedOption.elt.nativeElement.cloneNode(true);
            cloned.children[0].classList.remove('focus');
            cloned.children[0].classList.remove('selected');
            cloned.children[0].style.padding = '0';
            cloned.children[0].style.overflow = 'hidden';
            cloned.children[0].style.textOverflow = 'ellipsis';
            cloned.children[0].style.color = 'var(--dark-grey)';
            this.renderer.appendChild(this.labelNode.nativeElement, cloned);
        }
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
        if (!this.selectedOption) return;
        setTimeout(() => {
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
            let focusIndex = this.getFocusIndex();
            if (focusIndex == -1) focusIndex = 0;
            if (focusIndex < this.options.length - 1) {
                this.options.toArray()[focusIndex].focus = false;
                this.options.toArray()[(focusIndex+1)].focus = true;
                if (!this.open) this.onSelectedOptionChange(this.options.toArray()[(focusIndex+1)]);
                else this.scrollToFocusedOption();
            }
            event.preventDefault();
        } else if ('ArrowUp' == event.key) {
            let focusIndex = this.getFocusIndex();
            if (focusIndex >= 1) {
                this.options.toArray()[focusIndex].focus = false;
                this.options.toArray()[(focusIndex-1)].focus = true;
                if (!this.open) this.onSelectedOptionChange(this.options.toArray()[(focusIndex-1)]);
                else this.scrollToFocusedOption();
            }
            event.preventDefault();
        } else if ('Enter' == event.key || ' ' == event.key) {
            if (!this.open && !this.disabled) {
                this.open = true;
            } else {
                let focusIndex = this.getFocusIndex();
                if (focusIndex != -1) {
                    this.onSelectedOptionChange(this.options.toArray()[focusIndex]);
                }
            }
            event.preventDefault();
        } else if (event.keyCode >= 65 && event.keyCode <= 90) {
            let key = event.key.toLowerCase();
            let now = new Date().getTime();
            if ((now - this.lastSearchTime) > 1000 || key == this.currentSearch ) {
                this.currentSearch = '';
            } 
            this.lastSearchTime = now;
            this.currentSearch += key;
            let focusIndex = this.getFocusIndex();
            let currentText = this.options.toArray()[focusIndex].elt.nativeElement.textContent.trim().toLowerCase();
            let serachIn;
            if (focusIndex >= 0 && currentText.startsWith(this.currentSearch)) {
                serachIn = this.options.toArray().slice(focusIndex + 1);
            } else {
                serachIn = this.options.toArray();
            }
            for (let option of serachIn) {
                let text: string = option.elt.nativeElement.textContent.trim().toLowerCase();
                if (text.startsWith(this.currentSearch)) {
                    if (focusIndex >= 0) this.options.toArray()[focusIndex].focus = false;
                    option.focus = true;
                    if (!this.open) this.onSelectedOptionChange(this.options.toArray()[this.getFocusIndex()]);
                    else this.scrollToFocusedOption();
                    return;
                }
            }
        }
            
    }

    private getFocusIndex(): number {
        let foundedIndex = -1;
        let selectedIndex = -1;
            this.options.forEach((option, index) => {
                if (option.focus) {
                    foundedIndex = index;
                    return;
                }
                if (option.selected) selectedIndex = index;
            });
        if (foundedIndex == -1 && selectedIndex != -1) return selectedIndex;
        return foundedIndex;
    }

    writeValue(obj: any): void {
        this.ngModel = obj;
        //this.onChangeCallback(this.ngModel);
    }
    
    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    @HostListener('focusout', ['$event']) 
    private onFocusOut() {
        this.open = false; 
        this.onTouchedCallback();
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