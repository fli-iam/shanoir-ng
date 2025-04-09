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

import { Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { DatasetLight, DatasetService, Format } from 'src/app/datasets/shared/dataset.service';
import { DatasetType } from "../../../datasets/shared/dataset-type.model";
import { Dataset } from "../../../datasets/shared/dataset.model";
import { Option } from '../../select/select.component';
import { GlobalService } from '../../services/global.service';
import { DownloadInputIds } from '../mass-download.service';

@Component({
    selector: 'download-setup-alt',
    templateUrl: 'download-setup-alt.component.html',
    styleUrls: ['download-setup-alt.component.css'],
    standalone: false
})

export class DownloadSetupAltComponent implements OnInit {

    @Output() go: EventEmitter<{format: Format, converter: number, datasets: Dataset[] | DatasetLight[]}> = new EventEmitter();
    @Output() close: EventEmitter<void> = new EventEmitter();
    @Input() inputIds: DownloadInputIds;
    form: UntypedFormGroup;
    @ViewChild('window') window: ElementRef;
    loading: boolean;
    format: Format;
    converter: number;
    datasets: Dataset[] | DatasetLight[];
    hasDicom: boolean = false;

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

    constructor(private formBuilder: UntypedFormBuilder,
                globalService: GlobalService,
                private datasetService: DatasetService) {
        globalService.onNavigate.subscribe(() => {
            this.cancel();
        });
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
                });
            }
        }
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
            datasets: this.datasets
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

    // This method checks if the list of given datasets has dicom or not.
    private hasDicomInDatasets(datasets: Dataset[] | DatasetLight[]) {
        for (let dataset of datasets) {
            if (dataset.type != DatasetType.Eeg && dataset.type != DatasetType.BIDS && !dataset.hasProcessings) {
                return true;
            }
        }
        return false;
    }
}
