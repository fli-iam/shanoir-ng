import { Component, ViewChild, ElementRef, Renderer } from '@angular/core';


@Component({
    selector: 'click-tip',
    template: `
        <span #container class="help" [class.opened]="opened" (click)="toggle()">?
            <div class="content" *ngIf="opened">
                <ng-content></ng-content>
            </div>
        </span>
    `,
    styles: [
        ':host() { vertical-align: text-bottom; }',
        '.help { border: 1px solid var(--color-c); border-radius: 15px; font-size: 10px; font-weight: bold; cursor: pointer; width: 15px; height: 16px; text-align: center; vertical-align: middle; display: table-cell; position: relative; }',
        '.help.opened { box-shadow: 0 0 var(--shadow-height) 0 var(--shadow-color); background-color: lightgoldenrodyellow; }',
        '.help .content { position: absolute; width: 250px; font-size: 12px; background-color: lightgoldenrodyellow; padding: 6px 12px; box-shadow: 0 0 var(--shadow-height) 0 var(--shadow-color); color: var(--dark-grey); top: 14px; left: 16px; font-weight: normal; text-align: left; }'
    ]
})

export class ClickTipComponent {

    private opened: boolean = false;
    private static documentListenerInit = false;
    private static openedTips: Set<ClickTipComponent>; // every opened menu in the document (upgrade idea : named groups of menu)
    @ViewChild('container') container: ElementRef;

    constructor(public elementRef: ElementRef, private renderer: Renderer) { 
        ClickTipComponent.openedTips = new Set<ClickTipComponent>();
        if (!ClickTipComponent.documentListenerInit) {
            ClickTipComponent.documentListenerInit = true;
            document.addEventListener('click', ClickTipComponent.clickDocument.bind(this));
        }
    }

    public toggle() {
        if (this.opened) this.close();
        else this.open();
    }

    public open() {
        this.opened =  true;
        ClickTipComponent.openedTips.add(this);
    }

    public close() {
        ClickTipComponent.openedTips.delete(this);
        this.opened =  false;
    }

    public static clickDocument = (event: Event) => {
        ClickTipComponent.openedTips.forEach((tip) => {
            if (!tip.container.nativeElement.contains(event.target)) {
                tip.close();
            };
        });
    }
} 