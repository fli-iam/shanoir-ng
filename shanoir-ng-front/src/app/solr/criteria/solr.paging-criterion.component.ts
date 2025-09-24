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
import { Component, EventEmitter, forwardRef, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import * as shajs from 'sha.js';
import { Router } from '@angular/router';

import { slideDown, slideRight } from '../../shared/animations/animations';
import { FacetResultPage, FacetField, FacetPageable } from '../solr.document.model';
import { Page } from '../../shared/components/table/pageable.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';


@Component({
    selector: 'solr-paging-criterion',
    templateUrl: 'solr.paging-criterion.component.html',
    styleUrls: ['solr.criterion.component.css', 'solr.paging-criterion.component.css'],
    animations: [slideDown, slideRight],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SolrPagingCriterionComponent),
            multi: true,
        }
    ],
    standalone: false
})

export class SolrPagingCriterionComponent implements ControlValueAccessor, OnChanges {

    @Input() getPage: (pageable: FacetPageable, facetName: string) => Promise<FacetResultPage>;
    displayedFacets: FacetField[] = [];
    selectedFacets: FacetField[] = [];
    @Input() label: string = "";
    @Input() awesome: string;
    @Input() facetName: string;
    hasChecked: boolean = false;
    filterText: string;
    loaded: boolean = false;
    loadedPromise: Promise<void> = new Promise((resolve) => this.loadedPromiseResolve = resolve);
    private loadedPromiseResolve: () => void;
    loading: boolean = false;
    @Output() onChange: EventEmitter<string[]> = new EventEmitter();
    private _open: boolean = false;
    currentPage: FacetResultPage;
    maxPage: number = Infinity ;
    sortMode: 'INDEX' | 'COUNT' = 'INDEX';
    @Output() sortModeChange: EventEmitter<'INDEX' | 'COUNT'> = new EventEmitter();
    static readonly PAGE_SIZE: number = 15;
    filterTimeout: number = 0;
    private hash: string;

    protected propagateChange: (any) => void = () => {};
    protected propagateTouched = () => {};

    constructor(private router: Router) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.facetName) {
            this.hash = SolrPagingCriterionComponent.getHash(this.facetName, this.router.url);
            this.reloadSettings();
        }
    }

    goToPage(pageNumber: number): Promise<void> {
        let pageable: FacetPageable = new FacetPageable(pageNumber, SolrPagingCriterionComponent.PAGE_SIZE, this.sortMode, this.filterText);
        this.loading = true;
        return this.getPage(pageable, this.facetName).then(page => {
            this.loadPage(page);
            this.loaded = true;
            this.loadedPromiseResolve();
            this.loading = false;
        }).catch(error => {
            this.loading = false;
            throw error;
        });
    }

    loadPage(page: FacetResultPage) {
        if (!page || !page.content || page.content.length == 0) {
            if (this.currentPage?.number != page?.number - 1) {
                this.displayedFacets = [];
                this.currentPage = page ? page : new Page();
            }
            this.maxPage = this.currentPage ? this.currentPage.number : 1;
        } else {
            if (page.content.length < SolrPagingCriterionComponent.PAGE_SIZE) {
                this.maxPage = page.number;
            }
            this.displayedFacets = [];
            page.content.forEach(facet => {
                facet.checked = this.selectedFacets.findIndex(selectedfacet => selectedfacet.value == facet.value) != -1;
                this.displayedFacets.push(facet);
            });
            this.currentPage = page;
        }
    }

    getCurrentPageable(pageNumber?: number): FacetPageable {
        return new FacetPageable(pageNumber ? pageNumber : this.currentPage?.number, SolrPagingCriterionComponent.PAGE_SIZE, this.sortMode, this.filterText);
    }

    resetList(): Promise<void> {
        return this.goToPage(1);
    }

    public refresh(page?: FacetResultPage) {
        if (page) {
            this.maxPage = Infinity;
            this.loadPage(page);
            this.loaded = true;
            this.loadedPromiseResolve();
        } else if (this.open) {
            this.goToPage(this.currentPage ? this.currentPage.number : 1);
        }
    }

    clearSelection() {
        this.selectedFacets = [];
        this.displayedFacets.forEach(fac => fac.checked = false);
        this.hasChecked = false;
        this.propagateChange([]);
        this.onChange.emit([]);
    }

    clearFilter() {
        this.filterText = "";
        this.onFilterChange();
    }

    onCheckChange(facet: FacetField) {
        if (facet.checked) {
            this.selectedFacets.push(facet);
            //this.displayedFacets = this.displayedFacets.filter(dis => dis.value != facet.value);
        } else {
            this.selectedFacets = this.selectedFacets.filter(sel => sel.value != facet.value);
        }
        this.updateHasChecked();
        let selectedValues: string[] = this.selectedFacets.map(facet => facet.value);
        this.propagateChange(selectedValues);
        this.onChange.emit(selectedValues);
    }

    updateHasChecked() {
        this.hasChecked = this.selectedFacets.length > 0;
    }

    onFilterChange() {
        // wait till the user has stopped typing for 500ms before querying
        if (this.filterTimeout <= 0) {
            this.filterTimeout = 500;
            let everySecondHandler = setInterval(() => {
                this.filterTimeout -= 100;
                if (this.filterTimeout <= 0) {
                    clearInterval(everySecondHandler);
                    this.maxPage = Infinity;
                    this.resetList();
                }
            }, 100);
        } else {
            this.filterTimeout = 500;
        }
    }

    doOpen() {
        this._open = true;
        if (!this.loaded) {
            this.resetList();
        }
        this.saveSettings();
    }

    doClose() {
        this._open = false;
        this.saveSettings();
    }

    toggle() {
        if (this._open) this.doClose();
        else this.doOpen();
    }

    get open(): boolean {
        return this._open;
    }

    writeValue(selectedFacetValues: string[]): void {
        this.loadedPromise.then(() => {
            this.selectedFacets = [];
            selectedFacetValues?.forEach(val => {
                let displayed: FacetField = this.displayedFacets.find(fac => fac.value == val);
                if (displayed) {
                    this.selectedFacets.push(displayed);
                    displayed.checked = true;
                } else {
                    let facetField: FacetField = new FacetField();
                    facetField.checked = true;
                    facetField.field = { name: this.facetName };
                    facetField.key = { name: this.facetName };
                    facetField.value = val;
                    facetField.valueCount = 0;
                    this.selectedFacets.push(facetField);
                }
            });
            this.updateHasChecked();
        });
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }

    toggleSortMode() {
        this.sortMode = this.sortMode == 'INDEX' ? 'COUNT' : 'INDEX';
        this.resetList();
        this.saveSettings();
    }

    static getHash(facetName: string, routerUrl: string): string {
        let username: string = KeycloakService.auth.authz.tokenParsed.name;
        let stringToBeHashed: string = username + '-' + facetName + '-' + routerUrl;
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = hash.substring(0, 30);
        return hex;
    }

    reloadSettings() {
        let prefStr: string = localStorage.getItem(this.hash);
        if (prefStr) {
            let pref: FacetPreferences = JSON.parse(prefStr);
            if (pref.open) {
                this._open = true;
            }
            if (pref.sortMode && pref.sortMode != this.sortMode) this.sortMode = pref.sortMode;
        }
    }

    saveSettings() {
        let pref: FacetPreferences = new FacetPreferences();
        pref.open = this.open;
        pref.sortMode = this.sortMode;
        localStorage.setItem(this.hash, JSON.stringify(pref));
    }
}

export class FacetPreferences {

    open: boolean;
    sortMode: 'INDEX' | 'COUNT';
}
