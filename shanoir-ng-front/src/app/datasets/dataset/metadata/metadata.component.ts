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
import {AfterViewInit, Component, Input, OnDestroy, ViewChild} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatasetService } from '../../shared/dataset.service';
import { DicomService } from '../../../study-cards/shared/dicom.service'
import {BreadcrumbsService, Step} from '../../../breadcrumbs/breadcrumbs.service';
import { Location } from '@angular/common';
import {FilterablePageable, Page, Pageable} from "../../../shared/components/table/pageable.model";
import {
    BrowserPaginEntityListComponent
} from "../../../shared/components/entity/entity-list.browser.component.abstract";
import {Center} from "../../../centers/shared/center.model";
import {TableComponent} from "../../../shared/components/table/table.component";
import {CenterService} from "../../../centers/shared/center.service";
import {EntityService} from "../../../shared/components/entity/entity.abstract.service";
import {ColumnDefinition} from "../../../shared/components/table/column.definition.type";
import {AcquisitionEquipment} from "../../../acquisition-equipments/shared/acquisition-equipment.model";
import {ShanoirError} from "../../../shared/models/error.model";
import {DicomMetadata} from "./dicom-metadata.model";
import {Task} from "../../../async-tasks/task.model";
import {BrowserPaging} from "../../../shared/components/table/browser-paging.model";
import {EntityListComponent} from "../../../shared/components/entity/entity-list.component.abstract";


@Component({
    selector: 'dicom-metadata',
    templateUrl: 'metadata.component.html',
    styleUrls: ['metadata.component.css']
})

export class MetadataComponent extends EntityListComponent<DicomMetadata> implements AfterViewInit, OnDestroy {

    metadata: DicomMetadata[] = [];

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(
        private datasetService: DatasetService,
        private activatedRoute: ActivatedRoute,
        private dicomService: DicomService,
        breadcrumbsService: BreadcrumbsService,
        private location: Location) {
            super('dicom-metadata');
            breadcrumbsService.nameStep('Dicom metadata');
    }

    private loadMetadata() {
        const id: number = +this.activatedRoute.snapshot.params['id'];
        return Promise.all([this.datasetService.downloadDicomMetadata(id), this.dicomService.getDicomTags()]).then(([data, tags]) => {
            if (!data) {
                return;
            }
            let metadata = Object.entries(data[0]);
            metadata.forEach(entry => {
                let met = new DicomMetadata();
                met.id = parseInt(entry[0], 16);
                let group = entry[0].toString().substring(0,4);
                let element = entry[0].toString().substring(4);
                met.tag = group + ',' + element;
                met.keyword = tags.find(tag => tag.code == met.id)?.label;
                met.value = entry[1]['Value']?.toString()
                if (met.value == '[object Object]') {
                    met.value = JSON.stringify(entry[1]['Value'], null, 3);
                }
                if(met.value){
                    this.metadata.push(met);
                }
            })
        });
    }
    getColumnDefs(): ColumnDefinition[] {
        let columnDefs: ColumnDefinition[] = [
            { headerName: 'Tag', field: 'tag', width: '100px'},
            { headerName: 'Keyword', field: 'keyword', width: '200px'},
            { headerName: 'Value', field: 'value', wrap: true }
        ];
        return columnDefs;
    }

    getOptions() {
        return {'new': false, 'edit': false, 'view': false, 'delete': false, 'reload': true, id: false};
    }

    getPage = (pageable: Pageable): Promise<Page<DicomMetadata>> => {
        return Promise.resolve(new BrowserPaging(this.metadata, this.columnDefs).getPage(pageable));
    }

    getCustomActionsDefs(): any[] {
        return [];
    }


    goBack() {
        this.location.back();
    }

    getService(): EntityService<DicomMetadata> {
        return undefined;
    }

    ngAfterViewInit(): void {
        this.loadMetadata().then(() => {
            this.table.maxResults = this.metadata.length
            this.table.refresh();
        })
    }
}
