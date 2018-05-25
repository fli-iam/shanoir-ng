import { Input } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';

export abstract class AbstractInput implements ControlValueAccessor {

    @Input() private mode: 'create' | 'edit' | 'view';
    private model: any;
    private disabled: boolean = false;
    propagateChange = (_: any) => {};
    
    constructor() {}
    
    onChange() {
        this.propagateChange(this.model);
    }

    writeValue(obj: any): void {
        if (obj) this.model = obj;
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {}

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

}