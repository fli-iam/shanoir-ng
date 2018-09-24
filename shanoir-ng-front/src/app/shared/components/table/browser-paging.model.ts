import { Pageable, Page, Sort, FilterablePageable, Filter } from './pageable.model';
import { TableComponent } from './table.component';

export class BrowserPaging<T> {

    private lastSort: Sort;

    constructor(
            private items: T[],
            private columnDefs: any[]) {
        if (!this.items) throw Error('items cannot be null !');
    }

    public setItems(items: T[]) {
        this.items = items;
    }
    
    public setColumnDefs(columnDefs: any[]) {
        this.columnDefs = columnDefs;
    }

    public getPage(pageable: FilterablePageable): Page<T> {
        if ((!this.lastSort || !this.lastSort.equals(pageable.sort)) 
                && pageable.sort && pageable.sort.orders && pageable.sort.orders.length > 0 ) {
            this.lastSort = pageable.sort;
            this.items = this.sortItems(this.items, pageable); // SORT
        }
        let filtered: T[] = this.filter(this.items, pageable.filter); // FILTER
        let result: T[] = this.slice(filtered, pageable); // SLICE
        
        let page: Page<T> = new Page();
        page.content = result;
        page.number = pageable.pageNumber;
        page.size = pageable.pageSize;
        page.numberOfElements = filtered.length;
        page.totalElements = this.items.length;
        page.totalPages = Math.ceil(page.numberOfElements/page.size);
        return page;
    }

    private slice(items: T[], pageable: Pageable): T[] {
        if (items.length == 0) return items;
        let start: number = (pageable.pageNumber-1) * pageable.pageSize;
        let end: number = +start + +pageable.pageSize;
        return items.slice(start, end);
    }

    private sortItems(items: T[], pageable: Pageable) {
        let col: any;
        for (let column of this.columnDefs) {
            if (column.field == pageable.sort.orders[0].property) {
                col = column;
                break;
            }
        }
        if (!col) throw Error('cannot find a corresponding column to sort');

        return this.sortItemsByCol(items, col, pageable.sort.orders[0].direction == 'ASC');
    }

    /**
     * Sort items by col, then by id
     */
    private sortItemsByCol(items: T[], col: any, asc: boolean): T[] {
        // Some columns are incompatible with sorting
        if (col["suppressSorting"] || col["type"] == "button") {
            return;
        }
        // Regarding the data type, we set a neg infinity because unless this, 
        // null values can't be compared
        let negInf: any;
        switch (col["type"]) {
            case "number":
                negInf = -1 * Infinity;
                break;
            case "date":
                negInf = new Date(0);
                break;
            default:
                negInf = "";
        }
        /* Sort function */
        items.sort((n1, n2) => {
            let cell1 = TableComponent.getCellValue(n1, col);
            let cell2 = TableComponent.getCellValue(n2, col);
            if (col["type"] == "date") {
                // Real value for date
                cell1 = TableComponent.getFieldRawValue(n1, col["field"])
                cell2 = TableComponent.getFieldRawValue(n2, col["field"])
            }
            // If equality, test the id so the order is always the same
            if (cell1 == cell2) {
                if (n1["id"] != undefined) {
                    if (n1["id"] > n2["id"]) {
                        return 1;
                    } else if (n1["id"] < n2["id"]) {
                        return -1;
                    }
                }
                return 0;
            }

            if (cell1 == null) {
                cell1 = negInf;
            } else if (typeof cell1 == 'string') {
                cell1 = cell1.toLowerCase();
            }
            if (cell2 == null) {
                cell2 = negInf;
            } else if (typeof cell2 == 'string') {
                cell2 = cell2.toLowerCase();
            }

            // Comparison
            if (cell1 > cell2) {
                return asc ? 1 : -1;
            } else {
                return asc ? -1 : 1;
            }
        });
        return items;
    }


    /**
     * Filter items by a search string
     */
    private filter(items: T[], filter: Filter): T[] {
        if(!filter) return items;
        let searchStr: string = filter.searchStr;
        let searchField: string = filter.searchField;
        if (!searchStr) return items;
        if (typeof searchStr != 'string') searchStr = searchStr + "";
        searchStr = searchStr.toLowerCase().trim();
        if (searchStr.length == 0) return items;
        // Inspect every field and save the item if one field matches
        let result = [];
        for (let item of items) {
            for (let col of this.columnDefs) {
                let value: any = TableComponent.getCellValue(item, col);
                if (value && typeof value != 'boolean' && col["type"] != "button") {
                    let valueStr: string = TableComponent.getCellValue(item, col);
                    if (!searchField || searchField == "" || col.field == searchField) {
                        if (value && valueStr) {
                            if (typeof valueStr != 'string') valueStr = valueStr + "";
                            valueStr = valueStr.toLowerCase().trim();
                            if (valueStr.indexOf(searchStr) >= 0) {
                                result.push(item);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}