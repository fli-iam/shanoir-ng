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
import { slideDown, slideRight } from '../../shared/animations/animations';
import { FilterablePageable, Page, Pageable } from '../../shared/components/table/pageable.model';
import { FacetResultPage, FacetField, FacetPageable } from '../solr.document.model';


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
        }]  
})

export class SolrPagingCriterionComponent implements ControlValueAccessor {
   
    minSize: number = 5;
    @Input() getPage: (pageable: FacetPageable, facetName: string) => Promise<FacetResultPage>;
    displayedFacets: FacetField[] = [];
    selectedFacets: FacetField[] = [];
    @Input() label: string = "";
    @Input() facetName: string;
    hasChecked: boolean = false;
    filterText: string;
    loaded: boolean = false;
    loading: boolean = false;
    @Output() onChange: EventEmitter<string[]> = new EventEmitter();
    private _open: boolean = false;
    currentPage: FacetResultPage;
    maxPage: number = Infinity ;

    private pageLoading: number = -1; // prevent loading several times a same page
    filterTimeout: number = 0;

    protected propagateChange = (_: any) => {};
    protected propagateTouched = () => {};

    
    goToPage(pageNumber: number): Promise<void> {
        let pageable: FacetPageable = new FacetPageable(pageNumber, 15, 'INDEX', this.filterText);
        this.loading = true;
        return this.getPage(pageable, this.facetName).then(page => {
            if (!page || !page.content || page.content.length == 0) {
                this.maxPage = this.currentPage ? this.currentPage.number : 1;
            } else {
                if (page.content.length < pageable.pageSize) {
                    this.maxPage = page.number;
                }
                this.displayedFacets = [];
                page.content.forEach(facet => {
                    facet.checked = this.selectedFacets.findIndex(selectedfacet => selectedfacet.value == facet.value) != -1;
                    this.displayedFacets.push(facet);
                });
                this.currentPage = page;
            }
            this.loaded = true;
            this.loading = false;
        }).catch(error => {
            this.loading = false;
            throw error;
        });
    }

    getCurrentPageable(): FacetPageable {
        return new FacetPageable(this.currentPage?.number ? this.currentPage?.number : 1, 15, 'INDEX', this.filterText);
    }

    resetList(): Promise<void> {
        return this.goToPage(1);
    }

    public refresh(page?: FacetResultPage) {
        if (page && this.currentPage?.content) {
            // update facet counts with incoming page
            page.content.forEach((incomingFacetField, index) => {
                if (this.currentPage.content[index] && incomingFacetField.value == this.currentPage.content[index].value) {
                    this.currentPage.content[index].valueCount = incomingFacetField.valueCount;
                }
            });
        } else if (this.open) {
            this.goToPage(this.currentPage ? this.currentPage.number : 1);
        }
    }

    // onScroll(event: any) {
    //     if (!this.pagesEnded 
    //             && event.target.offsetHeight + event.target.scrollTop >= (event.target.scrollHeight - 100) // reached scroll end ?
    //             && this.pageLoading != this.currentPage.number) {
    //         this.pageLoading = this.currentPage.number ;
    //         this.appendPage(this.currentPage.number + 2);
    //     }
    // }

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
        this.loading = true;
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
    }

    doClose() {
        this._open = false;
    }

    get open(): boolean {
        return this._open;
    }

    writeValue(selectedFaceValues: string[]): void {
        this.selectedFacets = [];
        this.displayedFacets.forEach(facet => {
            facet.checked = selectedFaceValues?.includes(facet.value);
            if (facet.checked) this.selectedFacets.push(facet);
        });
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }

}