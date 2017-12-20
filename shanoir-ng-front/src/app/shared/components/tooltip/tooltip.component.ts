import { Component } from '@angular/core';


@Component({
    selector: 'tool-tip',
    templateUrl: 'tooltip.component.html',
    styleUrls: ['tooltip.component.css']
})

export class TooltipComponent {

    private opened: boolean = false;

    public open() {
        this.opened = true;
    }

    public close() {
        this.opened = false;
    }
} 