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

import { Component, EventEmitter, Input, OnInit, Output, ApplicationRef } from '@angular/core';

import { Order, Page, Pageable, Sort, Filter, FilterablePageable } from './pageable.model';
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';

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
    @Input() disableCondition: (item: any) => boolean;

    private page: Page<Object>;
    private isLoading: boolean = false;
    private maxResultsField: number;
    private maxResults: number = 20;
    private lastSortedCol: Object = null;
    private lastSortedAsc: boolean = true;
    private currentPage: number = 1;
    private filter: Filter;
    private loaderImageUrl: string = "assets/images/loader.gif";
    

    constructor(
            private applicationRef: ApplicationRef,
            private breadcrumbsService: BreadcrumbsService) {
        this.maxResultsField = this.maxResults;
    }


    ngOnInit() {
        let savedState = this.breadcrumbsService.currentStep.data.tableState;
        if (savedState) {
            this.lastSortedCol = this.columnDefs.find(col => col && savedState.lastSortedCol && col.field == savedState.lastSortedCol.field);
            this.lastSortedAsc = savedState.lastSortedAsc;
            this.filter = savedState.filter;
            this.maxResults = savedState.maxResults;
            this.goToPage(savedState.currentPage ? savedState.currentPage : 1);
        } else {
            this.getDefaultSorting();
            this.goToPage(1);
        }
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
        if (col.hasOwnProperty("cellRenderer")) {
            let params = new Object();
            params["data"] = item;
            return col["cellRenderer"](params);
        } else if (col.field == undefined) {
            return null;
        } else {
            return this.getFieldRawValue(item, col["field"]);
        }
    }

    public static getFieldRawValue(obj: Object, path: string): any {
        if (path == undefined || path == null) return;
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
        if (path == undefined || path == null) return;
        const split = path.split('.');
        let currentObj = obj;
        for(let i=0; i<split.length-1; i++) {
            currentObj = currentObj[split[i]];
        }
        currentObj[split[split.length-1]] = value;
    }

    /** 
     * Triggered when a field is edited
     */
    private onFieldEdit(obj: Object, col: Object, value: any) {
        this.setFieldRawValue(obj, col['field'], value); 
        this.rowEdit.emit(obj);
        if (col['onEdit']) col['onEdit'](obj, value);
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
        return col.type == 'boolean' || this.isValueBoolean(val);
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
        this.breadcrumbsService.currentStep.data.tableState = {
            lastSortedCol: this.lastSortedCol,
            lastSortedAsc: this.lastSortedAsc,
            filter: this.filter,
            currentPage: this.currentPage,
            maxResults: this.maxResults
        };
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

    private getDefaultSorting() {
        for (let col of this.columnDefs) {
            if (col.defaultSortCol) {
                this.lastSortedCol = col;
                this.lastSortedAsc = col.defaultAsc != undefined ? col.defaultAsc : true;
                return;
            }
        }
    }

    private cellEditable(item, col) {
        let colEditable: boolean = col.editable && (typeof col.editable === 'function' ? col.editable(item) : col.editable);
        return colEditable && !this.rowDisabled(item);
    }

    private rowDisabled(item): boolean {
        return this.disableCondition && this.disableCondition(item);
    }
}