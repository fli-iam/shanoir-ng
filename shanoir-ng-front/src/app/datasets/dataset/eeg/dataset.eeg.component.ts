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

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { EegDataset } from './dataset.eeg.model';
import { TableComponent } from '../../../shared/components/table/table.component';
import { BrowserPaging } from '../../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../../shared/components/table/pageable.model';
import{ Channel }from '../eeg/dataset.eeg.model';

@Component({
    selector: 'eeg-dataset-details',
    templateUrl: 'dataset.eeg.component.html'
})

export class EegDatasetComponent implements OnInit  {

    @Input() protected mode: Mode;
    @Input() public dataset: EegDataset;
    @ViewChild('channelsTable') table: TableComponent;

    public columnDefs: any[];

    private browserPaging: BrowserPaging<Channel>;
    private channelPromise: Promise<any>;

    ngOnInit(): void {

        function checkNullValue(value: any) {
            if (value) {
                return value;
            }
            return '';
        };

        this.columnDefs = [
           {headerName: 'Name', field: 'name', type: 'string', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.name);
            }},
            {headerName: 'Resolution', field: 'resolution', type: 'string', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.resolution);
            }},
            {headerName: 'Units', field: 'referenceUnits', type: 'string', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.referenceUnits);
            }},
            {headerName: 'Type', field: 'referenceType', type: 'string', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.referenceType);
            }},
            {headerName: 'Position', field: 'position', type: 'string', cellRenderer: (params: any) => {
                    if (params.data.x == null && params.data.z == null && params.data.y == null) {
                        return 'N/A'
                    } else {
                        return params.data.x + ' ' + params.data.y + ' ' + params.data.z;
                    }
            }},
            {headerName: 'low cutoff', field: 'lowCutoff', type: 'number', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.lowCutoff);
            }},
            {headerName: 'High cutoff', field: 'highCutoff', type: 'number', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.highCutoff);
            }},
            {headerName: 'Notch', field: 'notch', type: 'number', cellRenderer: (params: any) => {
                    return checkNullValue(params.data.notch);
            }}
        ];

        this.channelPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging([], this.columnDefs);
            this.browserPaging.setItems(this.dataset.channels);
            this.table.refresh();
        });
    }

   getPage(pageable: FilterablePageable): Promise<Page<Channel>> {
        return new Promise((resolve) => {
            this.channelPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }
}