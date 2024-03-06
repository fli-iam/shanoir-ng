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
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { Format } from 'src/app/datasets/shared/dataset.service';
import { GlobalService } from '../../services/global.service';
import { Option } from '../../select/select.component';
import { DownloadOptions } from '../mass-download.service';
import { AngularDeviceInformationService } from 'angular-device-information';

@Component({
    selector: 'download-setup',
    templateUrl: 'download-setup.component.html',
    styleUrls: ['download-setup.component.css']
})

export class DownloadSetupComponent implements OnInit {

    @Output() go: EventEmitter<DownloadOptions> = new EventEmitter();
    @Output() close: EventEmitter<void> = new EventEmitter();
    form: UntypedFormGroup;
    @Input() format: Format; 
    @Input() options: DownloadSetupOptions = new DownloadSetupOptions();
    @ViewChild('window') window: ElementRef;
    formatOptions: Option<Format>[] = [
        new Option<Format>('dcm', 'Dicom', null, null, null, !this.options.hasDicom),
        new Option<Format>('nii', 'Nifti', null, null, null, !this.options.hasNii),
        new Option<Format>('eeg', 'EEG', null, null, null, !this.options.hasEeg),
        new Option<Format>('BIDS', 'BIDS', null, null, null, !this.options.hasBids),
    ];
    winOs: boolean;
        
    constructor(private formBuilder: UntypedFormBuilder, globalService: GlobalService, deviceInformationService: AngularDeviceInformationService) {
        globalService.onNavigate.subscribe(() => {
            this.cancel();
        });
        this.winOs = deviceInformationService.getDeviceInfo()?.os?.toLocaleLowerCase().includes('windows');
    }

    ngOnInit(): void {
        this.form = this.buildForm();
    }

    private buildForm(): UntypedFormGroup {
        let formGroup = this.formBuilder.group({
            'format': [{value: this.format || 'dcm', disabled: this.format}, [Validators.required]],
            'nbQueues': [4, [Validators.required, Validators.min(1), Validators.max(1024)]],
            'unzip': [false, []],
        });
        if (this.winOs) {
            formGroup.addControl('shortPath', new UntypedFormControl(false));
        }
        return formGroup;
    }

    downloadNow() {
        let options: DownloadOptions = new DownloadOptions(this.form.get('format').value);
        options.nbQueues = this.form.get('nbQueues').value;
        options.unzip = this.form.get('unzip').value;
        if (this.form.get('shortPath')) options.shortPath = this.form.get('shortPath').value;
        this.go.emit(options);
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

export class DownloadSetupOptions {
    hasDicom?: boolean = true;
    hasNii?: boolean = true;
    hasEeg?: boolean = false;
    hasBids?: boolean = false;
}