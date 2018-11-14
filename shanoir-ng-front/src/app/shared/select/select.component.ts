import { AfterContentInit, Component, ContentChildren, ElementRef, EventEmitter, forwardRef, Input, OnChanges, OnDestroy, Output, QueryList, SimpleChanges, AfterContentChecked } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subscription } from 'rxjs';
import { SelectOptionComponent } from './select.option.component';
import { GlobalService } from '../services/global.service';

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

export class SelectBoxComponent implements ControlValueAccessor, OnDestroy, OnChanges, AfterContentChecked {
    
    @Input() ngModel: any = null;
    @Output() ngModelChange = new EventEmitter();
    @ContentChildren(forwardRef(() => SelectOptionComponent)) private options: QueryList<SelectOptionComponent>;
    private selectedOption: SelectOptionComponent;
    private openState: boolean = false;
    private globalClickSubscription: Subscription;
    private way: 'up' | 'down' = 'down';
    private hideToComputeHeight: boolean = false;
    private disabled: boolean = false;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    
    @Input() viewDisabled: boolean;
    @Input() newDisabled: boolean;
    @Input() addDisabled: boolean;
    @Output() onViewClick = new EventEmitter();
    @Output() onNewClick = new EventEmitter();
    @Output() onAddClick = new EventEmitter();

    constructor(private element: ElementRef, private globalService: GlobalService) {}

    ngOnDestroy() {
        this.unsubscribeToGlobalClick();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['ngModel']) {
            this.updateSelectedOption();
            this.onChangeCallback(this.ngModel);
        }
    }

    ngAfterContentChecked() {
        this.options.forEach((option) => {
            option.parent = this;
        });
        this.options.changes.subscribe(() => {
            this.options.forEach((option) => {
                option.parent = this;
            });
        })
        this.updateSelectedOption();
    }

    private updateSelectedOption() {
        this.selectedOption = null;
        if (this.ngModel && this.options) {
            this.options.forEach((option) => {
                if(option.value == this.ngModel || (option.value.id && this.ngModel && option.value.id == this.ngModel.id)) {
                    this.selectedOption = option;
                    option.selected = true;
                } else {
                    option.selected = false;
                }
            });
        }
    }

    public onSelectedOptionChange(option: SelectOptionComponent) {
        this.selectedOption = option;
        this.ngModelChange.emit(option.value);
        this.open = false;
    }
    
    public onOptionOver(option: SelectOptionComponent) {
        let focusIndex = this.getFocusIndex();
        if (focusIndex != -1) {
            this.options.toArray()[focusIndex].focus = false;
        }
        option.focus = true;
    }

    private get label(): string {
        if (!this.selectedOption) return null;
        return this.selectedOption.label;
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
            //event.detail.keyboardEvent.preventDefault();
        } else if ('ArrowUp' == event.key) {
            let focusIndex = this.getFocusIndex();
            if (focusIndex >= 1) {
                this.options.toArray()[focusIndex].focus = false;
                this.options.toArray()[(focusIndex-1)].focus = true;
                if (!this.open) this.onSelectedOptionChange(this.options.toArray()[(focusIndex-1)]);
                else this.scrollToFocusedOption();
            }
            //event.detail.keyboardEvent.preventDefault();
        } else if ('Enter' == event.key || 'Space' == event.key) {
            let focusIndex = this.getFocusIndex();
            if (focusIndex != -1) {
                this.onSelectedOptionChange(this.options.toArray()[focusIndex]);
            }
            //event.detail.keyboardEvent.preventDefault();
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

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    private clickView(): void {
        if(!this.viewDisabled && this.ngModel) this.onViewClick.emit();
    }

    private clickNew(): void {
        if(!this.newDisabled) this.onNewClick.emit();
    }

    private addView(): void {
        if(!this.addDisabled) this.onAddClick.emit();
    }
}