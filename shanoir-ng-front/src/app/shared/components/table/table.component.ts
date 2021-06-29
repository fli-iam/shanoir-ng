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
import { ApplicationRef, Component, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';

import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Filter, FilterablePageable, Order, Page, Pageable, Sort } from './pageable.model';


@Component({
    selector: 'shanoir-table',
    templateUrl: 'table.component.html',
    styleUrls: ['table.component.css'],
})
export class TableComponent implements OnInit, OnChanges {
    @Input() getPage: (pageable: Pageable, forceRefresh: boolean) => Promise<Page<any>>;
    @Input() columnDefs: any[];
    @Input() customActionDefs: any[];
    @Input() selectionAllowed: boolean = false;
    @Input() enableSettings: boolean = false;
    @Input() selection: Set<number> = new Set();
    @Output() selectionChange: EventEmitter<Set<number>> = new EventEmitter<Set<number>>();
    selectAll: boolean | 'indeterminate' = false;
    @Input() browserSearch: boolean = true;
    @Input() editMode: boolean = false;
    @Output() rowClick: EventEmitter<Object> = new EventEmitter<Object>();
    @Output() rowEdit: EventEmitter<Object> = new EventEmitter<Object>();
    @Input() disableCondition: (item: any) => boolean;
    @Input() maxResults: number = 20;
    page: Page<Object>;
    isLoading: boolean = false;
    maxResultsField: number;
    lastSortedCol: Object = null;
    lastSortedAsc: boolean = true;
    currentPage: number = 1;
    loaderImageUrl: string = "assets/images/loader.gif";
    isError: boolean = false;
    filter: Filter = new Filter(null, null);
    firstLoading: boolean = true;
    @ViewChild('settingsDialog') settingsDialog: ModalComponent;
    @Input() identifier: string = 'default'; // usefull for saving table config when there is more than one table in the page
    

    constructor(
            private applicationRef: ApplicationRef,
            private breadcrumbsService: BreadcrumbsService) {
        this.maxResultsField = this.maxResults;
    }


    ngOnChanges(changes: SimpleChanges): void {
        if (changes['selection'] && !changes['selection'].isFirstChange()) {
            this.saveSelection();
        }
    }


    ngOnInit() {
        let currentStep = this.breadcrumbsService.currentStep
        let savedState = currentStep && currentStep.data.tableState ? currentStep.data.tableState[this.identifier] : null;
        if (savedState) {
            this.lastSortedCol = this.columnDefs.find(col => col && savedState.lastSortedCol && col.field == savedState.lastSortedCol.field);
            this.lastSortedAsc = savedState.lastSortedAsc;
            this.filter = savedState.filter;
            this.maxResults = savedState.maxResults;
            this.columnDefs.forEach(col => {
                if (savedState.columns && savedState.columns[col.field]) {
                    col.width = savedState.columns[col.field].width;
                    col.hidden = savedState.columns[col.field].hidden;
                }
            })
            if (savedState.selection && Symbol.iterator in Object(savedState.selection)) {
                this.selection = new Set();
                savedState.selection.forEach(id => this.selection.add(id));
                this.emitSelectionChange();
            }
            this.goToPage(savedState.currentPage ? savedState.currentPage : 1)
                .then(() => this.firstLoading = false);
        } else {
            this.getDefaultSorting();
            this.goToPage(1)
                .then(() => this.firstLoading = false);
        }
    }

    
    
    get items(): Object[] {
        return this.page ? this.page.content : [];
    }
    

    sortBy(col: Object): void {
        if (col['suppressSorting'] || col["type"] == "button") return;
        let defaultAsc: boolean = col["defaultAsc"] != undefined ? col["defaultAsc"] : true;
        let asc: boolean = col == this.lastSortedCol ? !this.lastSortedAsc : defaultAsc;
        this.lastSortedCol = col;
        this.lastSortedAsc = asc;
        this.goToPage(1);
    }


    onSearchChange(filter: Filter) {
        this.filter = filter;
        this.clearSelection();
        this.goToPage(1);
    }


    onRowClick(item: Object) {
        if (this.rowClick.observers.length > 0 && !this.rowDisabled(item)) this.rowClick.emit(item);
        else if (this.selectionAllowed) this.onSelectChange(item, !this.isSelected(item));
    }


    public static getCellValue(item: Object, col: any): any {
        if (col.hasOwnProperty("cellRenderer")) {
            let params = new Object();
            params["data"] = item;
            return col["cellRenderer"](params);
        } else if (!col.field) {
            return null;
        } else {
            let fieldValue = this.getFieldRawValue(item, col["field"]);
            if (fieldValue) return fieldValue;
            else if (col.defaultField) 
                return this.getFieldRawValue(item, col["defaultField"]);
            else
                return;
        }
    }

    public static getFieldRawValue(obj: Object, path: string): any {
        if (!path) return;
        function index(robj: any, i: string) { return robj ? robj[i] : undefined };
        return path.split('.').reduce(index, obj);
    }

    /**
     * Get a cell content, resolving a renderer if necessary
     */
    getCellValue(item: Object, col: any): any {
        return TableComponent.getCellValue(item, col);
    }

    /**
     * Just get the field value, but not using any renderer!
     */
    getFieldRawValue(obj: Object, path: string): any {
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
    onFieldEdit(obj: Object, col: Object, value: any) {
        this.setFieldRawValue(obj, col['field'], value); 
        this.rowEdit.emit(obj);
        if (col['onEdit']) col['onEdit'](obj, value);
    }

    /**
     * Convert a cell content to a displayable string
     */
    renderCell(item: Object, col: any): string {
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
    isFieldBoolean(col: any): boolean {
        if (!this.items || this.items.length == 0) throw new Error('Cannot determine type of a column if there is no data');
        let val = TableComponent.getCellValue(this.items[0], col);
        return col.type == 'boolean' || this.isValueBoolean(val);
    }

    isColumnText(col: any): boolean {
        return !this.isFieldBoolean(col)
            && col.type != 'link'
            && col.type != 'button'
            && col.type != 'date'
            && col.type != 'number';
    }

    isColumnNumber(col: any): boolean {
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
    getColType(col: any): string {
        if (col.type != undefined) {
            return col.type;
        } else {
            return null;
        }
    }

    /**
     * Get a column type and format it to be used a dom element class
     */
    getColTypeStr(col: any): string {
        let type: string = this.getColType(col);
        return type != null ? "col-" + type : "";
    }

    /** 
     * Get a cell type and format it to be used a dom element class
     */
    getCellTypeStr(col: any): string {
        let type: string = this.getColType(col);
        return type != null ? "cell-" + type : "";
    }

    goToPage(p: number, forceRefresh: boolean = false): Promise<void> {
        this.currentPage = p;
        this.isLoading = true;
        return this.getPage(this.getPageable(), forceRefresh).then(page => {
            this.page = page;
            this.maxResultsField = page ? page.size : 0;
            this.computeSelectAll();
            setTimeout(() => {
                this.isError = false;
                this.isLoading = false;
            }, 200);
        }).catch(reason => {
            setTimeout(() => {
                this.isError = true;
                this.isLoading = false;
                throw reason;
            }, 200);
        });
    }

    /**
     * Call to refresh from outsilde
     */
    public refresh(): Promise<void> {
        return this.goToPage(this.currentPage, true);
    }

    private getPageable(): Pageable {
        this.saveState();
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

    private saveState() {
        let currentStep = this.breadcrumbsService.currentStep
        if(currentStep) {
            if (!this.breadcrumbsService.currentStep.data.tableState) this.breadcrumbsService.currentStep.data.tableState = [];
            this.breadcrumbsService.currentStep.data.tableState[this.identifier] = {
                lastSortedCol: this.lastSortedCol,
                lastSortedAsc: this.lastSortedAsc,
                filter: this.filter,
                currentPage: this.currentPage,
                maxResults: this.maxResults,
                columns: [],
                selection: []
            };
            this.saveSettings();
            this.saveSelection();
        }
    }

    saveSettings() {
        if (!this.breadcrumbsService.currentStep.data.tableState) this.breadcrumbsService.currentStep.data.tableState = [];
        this.breadcrumbsService.currentStep.data.tableState[this.identifier].columns = this.columnDefs.reduce((result, col) => {
            result[col.field] = { width: col.width, hidden: col.hidden };
            return result;
        }, {});
    }

     saveSelection() {
        if (!this.breadcrumbsService.currentStep.data.tableState) this.breadcrumbsService.currentStep.data.tableState = [];
        this.breadcrumbsService.currentStep.data.tableState[this.identifier].selection = [...this.selection];
    }

    updateMaxResults(): void {
        this.maxResults = this.maxResultsField;
        this.goToPage(1);
    }

    public getNbSelected(): number {
        return this.selection ? this.selection.size : 0;
    }

    onSelectAllChange() {
        if (this.selectAll == true) {

            // let pageableAll: Pageable;
            // if (this.filter) {
            //     pageableAll = new FilterablePageable(
            //         1, 
            //         this.page.totalElements,
            //         null,
            //         this.filter
            //     );
            // } else {
            //     pageableAll = new Pageable(
            //         1, 
            //         this.page.totalElements
            //     );
            // }
            // this.getPage(pageableAll).then(page => {
            //     this.selection = new Map();
            //     page.content.forEach(elt => this.selection.set(elt.id, elt));
            // });
            this.page.content.forEach(elt => this.selection.add(elt['id']));
            this.emitSelectionChange();
        } else if (this.selectAll == false) {
            this.page.content.forEach(elt => {
                this.selection.delete(elt['id']);
            });
            this.emitSelectionChange();
        }
    }

    unSelectAll() {
    }

    clearSelection() {
        this.selection = new Set();
        this.emitSelectionChange();
        this.selectAll = false;
    }

    computeSelectAll() {
        if (this.page && this.page.content) {
            let selectedOnCurrentPage: any[] = this.page.content.filter(row => this.selection.has(row['id']));
            if (selectedOnCurrentPage.length == this.page.content.length) {
                this.selectAll = true;
            } else if (selectedOnCurrentPage.length == 0) {
                this.selectAll = false;
            } else {
                this.selectAll = 'indeterminate';
            }
        }
    }

    emitSelectionChange() {
        this.saveSelection();
        this.selectionChange.emit(this.selection);
    }

    onSelectChange(item: Object, selected: boolean) {
        if (selected) {
            if (item['id']) this.selection.add(item['id']);
        } else {
            this.selection.delete(item['id']);
        }
        this.computeSelectAll();
        this.emitSelectionChange();
    }

    isSelected(item: Object): boolean {
        if (!item['id']) {
            console.log(item)
            this.selectionAllowed = false;
            throw new Error('TableComponent : if you are going to use the selectionAllowed input your items must have an id. (it\'s like in a night club)');
        }
        return this.selection.has(item['id']);
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

    cellEditable(item, col) {
        let colEditable: boolean = typeof col.editable === 'function' ? col.editable(item) : col.editable;
        return colEditable && !this.rowDisabled(item);
    }

    rowDisabled(item): boolean {
        return this.disableCondition && this.disableCondition(item);
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            console.log('table items', this.items);
        }
    }
}