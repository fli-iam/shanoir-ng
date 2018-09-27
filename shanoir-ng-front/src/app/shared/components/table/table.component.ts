import { Component, EventEmitter, Input, OnInit, Output, ApplicationRef } from '@angular/core';

import { Order, Page, Pageable, Sort, Filter, FilterablePageable } from './pageable.model';

@Component({
    selector: 'shanoir-table',
    templateUrl: 'table.component.html',
    styleUrls: ['table.component.css'],
})

export class TableComponent implements OnInit {
    @Input() getPage: (pageable: Pageable) => Promise<Page<any>>;
    @Input() columnDefs: any[];
    @Input() customActionDefs: any[];
    @Input() selectionAllowed: boolean = false; // TODO : selectable
    @Input() browserSearch: boolean = true;
    @Input() editMode: boolean = false;
    @Output() rowClick: EventEmitter<Object> = new EventEmitter<Object>();
    @Output() rowEdit: EventEmitter<Object> = new EventEmitter<Object>();
    
    private page: Page<Object>;
    private isLoading: boolean = false;
    private maxResultsField: number;
    private maxResults: number = 20;
    private lastSortedCol: Object = null;
    private lastSortedAsc: boolean = true;
    private currentPage: number = 1;
    private filter: Filter;
    private loaderImageUrl: string = "assets/images/loader.gif";
    

    constructor(private applicationRef: ApplicationRef) {
        this.maxResultsField = this.maxResults;
    }


    ngOnInit() {
        this.goToPage(1);
    }

    
    private get items(): Object[] {
        return this.page ? this.page.content : [];
    }
    

    private sortBy(col: Object): void {
        if (col['suppressSorting'] || col["type"] == "button") return;
        let defaultAsc: boolean = col["defaultAsc"] != undefined ? col["defaultAsc"] : true;
        let asc: boolean = col == this.lastSortedCol ? !this.lastSortedAsc : defaultAsc;
        this.lastSortedCol = col;
        this.lastSortedAsc = asc;
        this.goToPage(1);
    }


    private onSearchChange(filter: Filter) {
        this.filter = filter;
        this.goToPage(1);
    }


    private onRowClick(item: Object) {
        this.rowClick.emit(item);
    }


    public static getCellValue(item: Object, col: any): any {
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

    public static getFieldRawValue(obj: Object, path: string): any {
        function index(robj: any, i: string) { return robj ? robj[i] : undefined };
        return path.split('.').reduce(index, obj);
    }

    /**
     * Get a cell content, resolving a renderer if necessary
     */
    private getCellValue(item: Object, col: any): any {
        return TableComponent.getCellValue(item, col);
    }

    /**
     * Just get the field value, but not using any renderer!
     */
    private getFieldRawValue(obj: Object, path: string): any {
        return TableComponent.getFieldRawValue(obj, path);
    }

    /**
     * Set the property value
     */
    private setFieldRawValue(obj: Object, path: string, value: any) {
        const split = path.split('.');
        let currentObj = obj;
        for(let i=0; i<split.length-1; i++) {
            currentObj = currentObj[split[i]];
        }
        currentObj[split[split.length-1]] = value;
    }

    /**
     * Convert a cell content to a displayable string
     */
    private renderCell(item: Object, col: any): string {
        let result: any = TableComponent.getCellValue(item, col);
        if (result == null || this.isValueBoolean(result)) {
            return "";
        } else {
            return "" + result;
        }
    }

    /**
     * Test if a cell content is a boolean
     */
    private isFieldBoolean(col: any): boolean {
        if (!this.items || this.items.length == 0) throw new Error('Cannot determine type of a column if there is no data');
        let val = TableComponent.getCellValue(this.items[0], col);
        return this.isValueBoolean(val);
    }

    private isColumnText(col: any): boolean {
        return !this.isFieldBoolean(col)
            && col.type != 'link'
            && col.type != 'button'
            && col.type != 'date'
            && col.type != 'number';
    }

    private isColumnNumber(col: any): boolean {
        return col.type == 'number';
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

    private goToPage(p: number): void {
        this.currentPage = p;
        this.isLoading = true;
        this.getPage(this.getPageable()).then(page => {
            this.page = page;
            setTimeout(() => this.isLoading = false, 200);
        });
    }

    /**
     * Call to refresh from outsilde
     */
    public refresh() {
        this.goToPage(this.currentPage);
    }

    private getPageable(): Pageable {
        let orders: Order[] = [];
        if (this.lastSortedCol) {
            if (this.lastSortedCol['orderBy']) {
                for (let orderBy of this.lastSortedCol['orderBy']) {
                    orders.push(new Order(this.lastSortedAsc ? 'ASC' : 'DESC', orderBy));
                }
            } else {
                orders.push((new Order(this.lastSortedAsc ? 'ASC' : 'DESC', this.lastSortedCol["field"])));
            }
        }
        if (this.filter) {
            return new FilterablePageable(
                this.currentPage, 
                this.maxResults,
                new Sort(orders),
                this.filter
            );
        } else {
            return new Pageable(
                this.currentPage, 
                this.maxResults,
                new Sort(orders)
            );
        }
    }

    private updateMaxResults(): void {
        this.maxResults = this.maxResultsField;
        this.goToPage(1);
    }

    private getNbSelected(): number {
        if (!this.items) return 0;
        let nb: number = 0;
        for (let item of this.items) {
            if (item["isSelectedInTable"]) nb++;
        }
        return nb;
    }

    private selectAll() {
        if (!this.items) return;
        for (let item of this.items) {
            item["isSelectedInTable"] = true;
        }
    }

    private unSelectAll() {
        if (!this.items) return;
        for (let item of this.items) {
            item["isSelectedInTable"] = false;
        }
    }

}