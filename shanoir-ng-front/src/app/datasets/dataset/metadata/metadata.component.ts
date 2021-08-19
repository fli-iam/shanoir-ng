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
import { ActivatedRoute } from '@angular/router';
import { DatasetService } from '../../shared/dataset.service';
import { DicomService } from '../../../study-cards/shared/dicom.service'
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { Location } from '@angular/common';


@Component({
    selector: 'dicom-metadata',
    templateUrl: 'metadata.component.html',
    styleUrls: ['metadata.component.css']
})

export class MetadataComponent {

    metadataArr: {}[];
    

    constructor(
            private datasetService: DatasetService, 
            private activatedRoute: ActivatedRoute,
            private dicomService: DicomService,
            breadcrumbsService: BreadcrumbsService,
            private location: Location) {

        breadcrumbsService.nameStep('Dicom metadata');
        this.loadMetadata();
    }

    private loadMetadata() {
        const id: number = +this.activatedRoute.snapshot.params['id'];
        Promise.all([this.datasetService.downloadDicomMetadata(id), this.dicomService.getDicomTags()]).then(([data, tags]) => {
            let metadata = Object.entries(data[0]);
            metadata.forEach(entry => {
                const entryCode: number = parseInt(entry[0], 16);
                entry[1]['tagLabel'] = tags.find(tag => tag.code == entryCode)?.label;
                if (entry[1]['Value']?.toString() == '[object Object]') entry[1]['Value'] = JSON.stringify(entry[1]['Value']);
            })
            this.metadataArr = metadata;
        });
    }

    goBack() {
        this.location.back();
    }
}