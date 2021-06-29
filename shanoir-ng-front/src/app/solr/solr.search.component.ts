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
import { DatePipe } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { DatasetService } from '../datasets/shared/dataset.service';
import { slideDown } from '../shared/animations/animations';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { LoadingBarComponent } from '../shared/components/loading-bar/loading-bar.component';
import { Page, Pageable } from '../shared/components/table/pageable.model';
import { TableComponent } from '../shared/components/table/table.component';
import { DatepickerComponent } from '../shared/date-picker/date-picker.component';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { MsgBoxService } from '../shared/msg-box/msg-box.service';
import { StudyRightsService } from '../studies/shared/study-rights.service';
import { StudyUserRight } from '../studies/shared/study-user-right.enum';
import { FacetField, FacetResultPage, SolrDocument, SolrRequest, SolrResultPage } from './solr.document.model';
import { SolrService } from './solr.service';


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
    selectionColumnDefs: any[];
    customActionDefs: any[];
    selectionCustomActionDefs: any[];
    form: FormGroup;
    @ViewChild('table', { static: false }) table: TableComponent;
    @ViewChild('selectionTable', { static: false }) selectionTable: TableComponent;
    selectedDatasetIds: Set<number> = new Set();
    allFacetResultPages: FacetResultPage[] = [];
    clearTextSearch: () => void = () => {};
    syntaxError: boolean = false;
    datasetStartDate: Date | 'invalid';
    datasetEndDate: Date | 'invalid';
    tab: 'results' | 'selected' = 'results';
    role: 'admin' | 'expert' | 'user';
    rights: Map<number, StudyUserRight[]>;

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
            private solrService: SolrService, private router: Router, private datasetService: DatasetService,
            private keycloakService: KeycloakService, private studyRightsService: StudyRightsService,
            private confirmDialogService: ConfirmDialogService, private msgBoxService: MsgBoxService) {

        this.getFacets();
        this.getRole();
        if (this.role != 'admin') this.getRights();

        this.form = this.buildForm();
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('Solr Search'); 
        this.columnDefs = this.getColumnDefs();
        this.selectionColumnDefs = this.getSelectionColumnDefs();
        this.customActionDefs = this.getCustomActionsDefs();
        this.selectionCustomActionDefs = this.getSelectionCustomActionsDefs();

        let input: string = this.router.getCurrentNavigation().extras && this.router.getCurrentNavigation().extras.state ? this.router.getCurrentNavigation().extras.state['input'] : null;
        if (input) {
            // TODO
        }
    }

    getRole() {
        if (this.keycloakService.isUserAdmin()) {
            this.role = 'admin';
        } else if (this.keycloakService.isUserExpert()) {
            this.role = 'expert';
        } else {
            this.role = 'user';
        }
    }

    getRights() {
        this.studyRightsService.getMyRights().then(rights => this.rights = rights);
    }

    hasAdminRight(studyId: number) {
        if (this.role == 'admin') return true;
        else if (this.role == 'user') return false;
        else if (this.role == 'expert') return this.rights && this.rights.has(studyId) && this.rights.get(studyId).includes(StudyUserRight.CAN_ADMINISTRATE);
    }

    hasDownloadRight(studyId: number) {
        if (this.role == 'admin') return true;
        else return this.rights && this.rights.has(studyId) && this.rights.get(studyId).includes(StudyUserRight.CAN_DOWNLOAD);
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

    refreshTable() {
        if (this.tab != 'results') {
            this.openResultTab();
            this.table.refresh();
        } else {
            this.table.refresh();
        }
    }

    openResultTab() {
        this.tab = 'results';
    }

    openSelectionTab() {
        if (this.tab != 'selected' && this.selectedDatasetIds?.size > 0) {
            this.tab = 'selected';
            this.selectionTable.refresh();
        }
    }

    onDateChange(date: Date | 'invalid') {
        if (date !== undefined && (date === null || date != 'invalid')) {
            this.updateSelections();
            this.refreshTable();
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

    protected openDeleteConfirmDialog = (solrDocument: SolrDocument) => {
        this.confirmDialogService
            .confirm(
                'Delete dataset', 
                'Are you sure you want to delete the dataset "'
                    + solrDocument.datasetName 
                    + '" with id n° ' + solrDocument.datasetId + ' ?'
            ).then(res => {
                if (res) {
                    this.datasetService.delete(parseInt(solrDocument.datasetId)).then(() => {
                        this.selectedDatasetIds.delete(parseInt(solrDocument.datasetId));
                        this.table.refresh().then(() => {
                            this.msgBoxService.log('info', 'Dataset sucessfully deleted');
                        });
                    });                    
                }
            })
    }

    protected openDeleteSelectedConfirmDialog = () => {
        this.confirmDialogService
            .confirm(
                'Delete dataset', 
                'Are you sure you want to delete ' + this.selectedDatasetIds.size + ' dataset(s) ?'
            ).then(res => {
                if (res) {
                    this.datasetService.deleteAll([...this.selectedDatasetIds]).then(() => {
                        this.selectedDatasetIds = new Set();
                        if (this.tab == 'selected') this.selectionTable.refresh();
                        this.table.refresh().then(() => {
                            this.msgBoxService.log('info', 'Datasets sucessfully deleted');
                        });
                    });                    
                }
            })
    }

    private getCommonColumnDefs() {
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
            {headerName: "Exam Date", field:"examinationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.examinationDate)},
            {headerName: "", type: "button", awesome: "fa-eye", action: item => this.router.navigate(['/dataset/details/' + item.id])}
        ];
        return columnDefs;
    }

    // Grid columns definition
    getColumnDefs() {
        let columnDefs: any[] = this.getCommonColumnDefs();
        if (this.role == 'admin') {
            columnDefs.push({headerName: "", type: "button", awesome: "fa-trash", action: this.openDeleteConfirmDialog});
        } else if (this.role == 'expert') {
            columnDefs.push({headerName: "", type: "button", awesome: "fa-trash", action: this.openDeleteConfirmDialog, condition: solrDoc => this.hasAdminRight(solrDoc.studyId)});
        }
        return columnDefs;
    }

    getSelectionColumnDefs() {
        let columnDefs: any[] = this.getCommonColumnDefs();
        columnDefs.unshift({ headerName: "", type: "button", awesome: "fa-ban", action: item => {
            this.selectedDatasetIds.delete(item.id);
            this.selectionTable.refresh();
        }}) 
        return columnDefs;
    }

    getCustomActionsDefs(): any[] {
        let customActionDefs:any = [];
        customActionDefs.push(
            {title: "Clear selection", awesome: "fa-snowplow", action: () => this.selectedDatasetIds = new Set(), disabledIfNoSelected: true},
            {title: "Download as DICOM", awesome: "fa-download", action: () => this.massiveDownload('dcm'), disabledIfNoSelected: true},
            {title: "Download as Nifti", awesome: "fa-download", action: () => this.massiveDownload('nii'), disabledIfNoSelected: true},
            {title: "Delete selected", awesome: "fa-trash", action: this.openDeleteSelectedConfirmDialog, disabledIfNoSelected: true},
        );
        return customActionDefs;
    }

    getSelectionCustomActionsDefs(): any[] {
        let customActionDefs:any = [];
        customActionDefs.push(
            {title: "Clear selection", awesome: "fa-snowplow", action: () => {
                this.selectedDatasetIds = new Set();
                this.table.clearSelection();
                this.selectionTable.refresh();
            }, disabledIfNoResult: true},
            {title: "Download as DICOM", awesome: "fa-download", action: () => this.massiveDownload('dcm'), disabledIfNoResult: true},
            {title: "Download as Nifti", awesome: "fa-download", action: () => this.massiveDownload('nii'), disabledIfNoResult: true},
            {title: "Delete selected", awesome: "fa-trash", action: this.openDeleteSelectedConfirmDialog, disabledIfNoResult: true},
        );
        return customActionDefs;
    }

    massiveDownload(type: string) {
        if (this.selectedDatasetIds) {
            this.datasetService.downloadDatasets([...this.selectedDatasetIds], type, this.progressBar);
        }
    }

    onSelectionChange (selection: any) {
        setTimeout(() => {
            this.selectedDatasetIds = selection;
        });
    }

    onRowClick(solrRequest: any) {
        this.router.navigate(['/dataset/details/' + solrRequest.datasetId]);
    }

    onSearchTextChange(search: {searchTxt: string, expertMode: boolean}) {
        this.keyword = search.searchTxt;
        this.expertMode = search.expertMode;
        this.refreshTable();
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

    getSelectedPage(pageable: Pageable): Promise<Page<any>> {
        return this.solrService.getByDatasetIds([...this.selectedDatasetIds], pageable);
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