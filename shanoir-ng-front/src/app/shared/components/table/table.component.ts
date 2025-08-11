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
import { ChangeDetectionStrategy, Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { fromEvent, Subscription } from 'rxjs';

import { Router } from "@angular/router";
import * as shajs from 'sha.js';
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import * as AppUtils from '../../../utils/app.utils';
import { isDarkColor } from "../../../utils/app.utils";
import { slideDown } from '../../animations/animations';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { GlobalService } from '../../services/global.service';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { ColumnDefinition } from './column.definition.type';
import { Filter, FilterablePageable, Order, Page, Pageable, Sort } from './pageable.model';
import {TaskService} from "../../../async-tasks/task.service";
import {Task} from "../../../async-tasks/task.model";
import {dateFormat} from "../../localLanguage/localDate.abstract";

@Component({
    selector: 'shanoir-table',
    templateUrl: 'table.component.html',
    styleUrls: ['table.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: [slideDown],
    standalone: false
})
export class TableComponent implements OnInit, OnChanges, OnDestroy {
    @Input() getPage: (pageable: Pageable, forceRefresh: boolean) => Promise<Page<any>> | Page<any>;
    @Input() rowRoute: (item: any) => string;
    @Input() columnDefs: ColumnDefinition[];
    @Input() subRowsDefs: ColumnDefinition[];
    @Input() customActionDefs: any[];
    @Input() selectionAllowed: boolean = false;
    @Input() selection: Set<number> = new Set();
    @Input() selectedId: number;
    @Output() selectionChange: EventEmitter<Set<number>> = new EventEmitter<Set<number>>();
    selectAll: boolean | 'indeterminate' = false;
    @Input() browserSearch: boolean = true;
    @Input() collapseControls: boolean = false;
    @Input() editMode: boolean = false;
    @Output() rowClick: EventEmitter<Object> = new EventEmitter<Object>();
    @Output() rowEdit: EventEmitter<Object> = new EventEmitter<Object>();
    @Output() pageLoaded: EventEmitter<Page<any>> = new EventEmitter();
    @Input() disableCondition: (item: any) => boolean;
    @Input() maxResults: number = 20;
    @Input() subRowsKey: string;
    @Output() registerRefresh: EventEmitter<(number?) => void> = new EventEmitter();
    @Output() downloadStatsEvent: EventEmitter<any> = new EventEmitter();
    page: Page<Object>;
    isLoading: boolean = false;
    maxResultsField: number;
    pageNumber: number;
    lastSortedCol: Object = null;
    lastSortedAsc: boolean = true;
    currentPage: number = 1;
    loaderImageUrl: string = "assets/images/loader.gif";
    isError: boolean = false;
    filter: Filter = new Filter(null, null);
    firstLoading: boolean = true;
    currentDrag: {columns: any; leftOrigin: number, totalWidth: number, leftColIndex: number};
    private subscriptions: Subscription[] = [];
    private hash: string;
    private colSave: { width: string, hidden: boolean }[];
    compactMode: boolean = false;
    nbColumns: number;
    expended: boolean[] = [];
    subRowOpen: any = {};
    path: string;
    settingsOpened: boolean = false;

    constructor(
            private elementRef: ElementRef,
            private breadcrumbsService: BreadcrumbsService,
            private globalClickService: GlobalService,
            protected router: Router,
            private dialogService: ConfirmDialogService,
            private taskService: TaskService) {
        this.maxResultsField = this.maxResults;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['selection'] && !changes['selection'].isFirstChange()) {
            this.saveSelection();
        }
        if (changes.columnDefs && this.columnDefs) {
            setTimeout(() => {
                this.colSave = this.columnDefs.map(col => { return { width: col.width, hidden: col.hidden } });
                if (this.subRowsDefs) {
                    this.colSave = this.colSave.concat(this.subRowsDefs.map(col => { return { width: col.width, hidden: col.hidden } }));
                }
                this.hash = this.getHash();
                this.reloadSettings();
                this.reloadPreviousState();
                this.nbColumns = this.columnDefs.length;
                if (this.selectionAllowed) this.nbColumns++;
                if (this.subRowsDefs) this.nbColumns++;
            });
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(sub => sub.unsubscribe());
    }

    ngOnInit() {
        this.subscriptions.push(this.globalClickService.onGlobalMouseUp.subscribe(() => this.stopDrag()));
        this.subscriptions.push(fromEvent(window, 'resize').subscribe( evt => {
            this.checkCompactMode();
        }));
        this.checkCompactMode();
        this.registerRefresh.emit(this.refresh.bind(this));
    }

    private computeItemVars() {
        this.page?.content?.forEach((item, itemIndex) => {
            this.columnDefs?.forEach((col, colIndex) => {
                if (col.possibleValues) {
                    if (!this.page._savedContentRendering) this.page._savedContentRendering = [];
                    if (!this.page._savedContentRendering[itemIndex]) this.page._savedContentRendering[itemIndex] = [];
                    if (!this.page._savedContentRendering[itemIndex][colIndex]) this.page._savedContentRendering[itemIndex][colIndex] = {};
                    this.page._savedContentRendering[itemIndex][colIndex].possibleValues = this.isFunction(col.possibleValues) ? (col.possibleValues as ((item: any) => any[]))(item) : col.possibleValues;
                }
            });
        });
    }

    private checkCompactMode() {
        let width: number = this.elementRef.nativeElement.offsetWidth;
        this.compactMode = width < 620;
    }

    private reloadPreviousState() {
        let currentStep = this.breadcrumbsService.currentStep
        let savedState = currentStep && currentStep.data.tableState ? currentStep.data.tableState[this.hash] : null;
        if (savedState) {
            this.lastSortedCol = this.columnDefs.find(col => col && savedState.lastSortedCol && col.field == savedState.lastSortedCol.field);
            this.lastSortedAsc = savedState.lastSortedAsc;
            this.filter = savedState.filter;
            this.maxResults = savedState.maxResults;
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
        if (col['disableSorting'] || col["type"] == "button") return;
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

    downloadStats(item) {
        this.downloadStatsEvent.emit(item);
    }

    public static getCellValue(item: Object, col: ColumnDefinition): any {
        if (col.hasOwnProperty("cellRenderer")) {
            let params = new Object();
            params["data"] = item;
            return col["cellRenderer"](params);
        } else if (!col.field) {
            return null;
        } else {
            let fieldValue = this.getFieldRawValue(item, col["field"]);
            if (fieldValue) {
                return fieldValue;
            }
            else if (col.defaultField)
                return this.getFieldRawValue(item, col["defaultField"]);
            else
                return;
        }
    }

    public static harmonizeToDate(value: any): Date {
        let date: Date;
        if (value instanceof Date) {
            date = value;
        } else if (!Number.isNaN(Date.parse(value))) {
            date = new Date(Date.parse(value));
        } else if (Number.isInteger(value)) {
            date = new Date(value);
        } else {
            date = this.stringToDate(value);
        }
        return date;
    }

    public static getFieldRawValue(obj: Object, path: string): any {
        if (!path) return;
        function index(robj: any, i: string) { return robj ? robj[i] : undefined };
        return path.split('.').reduce(index, obj);
    }

    /**
     * Get a cell content, resolving a renderer if necessary
     */
    getCellValue(item: Object, col: ColumnDefinition): any {
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
    renderCell(item: Object, col: ColumnDefinition): any {
        let result: any = this.getCellValue(item, col);
        if (result == null || this.isValueBoolean(result)) {
            return "";
        } else if ((col.type == 'date' || col.type == 'dateTime') && !col.cellRenderer) {
            let date: Date = TableComponent.harmonizeToDate(result);
            let dateFormat;
            if (col.type == 'dateTime') dateFormat = {year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", hour12: false };
            else dateFormat = {year: "numeric", month: "2-digit", day: "2-digit"};
            return date?.toLocaleDateString(undefined, dateFormat) || result;
        } else if (result.text) {
            return result;
        } else {
            return result;
        }
    }

    private static stringToDate(dateString: string): Date {
        if (!dateString) return null;
        dateString += '';
        let split: string[] = dateString.split('-');
        if (split.length != 3) return null;
        let splitNum: number[] = split.map(elt => parseInt(elt));
        if (splitNum.includes(NaN)) return null;
        return new Date(splitNum[2],splitNum[1],splitNum[0]);
    }

    getCellGraphics(item: any, col: ColumnDefinition): any {
        if (col.hasOwnProperty("cellGraphics")) {
            return col["cellGraphics"](item);
        } else return null;
    }

    /**
     * Test if a cell content is a boolean
     */
    isFieldBoolean(col: ColumnDefinition): boolean {
        if (!this.items || this.items.length == 0) throw new Error('Cannot determine type of a column if there is no data');
        let val = this.getCellValue(this.items[0], col);
        return col.type == 'boolean' || this.isValueBoolean(val);
    }

    isColumnText(col: ColumnDefinition): boolean {
        return !this.isFieldBoolean(col)
            && col.type != 'link'
            && col.type != 'button'
            && col.type != 'date'
            && col.type != 'number';
    }

    isColumnNumber(col: ColumnDefinition): boolean {
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
    getColType(col: ColumnDefinition): string {
        if (col.type != undefined) {
            return col.type;
        } else {
            return null;
        }
    }

    /**
     * Get a column type and format it to be used a dom element class
     */
    getColTypeStr(col: ColumnDefinition): string {
        let type: string = this.getColType(col);
        return type != null ? "col-" + type : "";
    }

    /**
     * Get a cell type and format it to be used a dom element class
     */
    getCellTypeStr(col: ColumnDefinition): string {
        let type: string = this.getColType(col);
        return type != null ? "cell-" + type : "";
    }

    jumpToPage(p: number) {
        if (p <= this.page.totalPages) {
            this.goToPage(p);
        } else if (p < 0) {
            this.goToPage(1);
        } else {
            this.goToPage(this.page.totalPages);
        }
    }

    goToPage(p: number, forceRefresh: boolean = false): Promise<Page<any>> {
        this.currentPage = p;
        this.isLoading = true;
        let getPage: Page<any> | Promise<Page<any>> = this.getPage(this.getPageable(), forceRefresh)
        if (getPage instanceof Promise) {
            return getPage.then(page => {
                this.pageLoaded.emit(page);
                return this.computePage(page);
            }).catch(reason => {
                setTimeout(() => {
                    this.isError = true;
                    this.isLoading = false;
                }, 200);
                throw reason;
            });
        } else if (getPage instanceof Page) {
            return Promise.resolve(this.computePage(getPage)).then(page => {
                this.pageLoaded.emit(page);
                return page;
            });
        }

    }

    private computePage(page: Page<any>): Page<any> {
        this.page = page;
        this.computeItemVars();
        this.maxResultsField = page ? page.size : 0;
        this.computeSelectAll();
        setTimeout(() => {
            this.isError = false;
            this.isLoading = false;
        }, 200);
        return page;
    }

    /**
     * Call to refresh from outside
     */
    public refresh(page?: number): Promise<Page<any>> {
        if (page == undefined) {
            return this.goToPage(this.currentPage, true);
        } else {
            return this.goToPage(page, true);
        }
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
            this.breadcrumbsService.currentStep.data.tableState[this.hash] = {
                lastSortedCol: this.lastSortedCol,
                lastSortedAsc: this.lastSortedAsc,
                filter: this.filter,
                currentPage: this.currentPage,
                maxResults: this.maxResults,
                selection: []
            };
            this.saveSettings();
            this.saveSelection();
        }
    }

    saveSettings() {
        let pref: TablePreferences = new TablePreferences();
        pref.colWidths = this.columnDefs.map(col => { return {width: col.width, hidden: col.hidden}; });
        if (this.subRowsDefs) {
            pref.colWidths = pref.colWidths.concat(this.subRowsDefs.map(col => { return {width: col.width, hidden: col.hidden}; }));
        }
        pref.pageSize = this.maxResultsField;
        localStorage.setItem(this.hash, JSON.stringify(pref));
    }

    reloadSettings() {
        let prefStr: string = localStorage.getItem(this.hash);
        if (prefStr) {
            let pref: TablePreferences = JSON.parse(prefStr);
            this.maxResults = pref.pageSize;
            this.columnDefs.forEach((col, i) => {
                col.width = pref.colWidths[i]?.width;
                col.hidden = pref.colWidths[i]?.hidden;
            });
            this.subRowsDefs?.forEach((col, i) => {
                col.width = pref.colWidths[this.columnDefs.length + i]?.width;
                col.hidden = pref.colWidths[this.columnDefs.length + i]?.hidden;
            });
        }
    }

    saveSelection() {
        if (!this.breadcrumbsService.currentStep.data.tableState) this.breadcrumbsService.currentStep.data.tableState = [];
        if (!this.breadcrumbsService.currentStep.data.tableState[this.hash]) this.breadcrumbsService.currentStep.data.tableState[this.hash] = {};
        this.breadcrumbsService.currentStep.data.tableState[this.hash].selection = [...this.selection];
    }

    updateMaxResults(): void {
        this.maxResults = this.maxResultsField;
        this.saveSettings();
        this.goToPage(1);
    }

    public getNbSelected(): number {
        return this.selection ? this.selection.size : 0;
    }

    onSelectAllChange() {
        if (this.selectAll == true) {
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

    startDrag(leftColIndex: number, thRef: HTMLElement, event: MouseEvent, columnDefs: ColumnDefinition) {
        this.currentDrag = {
            columns: columnDefs,
            leftOrigin: event.pageX - thRef.offsetWidth + 10,
            totalWidth: (thRef.nextElementSibling as HTMLElement).offsetWidth + thRef.offsetWidth - 22,
            leftColIndex: leftColIndex
        };
    }

    moveDrag(event: MouseEvent) {
        if (this.currentDrag) {
            let leftDragWidth: number = event.pageX - this.currentDrag.leftOrigin;
            let nextIndex: number = this.currentDrag.columns.slice(this.currentDrag.leftColIndex + 1).findIndex(col => !col.hidden);
            if (leftDragWidth >= 10) {
                this.currentDrag.columns[this.currentDrag.leftColIndex].width = (leftDragWidth + 0) + 'px';
                if (this.currentDrag.totalWidth - leftDragWidth < 10 && nextIndex != -1) {
                    this.currentDrag.columns[nextIndex + this.currentDrag.leftColIndex + 1].width = 10 + 'px';
                } else {
                    this.currentDrag.columns[nextIndex + this.currentDrag.leftColIndex + 1].width = (this.currentDrag.totalWidth - leftDragWidth) + 'px';
                }
            } else {
                this.currentDrag.columns[this.currentDrag.leftColIndex].width = 10 + 'px';
                this.currentDrag.columns[nextIndex + this.currentDrag.leftColIndex + 1].width = (this.currentDrag.totalWidth - 10) + 'px';
            }
        }
    }

    stopDrag() {
        if (this.currentDrag) {
            this.currentDrag = null;
            this.saveSettings();
        }
    }

    private getHash(): string {
        let username: string = KeycloakService.auth.authz.tokenParsed.name;
        let stringToBeHashed: string = username + '_' + this.columnDefs.map(col => col.headerName + '-' + col.headerName).join('_');
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = hash.substring(0, 30);
        return hex;
    }

    resetColumns() {
        this.columnDefs.forEach((col, i) => {
            col.width = this.colSave[i].width;
            col.hidden = this.colSave[i].hidden;
        });
        this.subRowsDefs?.forEach((col, i) => {
            col.width = this.colSave[this.columnDefs.length + i].width;
            col.hidden = this.colSave[this.columnDefs.length + i].hidden;
        });
        this.saveSettings();
    }

    exportTable() {
        const MAX_ROWS: number = 10000;
        if (this.page.totalElements > MAX_ROWS) {
            this.dialogService.error('Too Many Rows', 'You are trying to export ' + this.page.totalElements
                + ' rows, the current max is at ' + MAX_ROWS + ', sorry.');
        } else {
            let csvStr: string = '';
            csvStr += this.columnDefs.map(col => col.headerName).join(','); // headers
            let completion: Promise<void> = Promise.resolve();
            for (let i = 0; i < this.page.totalPages; i++) { // here we could use a fixed page size
                let pageable: Pageable = this.getPageable();
                pageable.pageNumber = i + 1;
                let getPage: Page<any> | Promise<Page<any>> = this.getPage(pageable, true)
                completion = completion.then(() => { // load pages sequentially
                    if (getPage instanceof Promise) {
                        return getPage.then(page => {
                            for (let entry of page.content) {
                                csvStr = this.setValuesForCsvExport(csvStr, entry)
                            }
                        });
                    } else if (getPage instanceof Page) {
                        for (let entry of getPage.content) {
                            csvStr = this.setValuesForCsvExport(csvStr, entry)
                        }
                        return Promise.resolve();
                    }
                });
            }
            completion.then(() => {
                const csvBlob = new Blob([csvStr], {
                    type: 'text/csv'
                });
                AppUtils.browserDownloadFile(csvBlob, 'tableExport_' + new Date().toLocaleString('fr-FR'));
            });
        }
    }


    private setValuesForCsvExport(csvStr: string, entry: any) {
        csvStr += '\n' + this.columnDefs.map(col => {
            let value = TableComponent.getCellValue(entry, col) || '';
            if (this.isValidDate(value)) {
                const dateObj = new Date(value);
                value = this.formatDate(dateObj, dateFormat);
            }

            return `"${value}"`;
        }).join(',');

        return csvStr;
    }

    private isValidDate(value: any): boolean {
        if (!value) return false;
        const date = new Date(value);
        return !isNaN(date.getTime());
    }

    private formatDate(date: Date, formatStr: string): string {
        const dd = String(date.getDate()).padStart(2, '0');
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const yyyy = String(date.getFullYear());

        return formatStr
            .replace('dd', dd)
            .replace('MM', mm)
            .replace('yyyy', yyyy)
            .replace('jj', dd)
            .replace('tt', dd)
            .replace('aaaa', yyyy)
            .replace('jjjj', yyyy);
    }

    deploy(i: number) {
        this.subRowOpen[i] = true;
    }

    fold(i: number) {
        this.subRowOpen[i] = false;
    }

    isFunction(a: any): boolean {
        return typeof a === 'function';
    }

    getFontColor(colorInp: string): boolean {
      return isDarkColor(colorInp);
    }
}

export class TablePreferences {

    colWidths: {width: string, hidden: boolean}[];
    pageSize: number;
}
