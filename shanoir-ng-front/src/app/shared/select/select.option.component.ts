import { Component, Input, Host, AfterViewInit, ElementRef } from '@angular/core';
import { SelectBoxComponent } from './select.component';

@Component({
    selector: 'select-option',
    template: `
        <div (click)="select()" (mouseenter)="over()" [class.selected]="selected" [class.disabled]="disabled" [class.focus]="focus"><ng-content></ng-content></div>
    `,
    styles: [
        ':host() { height: 20px; display:block; border: none; white-space: nowrap; }',
        'div { padding: 0 5px; }',
        'div.focus { background-color: var(--grey); }',
        'div.selected { background-color: var(--color-b-light); }',
        'div.disabled { opacity: 0.5; }'
    ]
    
})

export class SelectOptionComponent implements AfterViewInit {
    
    @Input() value: any;
    @Input() disabled: any;
    public parent: SelectBoxComponent;
    public label: string;
    public selected: boolean = false;
    public focus: boolean = false;

    constructor(public elt: ElementRef) { 
       
    }

    ngAfterViewInit() {
        let textNode = this.elt.nativeElement.childNodes[1].childNodes[0];
        this.label = textNode.textContent;
    }

    private select() {
        if (!this.disabled) this.parent.onSelectedOptionChange(this);
    }

    private over() {
        if (!this.disabled) this.parent.onOptionOver(this);
    }

}