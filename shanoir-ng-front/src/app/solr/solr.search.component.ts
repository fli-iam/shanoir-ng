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

import { Component, ViewChild } from "@angular/core";
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { BreadcrumbsService } from "../breadcrumbs/breadcrumbs.service";
import { DatasetService } from "../datasets/shared/dataset.service";
import { slideDown } from "../shared/animations/animations";
import { Pageable } from "../shared/components/table/pageable.model";
import { TableComponent } from "../shared/components/table/table.component";
import { DatepickerComponent } from "../shared/date-picker/date-picker.component";
import { FacetField, FacetResultPage, SolrRequest, SolrResultPage } from "./solr.document.model";
import { SolrService } from "./solr.service";
import { LoadingBarComponent } from '../shared/components/loading-bar/loading-bar.component';
import { DatePipe } from "@angular/common";

@Component({
    selector: 'solr-search',
    templateUrl: 'solr.search.component.html',
    styleUrls: ['solr.search.component.css'],
    animations: [slideDown],
    providers: [DatePipe]
})

export class SolrSearchComponent{

    @ViewChild('progressBar') progressBar: LoadingBarComponent;

    facetResultPages: FacetResultPage[] = [];
    keyword: string;
    expertMode: boolean = false;
    selections: SelectionBlock[] = [];
    columnDefs: any[];
    customActionDefs: any[];
    form: FormGroup;
    @ViewChild('table', { static: false }) table: TableComponent;
    hasDownloadRight: boolean = true;
    selectedDatasetIds: number[];
    allFacetResultPages: FacetResultPage[] = [];
    clearTextSearch: () => void = () => {};
    syntaxError: boolean = false;
    datasetStartDate: Date | 'invalid';
    datasetEndDate: Date | 'invalid'; 

    private requestKeys: string[] = [
        'studyName',
        'subjectName',
        'examinationComment',
        'datasetName',
        'datasetType',
        'datasetNature'
    ];

    constructor(
            private breadcrumbsService: BreadcrumbsService, private formBuilder: FormBuilder, private datePipe: DatePipe,
            private solrService: SolrService, private router: Router, private datasetService: DatasetService) {
        
        this.getFacets();

        this.form = this.buildForm();
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('Solr Search'); 
        this.columnDefs = this.getColumnDefs();
        this.customActionDefs = this.getCustomActionsDefs();

        let input: string = this.router.getCurrentNavigation().extras && this.router.getCurrentNavigation().extras.state ? this.router.getCurrentNavigation().extras.state['input'] : null;
        if (input) {
            // TODO
        }
    }
    
    buildForm(): FormGroup {
        const searchBarRegex = '^((studyName|subjectName|datasetName|examinationComment|datasetTypes|datasetNatures)[:][*]?[a-zA-Z0-9\\s_\W\.\!\@\#\$\%\^\&\*\(\)\_\+\-\=]+[*]?[;])+$';
        let formGroup = this.formBuilder.group({
            'startDate': [this.datasetStartDate, [DatepickerComponent.validator]],
            'endDate': [this.datasetEndDate, [DatepickerComponent.validator, this.dateOrderValidator]],
        });
        return formGroup;
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[]) {
        let formError = this.formErrors(fieldName);
        if (formError) {
            for(let errorName of errors) {
                if(formError[errorName]) return true;
            }
        }
        return false;
    }

    dateOrderValidator = (control: AbstractControl): ValidationErrors | null => {
        if (this.datasetStartDate && this.datasetEndDate 
            && this.datasetStartDate > this.datasetEndDate) {
                return { order: true }
        }
        return null;
    }

    updateWithKeywords(solrRequest: SolrRequest): SolrRequest {
        solrRequest.searchText = this.keyword.trim();
        solrRequest.expertMode = this.expertMode;
        return solrRequest;
    }

    updateWithFields(solrRequest: SolrRequest): SolrRequest {
        this.allFacetResultPages.forEach(facetResultPage => {
            facetResultPage.content.forEach(facetResult => {
                if (facetResult.checked) {
                    const key: string = facetResult.key.name.split('_')[0];
                    if (!solrRequest[key]) solrRequest[key] = [];
                    solrRequest[key].push(facetResult.value);
                }
            });
        });
        if (this.datasetStartDate && this.datasetStartDate != 'invalid') {
            solrRequest.datasetStartDate = this.datasetStartDate;
        }
        if (this.datasetEndDate && this.datasetEndDate != 'invalid') {
            solrRequest.datasetEndDate = this.datasetEndDate;
        }
        return solrRequest;
    }
    
    updateSelections() {
        this.selections = [];
        if (this.datasetStartDate && this.datasetStartDate != 'invalid') {
            this.selections.push(new DateSelectionBlock(
                    'from: ' + this.datePipe.transform(this.datasetStartDate, 'dd/MM/yyy'),
                    () => this.datasetStartDate = null
            ));
        }
        if (this.datasetEndDate && this.datasetEndDate != 'invalid') {
            this.selections.push(new DateSelectionBlock(
                    'to: ' + this.datePipe.transform(this.datasetEndDate, 'dd/MM/yyy'),
                    () => this.datasetEndDate = null 
            ));
        }
        this.allFacetResultPages.forEach(facetRes => {
            this.selections = this.selections.concat(
                facetRes.content
                    .filter(facetField => facetField.checked)
                    .map(facetField => new FacetSelectionBlock(facetField))
            );
        })
    }

    onDateChange(date: Date | 'invalid') {
        if (date !== undefined && (date === null || date != 'invalid')) {
            this.updateSelections();
            this.table.refresh();
        }
    }

    removeAllFacets() {
        this.selections.forEach(selection => selection.remove());
        this.selections = [];
        this.clearTextSearch();
    }

    removeSelection(index: number) {
        this.selections[index].remove();
        this.selections.splice(index, 1);
    }

    getPage(pageable: Pageable): Promise<SolrResultPage> {
        if (this.form.valid) {
            let solrRequest: SolrRequest = new SolrRequest();
            if (this.keyword) solrRequest = this.updateWithKeywords(solrRequest);
            solrRequest = this.updateWithFields(solrRequest);

            return this.solrService.search(solrRequest, pageable).then(solrResultPage => {
                if (solrResultPage) { 
                    solrResultPage.content.map(solrDoc => solrDoc.id = solrDoc.datasetId);
                    this.facetResultPages = solrResultPage.facetResultPages;
                }
                return solrResultPage;
            }).catch(reason => {
                console.log(reason.error.code, reason.error.message, reason.error.code == 422 && reason.error.message == 'solr query failed')
                if (reason.error.code == 422 && reason.error.message == 'solr query failed') {
                    this.syntaxError = true;
                    return new SolrResultPage();
                } else throw reason;
            });
        } else {
            return Promise.resolve(new SolrResultPage());
        }
    }

    // Grid columns definition
    getColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };

        let columnDefs: any = [
            {headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Name", field: "datasetName"},
            {headerName: "Type", field: "datasetType", width: "30px"},
            {headerName: "Nature", field: "datasetNature", width: "30px"},
            {headerName: "Creation", field: "datasetCreationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.datasetCreationDate)},
            {headerName: "Study", field: "studyName"},
            {headerName: "Subject", field: "subjectName"},
            {headerName: "Exam", field: "examinationComment"},
            {headerName: "Exam Date", field:"examinationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.examinationDate)}
        ];
        return columnDefs;
    }

    getCustomActionsDefs(): any[] {
        let customActionDefs:any = [];
        if (this.hasDownloadRight) {
            customActionDefs.push(
                {title: "Download as DICOM", awesome: "fa-download", action: () => this.massiveDownload('dcm'), disabledIfNoSelected: true},
                {title: "Download as Nifti", awesome: "fa-download", action: () => this.massiveDownload('nii'), disabledIfNoSelected: true}
            );
        }
        return customActionDefs;
    }

    massiveDownload(type: string) {
        this.datasetService.downloadDatasets(this.selectedDatasetIds, type, this.progressBar);
    }

    onSelectionChange (selection) {
        this.selectedDatasetIds = [];
        selection.forEach(sel => this.selectedDatasetIds.push(sel.datasetId));
    }

    onRowClick(solrRequest: any) {
        this.router.navigate(['/dataset/details/' + solrRequest.datasetId]);
    }

    onSearchTextChange(search: {searchTxt: string, expertMode: boolean}) {
        this.keyword = search.searchTxt;
        this.expertMode = search.expertMode;
        this.table.refresh();
    }

    getFacets(): Promise<SolrResultPage> {
        return this.solrService.getFacets().then(solrResultPage => {
            if (solrResultPage) { 
                solrResultPage.content.map(solrDoc => solrDoc.id = solrDoc.datasetId);
                this.allFacetResultPages = solrResultPage.facetResultPages;
            }
            return solrResultPage;
        });
    }

    registerTextResetCallback(resetTextCallback: () => void) {
        this.clearTextSearch = resetTextCallback;
    }

}

export interface SelectionBlock {
    label: string;
    remove(): void;
}

export class FacetSelectionBlock implements SelectionBlock {

    constructor(private facetField: FacetField) {}

    get label(): string {
        return this.facetField.value;
    }

    remove(): void {
        this.facetField.checked = false;
    }
}

export class DateSelectionBlock implements SelectionBlock {

    constructor(private formattedLabel: string, public remove: () => void) {}

    get label(): string {
        return this.formattedLabel;
    }
}