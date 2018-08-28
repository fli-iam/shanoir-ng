import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Filter } from '../pageable.model';

@Component({
    selector: 'shanoir-table-search',
    templateUrl: 'search.component.html',
    styleUrls: ['search.component.css'],
})

export class TableSearchComponent {

    @Input() columnDefs: any[];
    @Output() onChange: EventEmitter<Filter> = new EventEmitter<Filter>();
    public searchField: string;
    public searchStr: string;

    private getSearchableColumns(): any[] {
        let cols: any[] = [];
        for (let col of this.columnDefs) {
            if (col.type != "boolean" && col.type != "button") {
                cols.push(col);
            }
        }
        return cols;
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