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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { slideDown } from '../../shared/animations/animations';
import { FacetResultPage, FacetField } from '../solr.document.model';


@Component({
    selector: 'solr-criterion',
    templateUrl: 'solr.criterion.component.html',
    styleUrls: ['solr.criterion.component.css'], 
    animations: [slideDown]
})

export class SolrCriterionComponent implements OnChanges {
    
    minSize: number = 5;
    @Input() allFacetPage: FacetResultPage;
    @Input() currentFacetPage: FacetResultPage;
    displayedFacets: FacetField[] = [];
    selectedFacets: FacetField[] = [];
    @Input() label: string = "";
    hasChecked: boolean = false;
    filterText: string;
    filteredCount: number;
    expanded: boolean = false;
    expandable: boolean;
    loaded: boolean = false;
    @Output() onChange: EventEmitter<string[]> = new EventEmitter();
    open: boolean = false;

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.allFacetPage && this.allFacetPage) {
            this.sortAllAlphabetically();
            this.loaded = true;
        }
        if (changes.currentFacetPage && this.currentFacetPage && this.allFacetPage) {
            this.updateCheckedAndCount();
            this.update();
        }
    }

    update() {
        //this.sortAll();
        this.filter();
        this.displayedFacets = [];
        this.selectedFacets = [];
        this.expandable = false;
        if ((this.filteredCount != null && this.filteredCount <= this.minSize)
                || (this.filteredCount == null && this.allFacetPage.content.length <= this.minSize)) {
            this.displayedFacets = this.allFacetPage.content;
        } else {
            this.allFacetPage.content.forEach((facet, i) => {
                if (this.expanded) {
                    if (facet.hidden && facet.checked) this.selectedFacets.push(facet);
                    else this.displayedFacets.push(facet);
                } else {
                    if (this.displayedFacets.length <= this.minSize && !facet.hidden) {
                        this.displayedFacets.push(facet);
                    }
                    // else if (facet.hidden && facet.checked) {
                    //     this.selectedFacets.push(facet);
                    // }
                    // } else if (facet.checked) {
                    //     this.selectedFacets.push(facet);
                    else if (this.displayedFacets.length > this.minSize && !facet.hidden) {
                        this.expandable = true;
                    }
                }
            });
        }
    }

    updateCheckedAndCount() { 
        this.allFacetPage.content.forEach((facet, i) => {
            let foundFacet: FacetField = this.currentFacetPage.content.find(currentFacet => currentFacet.value == facet.value);
            if (foundFacet) {
                //facet.checked = foundFacet.checked;
                facet.valueCount = foundFacet.valueCount;
            } else {
                facet.valueCount = 0;
            }
        });
        this.updateHasChecked();
    }

    sortAllByNumber() {
        this.allFacetPage.content.sort((a, b) => {
            if (a.valueCount == 0 && b.valueCount == 0) return 0;
            else if (a.valueCount > 0 && b.valueCount == 0) return -1;
            else if (a.valueCount == 0 && b.valueCount > 0) return 1;
            else if (a.valueCount > 0 && b.valueCount > 0) return 0;
        });
    }

    sortAllAlphabetically() {
        this.allFacetPage.content.sort((a, b) => {
            return a.value.localeCompare(b.value);
        });
    }

    clearSelection() {
        if (this.allFacetPage) {
            this.allFacetPage.content.forEach(facetField => facetField.checked = false);
            this.updateHasChecked();
            this.update();
        }
        this.onChange.emit();
    }

    clearFilter() {
        this.filterText = "";
        this.onFilterChange();
    }
    
    onCheckChange() {
        this.updateHasChecked();
        this.update();
        this.onChange.emit();
        // this.allFacetPage.content.filter(facet => facet.checked).map(facet => facet.value)
    }
    
    updateHasChecked() {
        this.hasChecked = this.allFacetPage && this.allFacetPage.content.filter(facetField => facetField.checked).length > 0;
    }
    
    onFilterChange() {
        this.update();
    }

    filter() {
        if (this.allFacetPage) {
            if (!this.filterText || this.filterText.trim().length == 0) {
                this.allFacetPage.content.forEach(facetField => {
                    facetField.hidden = false;
                });
                this.filteredCount = null;
            } else {
                this.filteredCount = 0;
                let terms: string[] = this.filterText.trim().toUpperCase().split(' ');
                this.allFacetPage.content.forEach(facetField => {
                    facetField.hidden = true;
                    terms.forEach(term => {
                        if (facetField.value.toUpperCase().includes(term)) {
                            facetField.hidden = false;
                            // Highlight ? (remove return)
                            return;
                        }
                    });
                    if (!facetField.hidden) this.filteredCount++;
                });
            }
        }
    }

    expand() {
        this.expanded = true;
        this.update();
    }

    shrink() {
        this.expanded = false;
        this.update();
    }

}