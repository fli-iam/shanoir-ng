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
    
    @Input() @HostBinding('class.on') ngModel: boolean = null;
    @Output() ngModelChange = new EventEmitter();
    private state: boolean = false;
    private onTouchedCallback = () => {};
    private onChangeCallback = (_: any) => {};
    @Input() disabled: boolean = false;

    constructor() {}

    @HostListener('click', ['$event']) 
    private onClick() {
        this.ngModel = !this.ngModel;
        this.ngModelChange.emit(this.ngModel);
    }

    @HostListener('keydown', ['$event']) 
    private onKeyPress(event: any) {
        if (' ' == event.key) {
            this.ngModel = !this.ngModel;
            this.ngModelChange.emit(this.ngModel);
            event.preventDefault();
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