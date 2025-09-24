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
import {Component, EventEmitter, forwardRef, Input, Output} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import { slideDown } from '../../shared/animations/animations';


@Component({
    selector: 'solr-text-search',
    templateUrl: 'solr.text-search.component.html',
    styleUrls: ['solr.text-search.component.css'],
    animations: [slideDown],
    providers: [
        {
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => SolrTextSearchComponent),
          multi: true,
        }],
    standalone: false
})

export class SolrTextSearchComponent implements ControlValueAccessor {

    showInfo: boolean = false;
    searchText: string = "";
    searchKeyWords: string[] = ["centerName", "datasetCreationDate", "studyName", "subjectName", "subjectType", "acquisitionEquipmentName", "datasetId", "datasetName", "datasetNature", "datasetType", "processed", "examinationComment", "examinationDate", "importDate", "tags", "magneticFieldStrength", "pixelBandwidth", "sliceThickness", "studyId", "sortingIndex"];
    @Output() onChange: EventEmitter<string> = new EventEmitter();
    @Output() onType: EventEmitter<void> = new EventEmitter();
    @Output() expertModeChange: EventEmitter<boolean> = new EventEmitter();
    @Input() syntaxError: boolean = false;
    @Input() syntaxErrorMsg: string;
    expertMode: boolean = false;
    protected propagateChange: (any) => void = () => {};
    protected propagateTouched = () => {};

    inputTextChange() {
        if (this.searchKeyWords.some(word => this.searchText.includes(word))) {
            this.expertModeChange.emit(true);
        }
    }

    onChangeSearch() {
        if (!this.syntaxError) {
            this.propagateChange(this.searchText);
            this.onChange.emit(this.searchText);
        }
    }

    onExpertModeChange() {
        if (this.searchText && this.searchText.length > 0) {
            this.onChangeSearch();
        }
    }

    clear(text?: string) {
        this.searchText = text ? text : '';
    }

    writeValue(value: string): void {
        this.searchText = value;
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }
}
