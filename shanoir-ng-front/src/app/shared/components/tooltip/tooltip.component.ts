import { Component, ViewChild, ElementRef, Renderer } from '@angular/core';


@Component({
    selector: 'tool-tip',
    templateUrl: 'tooltip.component.html',
    styleUrls: ['tooltip.component.css']
})

export class TooltipComponent {

    private opened: boolean = false;
    private static documentListenerInit = false;
    private static openedTips: Set<TooltipComponent>; // every opened menu in the document (upgrade idea : named groups of menu)
    @ViewChild('container') container: ElementRef;

    constructor(public elementRef: ElementRef, private renderer: Renderer) {
        TooltipComponent.openedTips = new Set<TooltipComponent>();
        if (!TooltipComponent.documentListenerInit) {
            TooltipComponent.documentListenerInit = true;
            document.addEventListener('mouseover', TooltipComponent.mouseOverDocument.bind(this));
        }
    }

    public open() {
        this.opened = true;
        TooltipComponent.openedTips.add(this);
    }

    public close() {
        TooltipComponent.openedTips.delete(this);
        this.opened = false;
    }

    public static mouseOverDocument = (event: Event) => {
        TooltipComponent.openedTips.forEach((tip) => {
            if (!tip.container.nativeElement.contains(event.target)) {
                tip.close();
            };
        });
    }
} 