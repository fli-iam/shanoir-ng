import { Component, Input, ContentChildren, forwardRef, QueryList } from '@angular/core';

@Component({
    selector: 'node',
    template: `
        <div class="node-wrapper">
            <span *ngIf="hasChildren && isOpen" (click)="close()" class="arrow">&#x25BE;</span>
            <span *ngIf="hasChildren && !isOpen" (click)="open()" class="arrow">&#9656;</span>
            <span *ngIf="pictoUrl"><img class="picto" src="{{pictoUrl}}"/></span>
            <span class="label">{{label}}</span>
            <div #childrentWrapper *ngIf="!loaded || (loaded && isOpen)" [class.hidden]="!loaded">
                <ng-content></ng-content>
            </div>
        </div>
    `,
    styleUrls: ['tree.node.component.css']
})

export class TreeNodeComponent {

    @Input() label: string;
    @Input() pictoUrl: string;
    @ContentChildren(forwardRef(() => TreeNodeComponent)) childNodes: QueryList<any>; 
    public isOpen: boolean = false;
    public loaded: boolean = false;
    public hasChildren: boolean = false;

    constructor() {
    }

    ngAfterViewInit() {
        this.updateHasChildren();
        this.loaded = true;
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

    public updateHasChildren(): void {
        this.hasChildren = this.childNodes.toArray().length > 1; // TODO : set to 0 when the bug is fixed https://github.com/angular/angular/issues/10098
    }

} 