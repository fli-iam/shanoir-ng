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


@Component({
    selector: 'progress-bar',
    templateUrl: 'loading-bar.component.html',
    styleUrls: ['loading-bar.component.css']
})

export class LoadingBarComponent {

    @Input() progress: number = 0;
    @Input() text: string = "";
    @Input() unknownDownload: boolean = false;
    @Input() width: number = 200;

    @HostBinding('style.width') get pixelWidth() {
        return this.width + 'px';
    }

    @HostBinding('class.error') get isError() {
        return this.progress == -1;
    }

    @HostBinding('class.done') get isDone() {
        return this.progress == 1;
    }

    getProgressText(): string {
        if (this.progress == -1) {
            return this.text ? this.text : 'ERROR';
        }
        else if (this.unknownDownload) {
            return this.getSizeStr(this.progress);
        }
        else return Math.floor(this.progress * 100) + "%";
    }

    getSizeStr(size: number): string {

        const base: number = 1024;
        const units: string[] = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

        if(size == null || size == 0){
            return "0 " + units[0];
        }

        const exponent: number = Math.floor(Math.log(size) / Math.log(base));
        let value: number = parseFloat((size / Math.pow(base, exponent)).toFixed(2));
        let unit: string = units[exponent];

        return value + " " + unit;
    }
} 