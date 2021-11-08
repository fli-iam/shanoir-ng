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

import { Component } from '@angular/core';


@Component({
    selector: 'tool-tip',
    templateUrl: 'tooltip.component.html',
    styleUrls: ['tooltip.component.css']
})

export class TooltipComponent {

    opened: boolean = false;
    private opening: boolean = false;
    private closing: boolean = false;

    onOver() {
        if (!this.opening) {
            this.closing = false;
            this.opening = true;
            setTimeout(() =>  {
                if (this.opening)
                    this.opened = true;
            }, 500);
        }
    }

    onLeave() {
        if (!this.closing) {
            this.closing = true;
            this.opening = false;
            setTimeout(() =>  {
                if (this.closing)
                    this.opened = false;
            }, 500);
        }
    }

    onClick() {
        this.opened = !this.opened;
        this.opening = false;
        this.closing = false;
    }
} 