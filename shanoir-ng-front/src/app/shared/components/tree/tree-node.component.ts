import { Component, Input, Output, ContentChildren, forwardRef, QueryList, ChangeDetectorRef, 
    EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component'
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

const noop = () => {
};

export const CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR: any = {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TreeNodeComponent),
    multi: true
};

@Component({
    selector: 'node',
    templateUrl: 'tree-node.component.html',
    styleUrls: ['tree-node.component.css'],
    providers: [CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR]
})

export class TreeNodeComponent implements ControlValueAccessor {

    @Input() label: string;
    @Input() pictoUrl: string;
    @Input() deploy: boolean;
    @Input() hasBox: boolean;
    @Input() buttonPicto: string;
    @Input() nodeParams: any;
    @Input() editable: boolean = false;
    @Input() link: boolean = false;
    @Input() tooltip: string;
    @ContentChildren(forwardRef(() => TreeNodeComponent)) childNodes: QueryList<any>; 
    @ContentChildren(forwardRef(() => DropdownMenuComponent)) menus: QueryList<any>; 
    public isOpen: boolean = false;
    public loaded: boolean = false;
    public hasChildren: boolean;
    public checked: boolean;
    @ViewChild('box') boxElt:ElementRef;
    @Output() buttonClick = new EventEmitter();
    @Output() labelClick = new EventEmitter();
    @Output() nodeSelected = new EventEmitter();
    private onTouchedCallback: () => void = noop;
    private onChangeCallback: (_: any) => void = noop;
    
    constructor(private cdr: ChangeDetectorRef) {

    }

    ngAfterViewInit() {
        if (this.deploy) {
            this.deployAll();
        }
        this.updateChildren();
        this.loaded = true;
        this.cdr.detectChanges();
    }

    public deployAll() {
        this.open();
        this.cdr.detectChanges();
        this.childNodes.forEach((node, index) => {
            if (index!= 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                node.deployAll();
            }
        });
    }

    public isClickable(): boolean {
        if (this.labelClick.observers.length > 0) {
            return true;
        }
        return false;
    }

    public open() {
        this.isOpen =  true;
    }

    public close() {
        this.isOpen =  false;
    }

    public toggle() {
        if (this.isOpen) this.close();
        else this.open();
    }

    public updateChildren(): void {
        this.hasChildren = this.childNodes.toArray().length > 1; // TODO : set to 0 when the bug is fixed https://github.com/angular/angular/issues/10098
        this.childNodes.forEach((child, index) => {
            if (index!= 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                child.notifyParent = this.updateSelf;
            }
        });
    }
    

    get value(): boolean {
        return this.checked;
    };

    set value(value: boolean) {
        if (value !== this.checked) {
            this.checked = value;
            this.onChangeCallback(value);
            this.childNodes.forEach((node, index) => {
                if (index!= 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                    node.value = value;
                }
            });
            this.notifyParent();
        }
    }

    private notifyParent: () => void;

    private updateSelf = () => {
        let allOn: boolean = true;
        let allOff: boolean = true;
        this.childNodes.forEach((child, index) => {
            if (index!= 0) { // TODO : THE IF INDEX != 0 HAS TO BE REMOVED ONCE THE BUG IS FIXED : https://github.com/angular/angular/issues/10098
                if (!child.checked) {
                    allOn = false;
                } else {
                    allOff = false;
                }
            }
        });
        if(allOff) this.setBox(false);
        else if(allOn) this.setBox(true);
        else this.setBox(null);
    };

    setBox(value: Boolean) {
        if (this.boxElt) this.boxElt.nativeElement.indeterminate = value == null;
        this.writeValue(value != null && value);
    }

    //From ControlValueAccessor interface
    writeValue(value: any) {
        if (value !== this.checked) {
            this.checked = value;
            if (this.notifyParent != undefined) this.notifyParent();
        }
    }

    //From ControlValueAccessor interface
    registerOnChange(fn: any) {
        this.onChangeCallback = fn;
    }

    //From ControlValueAccessor interface
    registerOnTouched(fn: any) {
        this.onTouchedCallback = fn;
    }

} 