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

import { Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Format } from 'src/app/datasets/shared/dataset.service';
import { GlobalService } from '../../services/global.service';
import { Option } from '../../select/select.component';

@Component({
    selector: 'download-setup-alt',
    templateUrl: 'download-setup-alt.component.html',
    styleUrls: ['download-setup-alt.component.css']
})

export class DownloadSetupAltComponent implements OnInit {

    @Output() go: EventEmitter<{format: Format, converter: number}> = new EventEmitter();
    @Output() close: EventEmitter<void> = new EventEmitter();
    form: UntypedFormGroup;
    @ViewChild('window') window: ElementRef;
    @Input() format: Format;
    @Input() converter: number;
    @Input() compatibilityMessage: boolean = true;

    formatOptions: Option<Format>[] = [
        new Option<Format>('dcm', 'Dicom', null, null, null),
        new Option<Format>('nii', 'Nifti', null, null, null),
    ];

    niftiConverters: Option<number>[] = [
        new Option<number>(1, 'DCM2NII_2008_03_31', null, null, null, false),
        new Option<number>(2, 'MCVERTER_2_0_7', null, null, null, false),
        new Option<number>(4, 'DCM2NII_2014_08_04', null, null, null, false),
        new Option<number>(5, 'MCVERTER_2_1_0', null, null, null, false),
        new Option<number>(6, 'DCM2NIIX', null, null, null, false),
        new Option<number>(7, 'DICOMIFIER', null, null, null, false),
        new Option<number>(8, 'MRICONVERTER', null, null, null, false),
    ];

    constructor(private formBuilder: UntypedFormBuilder, globalService: GlobalService) {
        globalService.onNavigate.subscribe(() => {
            this.cancel();
        });
    }

    ngOnInit(): void {
        this.form = this.buildForm();
    }

    private buildForm(): UntypedFormGroup {
        let formGroup = this.formBuilder.group({
            'format': [{value: this.format || 'dcm', disabled: this.format}, [Validators.required]],
            'converter': [{value: this.converter}],
        });
        return formGroup;
    }

    downloadNow() {
        this.go.emit({
            format: this.form.get('format').value,
            converter: (this.form.get('format').value == 'nii') ? this.form.get('converter').value : null,
            });
    }

    cancel() {
        this.close.emit();
    }

    @HostListener('click', ['$event'])
    onClick(clickEvent) {
        if (!this.window.nativeElement.contains(clickEvent.target)) {
            this.cancel();
        }
    }
}
