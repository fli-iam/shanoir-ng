import { Component } from '@angular/core';


@Component({
    selector: 'tool-tip',
    templateUrl: 'tooltip.component.html',
    styleUrls: ['tooltip.component.css']
})

export class TooltipComponent {

    private opened: boolean = false;
    private opening: boolean = false;
    private closing: boolean = false;

    private onOver() {
        if (!this.opening) {
            this.closing = false;
            this.opening = true;
            setTimeout(() =>  {
                if (this.opening)
                    this.opened = true;
            }, 500);
        }
    }

    private onLeave() {
        if (!this.closing) {
            this.closing = true;
            this.opening = false;
            setTimeout(() =>  {
                if (this.closing)
                    this.opened = false;
            }, 500);
        }
    }

    private onClick() {
        this.opened = !this.opened;
        this.opening = false;
        this.closing = false;
    }
} 