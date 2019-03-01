import { Component, ElementRef } from '@angular/core';

import { menuSlideDown } from '../animations/animations';
import { ImagesUrlUtil } from '../utils/images-url.util';

@Component({
    selector: 'notifications',
    templateUrl: 'notifications.component.html',
    styleUrls: ['notifications.component.css'],
    animations: [menuSlideDown]
})

export class NotificationsComponent {

    private animate: number = 0;
    private isOpen: boolean = false;
    private nbProcess: number = 3;
    private nbDone: number = 2;
    private newProcess: boolean = false;
    private newDones: boolean = true;
    private ImagesUrlUtil = ImagesUrlUtil;

    constructor(public elementRef: ElementRef) {
        document.addEventListener('click', () => {
            if (!elementRef.nativeElement.contains(event.target)) {
                if (this.isOpen) this.close();
            }
        });
    }

    private toggle() {
        if (this.isOpen) this.close();
        else this.open();
    }

    private close() {
        this.isOpen = false;
    }

    private open() {
        this.isOpen = true;
    }

}