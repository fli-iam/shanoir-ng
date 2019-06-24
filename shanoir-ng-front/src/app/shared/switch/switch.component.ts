import { Component, Input, Output, SimpleChanges, HostListener, HostBinding, EventEmitter, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';


@Component({
    selector: 'toggle-switch',
    templateUrl: 'switch.component.html',
    styleUrls: ['switch.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => ToggleSwitchComponent),
          multi: true,
        }]   
})

export class ToggleSwitchComponent implements ControlValueAccessor { 
    
    @Input() @HostBinding('class.on') ngModel: boolean = null;
    @Output() ngModelChange = new EventEmitter();
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() @HostBinding('class.disabled') disabled: boolean = false;
    @Input() reverse: boolean = false;

    constructor() {}

    @HostListener('click', ['$event']) 
    private onClick() {
        if (this.disabled) return; 
        this.ngModel = !this.ngModel;
        this.ngModelChange.emit(this.ngModel);
    }

    @HostListener('keydown', ['$event']) 
    private onKeyPress(event: any) {
        if (this.disabled) return;
        if (' ' == event.key) {
            this.ngModel = !this.ngModel;
            this.ngModelChange.emit(this.ngModel);
            event.preventDefault();
        } else if ('ArrowLeft' == event.key) {
            if (this.ngModel) {
                this.ngModel = false;
                this.ngModelChange.emit(this.ngModel);    
            }
        } else if ('ArrowRight' == event.key) {
            if (!this.ngModel) {
                this.ngModel = true;
                this.ngModelChange.emit(this.ngModel);    
            }
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['ngModel']) {
            this.onChangeCallback(this.ngModel);
        }
    }
    writeValue(obj: any): void {
        this.ngModel = obj;
    }
    
    registerOnChange(fn: any): void {
        this.onChangeCallback = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouchedCallback = fn;
    }

    @HostListener('focusout', ['$event']) 
    private onFocusOut() {
        this.onTouchedCallback();
    }

    @HostBinding('attr.tabindex')
    private get tabindex(): number {
        return this.disabled ? undefined : 0;
    } 
}