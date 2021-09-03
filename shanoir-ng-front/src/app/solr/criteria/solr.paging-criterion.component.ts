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
import { FilterablePageable, Page, Pageable } from '../../shared/components/table/pageable.model';
import { FacetResultPage, FacetField } from '../solr.document.model';


@Component({
    selector: 'solr-paging-criterion',
    templateUrl: 'solr.paging-criterion.component.html',
    styleUrls: ['solr.criterion.component.css'], 
    animations: [slideDown]
})

export class SolrPagingCriterionComponent {
    
    minSize: number = 5;
    @Input() getPage: (pageable: Pageable) => Promise<FacetResultPage>;
    displayedFacets: FacetField[] = [];
    selectedFacets: FacetField[] = [];
    @Input() label: string = "";
    hasChecked: boolean = false;
    filterText: string;
    filteredCount: number;
    totalCount: number;
    expanded: boolean = false;
    expandable: boolean;
    loaded: boolean = false;
    @Output() onChange: EventEmitter<string[]> = new EventEmitter();
    open: boolean = false;


    update() {
        this.displayedFacets = [];
        this.selectedFacets = [];
        this.expandable = false;

        let filter = null;
        let pageable: Pageable = new FilterablePageable(1, 100, null, filter);
        this.getPage(pageable).then(page => {
            this.displayedFacets = page.content;
            if (!filter) {
                this.totalCount = page.totalElements;
            } else {
                this.filteredCount = page.totalElements;
            }
        })
    }

    clearSelection() {
        // TODO
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
        this.hasChecked = false; // TODO
    }
    
    onFilterChange() {
        this.update();
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