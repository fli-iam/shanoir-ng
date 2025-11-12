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

import { Component, HostBinding, Input } from '@angular/core';

import { getSizeStr } from 'src/app/utils/app.utils';
import { NgIf } from '@angular/common';


@Component({
    selector: 'progress-bar',
    templateUrl: 'loading-bar.component.html',
    styleUrls: ['loading-bar.component.css'],
    imports: [NgIf]
})

export class LoadingBarComponent {

    @Input() progress: number = 0;
    @HostBinding('class.warning') @Input() warning: boolean = false;
    @Input() text: string = "";
    @Input() unknownDownload: boolean = false;
    @Input() width: number = 200;
    Math = Math;

    @HostBinding('style.width') get pixelWidth() {
        return this.width + 'px';
    }

    @HostBinding('class.error') get isError() {
        return this.progress == -1;
    }

    @HostBinding('class.done') get isDone() {
        return this.progress == 1 && !this.warning;
    }

    getProgressText(): string {
        if (this.progress == -1) {
            return this.text ? this.text : 'ERROR';
        }
        else if (this.unknownDownload || this.progress > 1) {
            return this.getSizeStr(this.progress);
        }
        else return Math.ceil(this.progress * 100) + "%";
    }

    getSizeStr(size: number): string {
        return getSizeStr(size);
    }
} 