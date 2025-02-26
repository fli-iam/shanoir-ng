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
import {Component, OnDestroy, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {DatasetService} from '../../shared/dataset.service';
import {DicomService} from '../../../study-cards/shared/dicom.service'
import {BreadcrumbsService} from '../../../breadcrumbs/breadcrumbs.service';
import {Location} from '@angular/common';
import {Page, Pageable} from "../../../shared/components/table/pageable.model";
import {TableComponent} from "../../../shared/components/table/table.component";
import {ColumnDefinition} from "../../../shared/components/table/column.definition.type";
import {DicomMetadata} from "./dicom-metadata.model";
import {BrowserPaging} from "../../../shared/components/table/browser-paging.model";
import { Selection, TreeService } from 'src/app/studies/study/tree.service';
import { Subscription } from 'rxjs';


@Component({
    selector: 'dicom-metadata',
    templateUrl: 'metadata.component.html',
    styleUrls: ['metadata.component.css'],
    standalone: false
})

export class MetadataComponent implements OnDestroy {

    metadata: DicomMetadata[] = [];
    columnDefs: ColumnDefinition[];
    subscriptions: Subscription[] = [];

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(
            private datasetService: DatasetService,
            private activatedRoute: ActivatedRoute,
            private dicomService: DicomService,
            private breadcrumbsService: BreadcrumbsService,
            private location: Location,
            private treeService: TreeService) {

        this.subscriptions.push(this.activatedRoute.params.subscribe(
            params => {
                this.treeService.activateTree(this.activatedRoute); // at each routing event
                breadcrumbsService.nameStep('Dicom metadata');
                const id = +params['id'];
                this.metadata = [];
                this.loadMetadata(id).then(() => {
                    this.table.maxResults = this.metadata.length
                    this.table.refresh();
            })
        }));
        this.columnDefs = this.getColumnDefs();
    }
    ngOnDestroy(): void {
        this.subscriptions.forEach(s => s.unsubscribe());
    }

    private loadMetadata(id: number) {
        this.datasetService.get(id, 'lazy').then(ds => {
            this.treeService.select(new Selection(id, 'dicomMetadata', [ds.study.id], ds));
        });

        return Promise.all([this.datasetService.downloadDicomMetadata(id), this.dicomService.getDicomTags()]).then(([data, tags]) => {
            if (!data) {
                return;
            }
            let metadata = Object.entries(data[0]);
            metadata.forEach(entry => {

                let met = new DicomMetadata();

                let group = entry[0].toString().substring(0,4);
                let element = entry[0].toString().substring(4);
                met.tag = group + ',' + element;

                let code = parseInt(entry[0], 16);
                met.keyword = tags.find(tag => tag.code == code)?.label;

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
        return [
            {headerName: 'Tag', field: 'tag', width: '100px'},
            {headerName: 'Keyword', field: 'keyword', width: '200px'},
            {headerName: 'Value', field: 'value', wrap: true}
        ];
    }

    getPage = (pageable: Pageable): Promise<Page<DicomMetadata>> => {
        return Promise.resolve(new BrowserPaging(this.metadata, this.columnDefs).getPage(pageable));
    }


    goBack() {
        this.location.back();
    }
}
