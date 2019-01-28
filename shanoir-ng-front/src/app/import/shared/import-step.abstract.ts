import { Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';


export abstract class AbstractImportStepComponent implements OnChanges {

    @Input() open: boolean;
    @Input() disabled: boolean;
    @Input() lastEnabled: boolean = false;
    @Output() headerClick = new EventEmitter<any>();
    @Output() validityChange = new EventEmitter<boolean>();
    private valid: boolean;

    constructor() {}
    
    ngOnChanges(changes: SimpleChanges) {
        this.updateValidity();
    }

    protected updateValidity() {
        let newValue = this.getValidity();
        if (this.valid != newValue) {
            this.validityChange.emit(newValue);
        }
        this.valid = newValue;
    }

    abstract getValidity(): boolean;

    private onHeaderClick(event: any) {
        if (!this.disabled) this.headerClick.emit(event);
    }

}