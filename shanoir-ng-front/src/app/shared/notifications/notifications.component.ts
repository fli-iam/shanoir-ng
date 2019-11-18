/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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

    animate: number = 0;
    isOpen: boolean = false;
    nbProcess: number = 3;
    nbDone: number = 2;
    newProcess: boolean = false;
    newDones: boolean = true;
    ImagesUrlUtil = ImagesUrlUtil;

    constructor(public elementRef: ElementRef) {
        document.addEventListener('click', () => {
            if (!elementRef.nativeElement.contains(event.target)) {
                if (this.isOpen) this.close();
            }
        });
    }

    toggle() {
        if (this.isOpen) this.close();
        else this.open();
    }

    close() {
        this.isOpen = false;
    }

    open() {
        this.isOpen = true;
    }

}