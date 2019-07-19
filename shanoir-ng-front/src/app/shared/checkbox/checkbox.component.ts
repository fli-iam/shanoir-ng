import { Component, Input, Output, SimpleChanges, HostListener, HostBinding, EventEmitter, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';


@Component({
    selector: 'checkbox',
    templateUrl: 'checkbox.component.html',
    styleUrls: ['checkbox.component.css'],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => CheckboxComponent),
          multi: true,
        }]   
})

export class CheckboxComponent implements ControlValueAccessor { 
    
    @Input() @HostBinding('class.on') ngModel: boolean | 'indeterminate' = null;
    @Output() ngModelChange = new EventEmitter();
    @Output() onChange = new EventEmitter();
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() @Input() @HostBinding('class.disabled') disabled: boolean = false;

    constructor() {}

    @HostListener('click', ['$event']) 
    private onClick() {
        if (this.disabled) return;
        this.ngModel = !this.ngModel;
        this.ngModelChange.emit(this.ngModel);
        this.onChange.emit(this.ngModel);
        this.onChangeCallback(this.ngModel);
        this.onTouchedCallback();
    }

    @HostListener('keydown', ['$event']) 
    private onKeyPress(event: any) {
        if (this.disabled) return;
        if (' ' == event.key) {
            this.ngModel = !this.ngModel;
            this.ngModelChange.emit(this.ngModel);
            this.onChange.emit(this.ngModel);
            this.onChangeCallback(this.ngModel);
            this.onTouchedCallback();
            event.preventDefault();
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['ngModel'] && !changes['ngModel'].firstChange) {
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