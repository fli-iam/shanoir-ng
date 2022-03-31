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
import { Page, Pageable } from '../shared/components/table/pageable.model';
import { Range } from '../shared/models/range.model';


export class SolrDocument {
    datasetId: string;
    datasetName: string;
    datasetType: string;
    datasetNature: string;
    datasetCreationDate: Date;
    examinationComment: string;
    centerName: string;
    tags: string;
    examinationDate: Date;
    subjectName: string;
    studyName: string;
    studyId: string;
    id: string; // only for the table component..
}

export class SolrRequest {
    studyName: string[];
    subjectName: string[];
    examinationComment: string[];
    centerName: string[];
    datasetName: string[];
    datasetStartDate: Date | 'invalid';
    datasetEndDate: Date | 'invalid';
    datasetType: string[];
    datasetNature: string[];
    tags: string[];
    searchText: string;
    expertMode: boolean = false;
    sliceThickness: Range = new Range(null, null);
    pixelBandwidth: Range = new Range(null, null);
    magneticFieldStrength: Range = new Range(null, null);
    facetPaging: Map<String, FacetPageable>;
 }

export class FacetField {
    field: { name: string };
    key: { name: string };
    value: string;
    valueCount: number;
    checked: boolean;
    hidden: boolean;
}

export class FacetResultPage extends Page<FacetField>{}

export class SolrResultPage extends Page<SolrDocument>{

    facetResultPages: FacetResultPage[];
}

export class FacetPageable {
    constructor(
        public pageNumber: number,
        public pageSize: number,
        public facetOrder?: 'COUNT' | 'INDEX',
        public filter?: string
    ) {}

    static build(pageable: Pageable, filter: string): FacetPageable {
        return new FacetPageable(pageable.pageNumber, pageable.pageSize, 'COUNT', filter);
    }
}