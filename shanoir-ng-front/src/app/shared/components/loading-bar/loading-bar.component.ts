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

import { Component, Input } from '@angular/core';


@Component({
    selector: 'progress-bar',
    templateUrl: 'loading-bar.component.html',
    styleUrls: ['loading-bar.component.css']
})

export class LoadingBarComponent {

    @Input() progress: number = 0;
    @Input() text: string = "";
    width: number = 200;

    getProgressText(): string {
        if (this.progress === -1 && this.text) {
            return this.text;
        }
        return Math.floor(this.progress * 100) + "%";
    }

    onResized(event) {
    }

} 