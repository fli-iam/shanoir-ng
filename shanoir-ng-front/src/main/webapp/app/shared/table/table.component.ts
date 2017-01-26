import { Component, Input } from '@angular/core';

@Component({
    selector: 'shanoir-table',
    moduleId: module.id,
    templateUrl: 'table.component.html',
    styleUrls: ['../css/common.css', 'table.component.css'],
})

export class TableComponent {
    @Input() columnDefs: any[];
    @Input() items: Object[];
    private itemsSave: Object[];
    private itemsLoaded: boolean = false;

    public lastSortedCol: String = "";
    public lastSortedAsc: boolean = true;
    public searchField: String = "";
    public searchStr: String;

    constructor() {
    }

    /**
     * Sort items by col, then by id
     */
    public sortBy(col: Object): void {
        // Some columns are incompatible with sorting
        if (col["suppressSorting"] || col["type"] == "button") {
            return;
        }

        let field: string = col["field"];
        let defaultAsc: boolean = true;
        let asc: boolean =  field == this.lastSortedCol ? !this.lastSortedAsc : defaultAsc;
        this.lastSortedCol = field;
        this.lastSortedAsc = asc;
        let negInf;
        /* Sort function */
        this.items.sort((n1,n2) => {
            let cell1 = this.getCellValue(n1, col);
            let cell2 = this.getCellValue(n2, col);
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
            // Regarding the data type, we set a neg infinity because unless this, 
            // null values can't be compared
            if (negInf == undefined) {
                switch (typeof cell1 != null ? cell1 : cell2) {
                    case "number": 
                        negInf = -1*Infinity;
                        break;
                    case "date":
                        negInf = new Date();
                        break;
                    default:
                        negInf = "";
                }
            }
            if (cell1 == null) cell1 = negInf;
            if (cell2 == null) cell2 = negInf;
            // Comparison
            if (cell1 > cell2) {
                return asc ? 1 : -1;
            }
            else if (cell1 < cell2) {
                return asc ? -1 : 1;
            }
        });
    }

    /**
     * Filter items by a search string
     */
    public search(): void {
        if (this.searchStr == undefined) {
            return;
        }
        let searchStr: string = this.searchStr.toLowerCase().trim();
        if (this.items == undefined /*|| (searchStr.length > 0 && searchStr.length < 3)*/) {
            return;
        }
        // Here we need to save the items so we will be able to reset the filter.
        // Do it only once. 
        // Not possible to do it on initialisation because item list can be asynchronous.
        if (!this.itemsLoaded) {
            this.itemsLoaded = true;
            this.itemsSave = [];
            for (let item of this.items) { this.itemsSave.push(item) }
        }

        this.items = [];
        // If seacrh string empty, reset the filter
        if (searchStr.length == 0) {
            for (let item of this.itemsSave) { this.items.push(item); }
            return;
        }
        // Inspect every field and same the item if one field matches
        for (let item of this.itemsSave) {
            for (let col of this.columnDefs) {
                let value: any = this.getCellValue(item, col);
                if (!this.isValueBoolean(value) && col["type"] != "button") {
                    let valueStr: string = this.renderCell(item, col);
                    if (this.searchField == "" || col.field == this.searchField) {
                        if (value != undefined && value != null) {
                            valueStr = valueStr.toLowerCase().trim();
                            if (valueStr.indexOf(searchStr) >= 0) {
                                this.items.push(item);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Reset the search
     */
    public resetSearch(): void {
        // If seacrh string empty, reset the filter
        if (this.itemsLoaded) {
            this.items = [];
            let itemsTmp = [];
            for (let item of this.itemsSave) { 
                this.items.push(item); 
            }
        }
        this.searchStr = "";
    }

    /**
     * Get a cell content, resolving a rendrer if necessary
     */
    public getCellValue(item: Object, col: any): any {
        let result: any;
        if (col.field == undefined) {
            return null;
        } if (col.hasOwnProperty("cellRenderer")) {
            let params = new Object();
            params["data"] = item;
            return col["cellRenderer"](params);
        } else {
            return this.getFieldRawValue(item, col["field"]);
        }
    }

    /**
     * Just get the field value, but not using any renderer!
     */
    private getFieldRawValue(obj: Object, path: string): any {
        function index(robj, i) {return robj[i]}; 
        return path.split('.').reduce( index, obj ); 
    }
    
    /**
     * Convert a cell content to a displayable string
     */
    public renderCell(item: Object, col: any): string {
        let result: any = this.getCellValue(item, col);
        if (result == null || this.isValueBoolean(result)) { 
            return "";
        } else {
            return "" + result;
        }
    }

    /**
     * Test if a cell content is a boolean
     */
    private isFieldBoolean(obj: Object, col: any): boolean {
        let val = this.getCellValue(obj, col);
        return this.isValueBoolean(val);
    }

    /**
     * Test if a value is a boolean
     */
    private isValueBoolean(val: any): boolean {
        return val != undefined
            && val != null
            && (typeof val == "boolean" 
            || typeof val == "Boolean");
    }

    /**
     * Get a column type attribute
     */
    private getColType(col: any): string {
        if (col.type != undefined) {
            return col.type;
        } else {
            return null;
        }
    }

    /**
     * Get a column type and format it to be used a dom element class
     */
    private getColTypeStr(col: any): string {
        let type: string = this.getColType(col) ;
        return type != null ? "col-"+type : "";
    }

    /** 
     * Get a cell type and format it to be used a dom element class
     */
    private getCellTypeStr(col: any): string {
        let type: string = this.getColType(col) ;
        return type != null ? "cell-"+type : "";
    }

    /**
     * Get the columns that can be used for searching
     */
    private getSearchableColumns(): any[] {
        let cols: any[] = [];
        for (let col of this.columnDefs) {
            if (col.type != "boolean" && col.type != "button") {
                cols.push(col);
            }
        }
        return cols;
    }
}