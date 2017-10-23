import { Component, ViewChild, ElementRef, Renderer } from '@angular/core';


@Component({
    selector: 'tool-tip',
    template: `
        <span #container class="help" [class.opened]="opened" (mouseover)="open()" (mouseleave)="close()">?
            <div class="content" *ngIf="opened">
                <ng-content></ng-content>
            </div>
        </span>
    `,
    styles: [
        ':host() { vertical-align: top; display: inline-block; }',
        '.help { border: 1px solid var(--color-c); border-radius: 15px; font-size: 10px; font-weight: bold; cursor: pointer; width: 15px; height: 14px; text-align: center; vertical-align: middle; display: table-cell; position: absolute; z-index: 10; }',
        '.help.opened { box-shadow: 0 0 var(--shadow-height) 0 var(--shadow-color); background-color: lightgoldenrodyellow; }',
        '.help .content { position: absolute; width: 250px; font-size: 12px; background-color: lightgoldenrodyellow; padding: 6px 12px; box-shadow: 0 0 var(--shadow-height) 0 var(--shadow-color); color: var(--dark-grey); top: 14px; left: 16px; font-weight: normal; text-align: left; }'
    ]
})

export class ToolTipComponent {

    private opened: boolean = false;
    private static documentListenerInit = false;
    private static openedTips: Set<ToolTipComponent>; // every opened menu in the document (upgrade idea : named groups of menu)
    @ViewChild('container') container: ElementRef;

    constructor(public elementRef: ElementRef, private renderer: Renderer) { 
        ToolTipComponent.openedTips = new Set<ToolTipComponent>();
        if (!ToolTipComponent.documentListenerInit) {
            ToolTipComponent.documentListenerInit = true;
            document.addEventListener('mouseover', ToolTipComponent.mouseOverDocument.bind(this));
        }
    }

    public open() {
        this.opened =  true;
        ToolTipComponent.openedTips.add(this);
    }

    public close() {
        ToolTipComponent.openedTips.delete(this);
        this.opened =  false;
    }

    public static mouseOverDocument = (event: Event) => {
        ToolTipComponent.openedTips.forEach((tip) => {
            if (!tip.container.nativeElement.contains(event.target)) {
                tip.close();
            };
        });
    }
} 