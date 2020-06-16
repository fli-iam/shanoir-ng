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

import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { Filter } from '../pageable.model';

@Component({
    selector: 'shanoir-table-search',
    templateUrl: 'search.component.html',
    styleUrls: ['search.component.css'],
})

export class TableSearchComponent implements OnChanges {

    @Input() columnDefs: any[];
    @Input() filter: Filter; 
    @Output() filterChange: EventEmitter<Filter> = new EventEmitter<Filter>();
    private searchableColumns: any[] = [];

    ngOnChanges(changes: SimpleChanges) {
        if (changes['columnDefs']) {
            this.computeSearchableColumns();
        }
    }

    private computeSearchableColumns(): any[] {
        if (!this.columnDefs) return [];
        let cols: any[] = [];
        for (let col of this.columnDefs) {
            if (col.type != "boolean" && col.type != "button") {
                cols.push(col);
            }
        }
        this.searchableColumns = cols;
    }

    private search() {
        this.filterChange.emit(this.filter);
    }

    public resetSearch(): void {
        this.filter.searchStr = "";
        this.filter.searchField = "";
        this.search();
    }
   
}