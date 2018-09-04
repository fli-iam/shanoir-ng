import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { Filter } from '../pageable.model';

@Component({
    selector: 'shanoir-table-search',
    templateUrl: 'search.component.html',
    styleUrls: ['search.component.css'],
})

export class TableSearchComponent implements OnChanges {

    @Input() columnDefs: any[];
    @Output() onChange: EventEmitter<Filter> = new EventEmitter<Filter>();
    private searchField: string;
    private searchStr: string;
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
        this.onChange.emit(new Filter(this.searchStr, this.searchField));
    }

    public resetSearch(): void {
        this.searchStr = "";
        this.searchField = "";
        this.search();
    }
   
}