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


@Component({
    selector: 'solr-text-search',
    templateUrl: 'solr.text-search.component.html',
    styleUrls: ['solr.text-search.component.css'],
    animations: [slideDown]
})

export class SolrTextSearchComponent implements OnChanges {

    showInfo: boolean = false;
    searchText: string = "";
    @Input() registerResetCallback : (reset:() => void) => void;
    @Output() onChange: EventEmitter<{searchTxt: string, expertMode: boolean}> = new EventEmitter();
    @Output() onType: EventEmitter<void> = new EventEmitter();
    @Input() syntaxError: boolean = false;
    expertMode: boolean = false;

    onChangeSearch() {
        if (this.expertMode) {
            if (!this.syntaxError) {
                this.onChange.emit({searchTxt: this.searchText, expertMode: true});        
            }
        } else { 
            this.onChange.emit({searchTxt: this.searchText, expertMode: false}); 
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['registerResetCallback'] && this.registerResetCallback && changes['registerResetCallback'].firstChange) {
            this.registerResetCallback(this.clear.bind(this));
        }
    }

    onExpertModeChange() {
        if (this.searchText && this.searchText.length > 0) {
            this.onChangeSearch();
        }
    }

    clear(text?: string, expertMode?: boolean) {
        this.searchText = text ? text : '';
        this.expertMode = !!expertMode;
    }
}