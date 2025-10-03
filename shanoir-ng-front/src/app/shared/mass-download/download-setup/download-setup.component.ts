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

import { Component, ElementRef, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { AngularDeviceInformationService } from 'angular-device-information';
import { Subscription } from 'rxjs';

import { DatasetLight, DatasetService, Format } from 'src/app/datasets/shared/dataset.service';

import { DatasetType } from "../../../datasets/shared/dataset-type.model";
import { Dataset } from "../../../datasets/shared/dataset.model";
import { Option } from '../../select/select.component';
import { GlobalService } from '../../services/global.service';
import { DownloadInputIds, DownloadSetup } from '../mass-download.service';

@Component({
    selector: 'download-setup',
    templateUrl: 'download-setup.component.html',
    styleUrls: ['download-setup.component.css'],
    standalone: false
})

export class DownloadSetupComponent implements OnInit, OnDestroy {

    @Output() go: EventEmitter<DownloadSetup> = new EventEmitter();
    @Output() close: EventEmitter<void> = new EventEmitter();
    @Input() inputIds: DownloadInputIds;
    @Input() totalSize?: number;
    form: UntypedFormGroup;
    loading: boolean;
    loaded: boolean = false;
    format: Format;
    converter: number;
    datasets: Dataset[] | DatasetLight[];
    hasDicom: boolean = false;
    formatOptions: Option<Format>[] = [
        new Option<Format>('dcm', 'Dicom', null, null, null, false),
        new Option<Format>('nii', 'Nifti', null, null, null, false),
    ];
    niftiConverters: Option<number>[] = [
        new Option<number>(null, 'Existing nifti (if available)', null, null, null, false),
        new Option<number>(1, 'Dcm2nii_2008_03_31', null, null, null, false),
        new Option<number>(2, 'McVerter_2_0_7', null, null, null, false),
        new Option<number>(4, 'Dcm2nii_2014_08_04', null, null, null, false),
        new Option<number>(5, 'McVerter_2_1_0', null, null, null, false),
        new Option<number>(6, 'Dcm2niix', null, null, null, false),
        new Option<number>(7, 'Dicomifier', null, null, null, false),
        new Option<number>(8, 'MriConverter', null, null, null, false),
    ];
    winOs: boolean;
    @ViewChild('window') window: ElementRef;
    protected subscriptions: Subscription[] = [];

    constructor(
            private formBuilder: UntypedFormBuilder,
            globalService: GlobalService,
            deviceInformationService: AngularDeviceInformationService,
            private datasetService: DatasetService) {

        globalService.onNavigate.subscribe(() => {
            this.cancel();
        });
        this.winOs = deviceInformationService.getDeviceInfo()?.os?.toLocaleLowerCase().includes('windows');
        this.form = this.buildForm();
    }

    ngOnInit(): void {
        if (this.inputIds) {
            let fetchDatasets: Promise<Dataset[] | DatasetLight[]>;
            if (this.inputIds.studyId) {
                if (this.inputIds.subjectId) {
                    fetchDatasets = this.datasetService.getByStudyIdAndSubjectId(this.inputIds.studyId, this.inputIds.subjectId);
                } else {
                    fetchDatasets = this.datasetService.getByStudyId(this.inputIds.studyId);
                }
            } else if (this.inputIds.examinationId) {
                fetchDatasets = this.datasetService.getByExaminationId(this.inputIds.examinationId);
            } else if (this.inputIds.acquisitionId) {
                fetchDatasets = this.datasetService.getByAcquisitionId(this.inputIds.acquisitionId);
            } else if (this.inputIds.datasetIds) {
                fetchDatasets = this.datasetService.getByIds(new Set(this.inputIds.datasetIds));
            }
            if (fetchDatasets) {
                this.loading = true;
                fetchDatasets.then(
                    datasetsResult => {
                        this.datasets = datasetsResult;
                        this.hasDicom = this.hasDicomInDatasets(this.datasets);
                    }
                ).finally(() => {
                    this.loading = false;
                    this.loaded = true;
                });
            }
        }
        // Empty converter by default
        this.converter = 6;
    }

    private buildForm(): UntypedFormGroup {
        let formGroup = this.formBuilder.group({
            'format': [{value: this.format || 'dcm', disabled: this.format}, [Validators.required]],
            'converter': [{value: this.converter}],
            'nbQueues': [4, [Validators.required, Validators.min(1), Validators.max(1024)]],
            'unzip': [false],
            'subjectFolders': [true],
            'examinationFolders': [true],
            'acquisitionFolders': [false],
            'datasetFolders': [false]
        });
        if (this.winOs) {
            formGroup.addControl('shortPath', new UntypedFormControl(false));
        }
        this.subscriptions.push(formGroup.get('unzip').valueChanges.subscribe(val => {
            formGroup.get('datasetFolders').setValue(val);
        }));
        return formGroup;
    }

    downloadNow() {
        let setup: DownloadSetup = new DownloadSetup(this.form.get('format').value);
        setup.nbQueues = this.form.get('nbQueues').value;
        setup.unzip = this.form.get('unzip').value;
        setup.subjectFolders = this.form.get('subjectFolders').value;
        setup.examinationFolders = this.form.get('examinationFolders').value;
        setup.acquisitionFolders = this.form.get('acquisitionFolders').value;
        setup.datasetFolders = this.form.get('datasetFolders').value;
        setup.converter = (this.form.get('format').value == 'nii') ? this.form.get('converter')?.value : null;
        if (this.form.get('shortPath')) setup.shortPath = this.form.get('shortPath').value;
        setup.datasets = this.datasets;
        this.go.emit(setup);
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
    // This method checks if the list of given datasets has dicom or not.
    private hasDicomInDatasets(datasets: {type: DatasetType, hasProcessings: boolean}[]) {
        for (let dataset of datasets) {
            if (dataset.type != DatasetType.Eeg && dataset.type != DatasetType.BIDS && dataset.type != DatasetType.Generic) {
                return true;
            }
        }
        return false;
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }

}
