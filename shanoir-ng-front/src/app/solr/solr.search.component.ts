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
import { FacetResultPage, SolrRequest, SolrResultPage } from "./solr.document.model";
import { SolrService } from "./solr.service";

@Component({
    selector: 'solr-search',
    templateUrl: 'solr.search.component.html',
    styleUrls: ['solr.search.component.css'],
    animations: [slideDown]
})

export class SolrSearchComponent{
    facetResultPages: FacetResultPage[] = [];
    solrRequest: SolrRequest = new SolrRequest();
    keyword: string;
    selections: any[];
    columnDefs: any[];
    customActionDefs: any[];
    form: FormGroup;
    @ViewChild('table', { static: false }) table: TableComponent;
    hasDownloadRight: boolean = true;
    selectedDatasetIds: number[];
    allStudies: FacetResultPage; 

    constructor(
            private breadcrumbsService: BreadcrumbsService, private formBuilder: FormBuilder,
            private solrService: SolrService, private router: Router, private datasetService:DatasetService) {
        
        this.form = this.buildForm();
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('Solr Search'); 
        this.columnDefs = this.getColumnDefs();
        this.customActionDefs = this.getCustomActionsDefs();
    }
    
    buildForm(): FormGroup {
        const searchBarRegex = '^((studyName|subjectName|datasetName|examinationComment|datasetTypes|datasetNatures)[:][*]?[a-zA-Z0-9\\s_\W]+[*]?[;])+$';
        let formGroup = this.formBuilder.group({
            'keywords': [this.keyword, Validators.pattern(searchBarRegex)],
            'studyName': [this.solrRequest.studyName],
            'subjectName': [this.solrRequest.subjectName],
            'datasetName': [this.solrRequest.datasetName],
            'examinationComment': [this.solrRequest.examinationComment],
            'startDate': [this.solrRequest.datasetStartDate, [DatepickerComponent.validator]],
            'endDate': [this.solrRequest.datasetEndDate, [DatepickerComponent.validator, this.dateOrderValidator]],
            'datasetTypes': [this.solrRequest.datasetType],
            'datasetNatures': [this.solrRequest.datasetNature]
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
        if (this.solrRequest.datasetStartDate && this.solrRequest.datasetEndDate 
            && this.solrRequest.datasetStartDate > this.solrRequest.datasetEndDate) {
                return { order: true }
        }
        return null;
    }

    splitKeywords() {
        let keywords = this.keyword.split(';', this.keyword.split(';').length - 1);
        Object.keys(this.solrRequest).forEach(key => {
            keywords.forEach(keyword => {
                if (keyword.split(':')[0] == key) {
                    if (!this.solrRequest[key]) this.solrRequest[key] = []; 
                    if (!this.solrRequest[key].includes(keyword.split(':')[1])) this.solrRequest[key].push(keyword.split(':')[1])
                }
            })
        }) 
    }    
    
    updateSelections() {
        if (this.keyword) this.splitKeywords();
        this.selections = Object.keys(this.solrRequest).map((key)=>{return {key:key, value:this.solrRequest[key]} });
    }

    removeAllFacets() {
        this.selections = null;
        this.keyword = null;
        for (let key of Object.keys(this.solrRequest)) {
            this.solrRequest[key] = null;
        }
    }

    removeSelection(keyS:string, valueS: string) {
        for (let key of Object.keys(this.solrRequest)) {
            if (key && this.solrRequest[key] && key == keyS && this.solrRequest[key].includes(valueS)) {
                this.solrRequest[key] = this.solrRequest[key].filter(item => item !== valueS);
                if (this.keyword && this.keyword.includes(valueS)) this.keyword = this.keyword.replace(key + ':' + valueS + ';', '');
                if (this.solrRequest[key].length == 0) this.solrRequest[key] = null;
            } 
        }
    }

    showAllStudies() {
        this.solrRequest.studyName = null;
    }

    getPage(pageable: Pageable): Promise<SolrResultPage> {
        if (this.form.valid) {
            this.updateSelections();
            let savedStates = [];

            for (let key of Object.keys(this.solrRequest)) {
                if (key && this.solrRequest[key] && !(this.solrRequest[key] instanceof Date)) savedStates.push(this.solrRequest[key]);
            }
            return this.solrService.search(this.solrRequest, pageable).then(solrResultPage => {
                if (solrResultPage) { 
                    solrResultPage.content.map(solrDoc => solrDoc.id = solrDoc.datasetId);
                
                    if (!savedStates[0]) this.allStudies = solrResultPage['facetResultPages'][0];
                    solrResultPage['facetResultPages'].forEach((facetResultPage, i) => {
                        facetResultPage.content.forEach((facetField, j) => {
                            if (savedStates[i] && savedStates[i].includes(facetField.value)) facetField.checked = true;
                            facetResultPage.content[j] = facetField;
                            this.facetResultPages[i] = facetResultPage;
                        })
                    })} 
                return solrResultPage;
            });
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
                {title: "dcm",awesome: "fa-download", action: () => this.massiveDownload('dcm')},
                {title: "nii",awesome: "fa-download", action: () => this.massiveDownload('nii')}
            );
        }
        return customActionDefs;
    }

    massiveDownload(type: string) {
        this.datasetService.downloadDatasets(this.selectedDatasetIds, type);
    }

    onSelectionChange (selection) {
        this.selectedDatasetIds = [];
        selection.forEach(sel => this.selectedDatasetIds.push(sel.datasetId));
    }

    onRowClick(solrRequest: any) {
        this.router.navigate(['/dataset/details/' + solrRequest.datasetId]);
    }
}