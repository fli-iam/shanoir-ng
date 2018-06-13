import { Component, EventEmitter, Input, Output } from '@angular/core';

import { ImagesUrlUtil } from '../../utils/images-url.util';
import { Pageable } from './pageable.model';

@Component({
    selector: 'shanoir-table',
    templateUrl: 'table.component.html',
    styleUrls: ['table.component.css'],
})

export class TableComponent {
    @Input() backPagination: boolean = false;
    @Input() columnDefs: any[];
    @Input() items: Object[];
    @Input() nbAllItems: number;
    @Input() customActionDefs: any[];
    @Input() rowClickAction: Object;
    @Input() selectionAllowed: boolean = false;
    @Output() queryListEvent = new EventEmitter();
    private itemsSave: Object[];
    private itemsLoaded: boolean = false;
    private maxResultsField: number;

    private checkAllImageUrl: string = ImagesUrlUtil.CHECK_ALL_ICON_PATH;
    private loaderImageUrl: string = ImagesUrlUtil.LOADER_IMAGE_PATH;
    private uncheckAllImageUrl: string = ImagesUrlUtil.UNCHECK_ALL_ICON_PATH;

    public isLoading: boolean = false;
    public maxResults: number = 20;
    public lastSortedCol: Object = null;
    public lastSortedAsc: boolean = true;
    public searchField: String = "";
    public searchStr: String;
    public currentPage: number = 1;

    constructor() {
        this.maxResultsField = this.maxResults;
    }

    /**
     * The parent must bind a loading boolean to tell the table that the item list is loading, then loaded.
     * Setting it to true then false must be repeated every time the item list is updated by the parent.
     * 
     * DONT'T MODIFY this.loading IN THE TABLE COMPONENT! Instead use this.isLoading to display and hide the loader.
     */
    @Input()
    set loading(loading: boolean) {
        if (this.isLoading && !loading) {
            if (this.items != undefined) {
                this.itemsSave = [];
                for (let item of this.items) { this.itemsSave.push(item); }
                // Choose default sorting
                this.lastSortedCol = this.columnDefs[0];
                for (let col of this.columnDefs) {
                    if (col.defaultSortCol != undefined) {
                        this.lastSortedCol = col;
                        this.lastSortedAsc = col["defaultAsc"] != undefined ? col["defaultAsc"] : true;
                        break;
                    }
                }
                this.itemsLoaded = true;
                this.refreshSorting();
            } else {
                this.itemsLoaded = false;
            }
        } else if (!this.isLoading && loading) {
            this.itemsLoaded = false;
        }
        this.isLoading = loading;
    }

    /**
     * Do a search and display results
     */
    public search(): void {
        if (!this.itemsLoaded) { return; }
        if (this.searchStr == undefined) { return; }
        this.isLoading = true;
        this.goToPage(1);
        this.filter();
        this.refreshSorting();
        this.isLoading = false;
    }

    /**
     * Sort items by col, then by id
     */
    public sortBy(col: Object): void {
        let defaultAsc: boolean = col["defaultAsc"] != undefined ? col["defaultAsc"] : true;
        let asc: boolean = col == this.lastSortedCol ? !this.lastSortedAsc : defaultAsc;
        this.sortByOrderBy(col, asc);
        this.goToPage(1);
    }

    /**
     * Re-sort with last terms
     */
    private refreshSorting(): void {
        this.sortByOrderBy(this.lastSortedCol, this.lastSortedAsc);
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
        this.searchField = "";
        this.refreshSorting();
        this.goToPage(1);
    }

    /**
     * Sort items by col, then by id
     */
    public sortByOrderBy(col: Object, asc: boolean) {
        if (!this.itemsLoaded) { return; }
        // Some columns are incompatible with sorting
        if (col["suppressSorting"] || col["type"] == "button") {
            return;
        }
        this.lastSortedCol = col;
        this.lastSortedAsc = asc;

        // Regarding the data type, we set a neg infinity because unless this, 
        // null values can't be compared
        let negInf: any;
        switch (col["type"]) {
            case "number":
                negInf = -1 * Infinity;
                break;
            case "date":
                negInf = new Date();
                break;
            default:
                negInf = "";
        }
        /* Sort function */
        this.items.sort((n1, n2) => {
            let cell1 = this.getCellValue(n1, col);
            let cell2 = this.getCellValue(n2, col);
            if (col["type"] == "date") {
                // Real value for date
                cell1 = this.getFieldRawValue(n1, col["field"])
                cell2 = this.getFieldRawValue(n2, col["field"])
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
            } else {
                if (col["type"] == null || col["type"] == "sting") {
                    // Sort insensitive
                    // cell1 = cell1.toLowerCase();
                }
            }
            if (cell2 == null) {
                cell2 = negInf;
            } else {
                if (col["type"] == null || col["type"] == "string") {
                    // Sort insensitive
                    // cell2 = cell2.toLowerCase();
                }
            }

            // Comparison
            if (cell1 > cell2) {
                return asc ? 1 : -1;
            } else {
                return asc ? -1 : 1;
            }
        });
    }

    /**
     * Filter items by a search string
     */
    private filter(): void {
        let searchStr: string = this.searchStr.toLowerCase().trim();
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
     * Get a cell content, resolving a rendrer if necessary
     */
    public getCellValue(item: Object, col: any): any {
        let result: any;
        if (col.field == undefined || col.type == 'link') {
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
        function index(robj: any, i: string) { return robj[i] };
        return path.split('.').reduce(index, obj);
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
            && typeof val == "boolean";
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
        let type: string = this.getColType(col);
        return type != null ? "col-" + type : "";
    }

    /** 
     * Get a cell type and format it to be used a dom element class
     */
    private getCellTypeStr(col: any): string {
        let type: string = this.getColType(col);
        return type != null ? "cell-" + type : "";
    }

    public getItems(): Object[] {
        if (this.backPagination) {
            return this.items;
        } else {
            return this.items.slice((this.currentPage - 1) * this.maxResults, this.currentPage * this.maxResults);
        }
    }

    /**
     * Get the columns that can be used for searching
     */
    public getSearchableColumns(): any[] {
        let cols: any[] = [];
        for (let col of this.columnDefs) {
            if (col.type != "boolean" && col.type != "button") {
                cols.push(col);
            }
        }
        return cols;
    }

    public getMaxPage(): number {
        if (this.nbAllItems) {
            return Math.ceil(this.nbAllItems / this.maxResults);
        } else if (this.items == undefined || this.items == null) {
            return 0;
        } else {
            return Math.ceil(this.items.length / this.maxResults);
        }
    }


    public getPagerList(): number[] {
        let nbLinks = 7; // Must be odd
        let half = (Math.floor(nbLinks / 2));
        let list: number[] = [];
        if (this.currentPage <= half + 2) {
            if (this.getMaxPage() <= nbLinks) {
                for (let i = 1; i <= nbLinks + 1 && i <= this.getMaxPage(); i++) {
                    list.push(i);
                }
            } else {
                for (let i = 1; i <= nbLinks; i++) {
                    list.push(i);
                }
                list.push(null);
                list.push(this.getMaxPage());
            }
        } else {
            list.push(1);
            list.push(null);
            if (this.getMaxPage() <= this.currentPage + half) {
                for (let i = this.getMaxPage() - nbLinks + 1; i <= this.getMaxPage(); i++) {
                    list.push(i);
                }
            } else {
                for (let i = this.currentPage - half + 1; i <= this.currentPage + half - 1; i++) {
                    list.push(i);
                }
                list.push(null);
                list.push(this.getMaxPage());
            }
        }
        return list;
    }


    public goToPage(p: number): void {
        this.currentPage = p;
        if (this.backPagination) {
            // Paging from the database.
            let pageable: Pageable = {
                page: this.currentPage - 1,
                size: this.maxResults,
                sortProperty: this.lastSortedCol["field"],
                asc: this.lastSortedAsc
            };
            this.queryListEvent.emit(pageable);
        }
    }


    public updateMaxResults(): void {
        this.maxResults = this.maxResultsField;
        this.goToPage(1);
    }

    public getNbSelected(): number {
        if (!this.itemsLoaded) return 0;
        let nb: number = 0;
        for (let item of this.items) {
            if (item["isSelectedInTable"]) nb++;
        }
        return nb;
    }

    public selectAll() {
        if (!this.itemsLoaded) return;
        for (let item of this.items) {
            item["isSelectedInTable"] = true;
        }
    }

    public unSelectAll() {
        if (!this.itemsLoaded) return;
        for (let item of this.items) {
            item["isSelectedInTable"] = false;
        }
    }

    private returnFalse() {
        return false;
    }

    private getNbTotal() {
        return this.nbAllItems ? this.nbAllItems : this.itemsSave.length;
    }
}