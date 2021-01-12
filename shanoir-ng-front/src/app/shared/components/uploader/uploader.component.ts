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

import { Component, ViewChild, ElementRef, Output, EventEmitter, Input } from '@angular/core';
import { ImagesUrlUtil } from '../../utils/images-url.util';


@Component({
    selector: 'upload-file',
    templateUrl: 'uploader.component.html',
    styleUrls: ['uploader.component.css']
})
export class UploaderComponent {

    @ViewChild('input', { static: false }) private fileInput: ElementRef;
    @Output() fileChange = new EventEmitter<any>();
    @Input() loading: boolean = false;
    @Input() error: boolean = false;
    readonly ImagesUrlUtil = ImagesUrlUtil;
    filename: string;
    
    click() {
        this.fileInput.nativeElement.click();
    }

    changeFile(file: any) {
        this.filename = undefined;
        if (file && file.target && file.target.files && file.target.files[0]) 
            this.filename = file.target.files[0].name;
        else this.filename = null;
        this.fileChange.emit(file);
    }

}