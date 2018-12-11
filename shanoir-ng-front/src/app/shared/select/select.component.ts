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
    @ViewChild('label', {read: ElementRef}) labelNode: ElementRef;
    private selectedOption: SelectOptionComponent;
    private openState: boolean = false;
    private globalClickSubscription: Subscription;
    private way: 'up' | 'down' = 'down';
    private hideToComputeHeight: boolean = false;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() disabled: boolean = false;
    @Input() placeholder: string;
    
    @Input() viewDisabled: boolean;
    @Input() newDisabled: boolean;
    @Input() addDisabled: boolean;
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
            this.onChangeCallback(this.ngModel);
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
        })
        this.updateSelectedOption();
        this.updateLabel();
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
            for (let focus of cloned.getElementsByClassName('focus')) {
                focus.classList.remove('focus');
            }
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
            this.element.nativeElement.querySelector('.list').scrollTop = top;
        });  
    }

    private scrollToFocusedOption() {
        let focusIndex = this.getFocusIndex();
        if (focusIndex == -1) return;
        let focusesOption = this.options.toArray()[focusIndex];
        setTimeout(() => {
            let top: number = focusesOption.elt.nativeElement.offsetTop;
            let height: number = focusesOption.elt.nativeElement.offsetHeight;
            let frameTop: number = this.element.nativeElement.querySelector('.list').scrollTop;
            let frameHeight: number = this.element.nativeElement.querySelector('.list').offsetHeight;
            if (top < frameTop) {
                this.element.nativeElement.querySelector('.list').scrollTop = top;
            } else if (top > frameTop + frameHeight - height) {
                this.element.nativeElement.querySelector('.list').scrollTop = frameTop + height ;
            }
        });  
    }

    chooseOpeningWay() {
        this.hideToComputeHeight = true;
        this.way = 'down'
        setTimeout(() => {
            let bottom = this.element.nativeElement.querySelector('.list').getBoundingClientRect().bottom;
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
        this.updateSelectedOption();
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
        if(!this.viewDisabled && this.ngModel) this.onViewClick.emit(this.ngModel);
    }

    private clickNew(): void {
        if(!this.newDisabled) this.onNewClick.emit();
    }

    private clickAdd(): void {
        if(!this.addDisabled) this.onAddClick.emit();
    }
}