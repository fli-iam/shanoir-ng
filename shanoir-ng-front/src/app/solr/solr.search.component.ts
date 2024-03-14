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
import { formatDate } from '@angular/common';
import { AfterContentInit, Component, ComponentRef, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { DatasetService } from '../datasets/shared/dataset.service';
import { slideDown } from '../shared/animations/animations';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';

import { AfterViewChecked } from "@angular/core";
import { environment } from "../../environments/environment";
import { DatasetAcquisition } from '../dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from '../dataset-acquisitions/shared/dataset-acquisition.service';
import { ExecutionDataService } from '../vip/execution.data-service';
import { LoadingBarComponent } from '../shared/components/loading-bar/loading-bar.component';
import { ColumnDefinition } from '../shared/components/table/column.definition.type';
import { Page, Pageable } from "../shared/components/table/pageable.model";
import { TableComponent } from "../shared/components/table/table.component";
import { ConsoleService } from '../shared/console/console.service';
import { DatepickerComponent } from "../shared/date-picker/date-picker.component";
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { MassDownloadService } from '../shared/mass-download/mass-download.service';
import { Range } from '../shared/models/range.model';
import { StudyRightsService } from '../studies/shared/study-rights.service';
import { StudyUserRight } from '../studies/shared/study-user-right.enum';
import { FacetPreferences, SolrPagingCriterionComponent } from './criteria/solr.paging-criterion.component';
import { FacetField, FacetPageable, FacetResultPage, SolrDocument, SolrRequest, SolrResultPage } from './solr.document.model';
import { SolrService } from "./solr.service";
import { Clipboard } from '@angular/cdk/clipboard';
import {StudyService} from "../studies/shared/study.service";
import {Study} from "../studies/shared/study.model";
import {ServiceLocator} from "../utils/locator.service";
import { Observable } from 'rxjs-compat';
import { SuperPromise } from 'src/app/utils/super-promise';
import {take} from "rxjs/operators";
import {Format} from "@angular-devkit/build-angular/src/builders/extract-i18n/schema";
import { TaskState } from '../async-tasks/task.model';
import {DatasetCopyDialogComponent} from "../shared/components/dataset-copy-dialog/dataset-copy-dialog.component";

const TextualFacetNames: string[] = ['studyName', 'subjectName', 'subjectType', 'acquisitionEquipmentName', 'examinationComment', 'datasetName', 'datasetType', 'datasetNature', 'tags'];
const RangeFacetNames: string[] = ['sliceThickness', 'pixelBandwidth', 'magneticFieldStrength'];
export type TextualFacet = typeof TextualFacetNames[number];
@Component({
    selector: 'solr-search',
    templateUrl: 'solr.search.component.html',
    styleUrls: ['solr.search.component.css'],
    animations: [slideDown],
})

export class SolrSearchComponent implements AfterViewChecked, AfterContentInit {

    progressState: TaskState = new TaskState();
    @ViewChildren(SolrPagingCriterionComponent) pagingCriterion: QueryList<SolrPagingCriterionComponent>;
    selections: SelectionBlock[] = [];
    columnDefs: ColumnDefinition[];
    selectionColumnDefs: ColumnDefinition[];
    customActionDefs: any[];
    selectionCustomActionDefs: any[];
    form: UntypedFormGroup;
    @ViewChild('table', { static: false }) table: TableComponent;
    @ViewChild('selectionTable', { static: false }) selectionTable: TableComponent;
    selectedDatasetIds: Set<number> = new Set();
    syntaxError: boolean = false;
    dateOpen: boolean = false;
    datasetStudymap: Map<number, number> = new Map();

    tab: 'results' | 'selected' = 'results';
    role: 'admin' | 'expert' | 'user';
    rights: Map<number, StudyUserRight[]>;
    loaded: boolean = false;
    firstPageLoaded: boolean = false;
    viewChecked: boolean = false;
    canDownload: boolean = true;
    solrRequest: SolrRequest = new SolrRequest();
    private facetPageable: Map<string, FacetPageable>;
    contentPage: SolrResultPage[] = [];

    studies: Study[];
    selectedStudies: string[]=[];
    hasCopyRight: boolean = false;
    selectedLines: SolrDocument[]=[];

    constructor(
            private breadcrumbsService: BreadcrumbsService, private formBuilder: UntypedFormBuilder,
            private solrService: SolrService, private router: Router, private datasetService: DatasetService, private datasetAcquisitionService: DatasetAcquisitionService,
            private keycloakService: KeycloakService, private studyRightsService: StudyRightsService, private downloadService: MassDownloadService, private clipboard: Clipboard,
            private confirmDialogService: ConfirmDialogService, private consoleService: ConsoleService, private processingService: ExecutionDataService, private studyService: StudyService) {

        this.getRole();
        if (this.role != 'admin') this.getRights();

        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('Solr Search');

        this.form = this.buildForm();
        this.columnDefs = this.getColumnDefs();
        this.selectionColumnDefs = this.getSelectionColumnDefs();
        this.customActionDefs = this.getCustomActionsDefs();
        this.selectionCustomActionDefs = this.getSelectionCustomActionsDefs();
        this.studyService.getAll().then(studies => {
            this.studies = studies;
        });

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
        else return this.rights && this.rights.has(studyId) && this.rights.get(studyId).includes(StudyUserRight.CAN_ADMINISTRATE);
    }

    hasDownloadRight(studyId: number) {
        if (this.role == 'admin') return true;
        else return this.rights && this.rights.has(studyId) && this.rights.get(studyId).includes(StudyUserRight.CAN_DOWNLOAD);
    }

    ngAfterViewChecked(): void {
        if (!this.viewChecked) {
            setTimeout(() => {
                this.viewChecked = true;
            });
        }
    }

    ngAfterContentInit(): void {
        if (this.breadcrumbsService.currentStep && this.breadcrumbsService.currentStep.data.solrRequest) {
            this.loadState();
            this.updateSelections();
        }
        this.loaded = true;
    }

    buildForm(): UntypedFormGroup {
        const searchBarRegex = '^((studyName|subjectName|datasetName|examinationComment|datasetTypes|datasetNatures|acquisitionEquipmentName)[:][*]?[a-zA-Z0-9\\s_\W\.\!\@\#\$\%\^\&\*\(\)\_\+\-\=]+[*]?[;])+$';
        let formGroup = this.formBuilder.group({
            'startDate': [this.solrRequest.datasetStartDate, [DatepickerComponent.validator]],
            'endDate': [this.solrRequest.datasetEndDate, [DatepickerComponent.validator, this.dateOrderValidator]],
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

    private saveState() {
        this.breadcrumbsService.currentStep.data.solrRequest = this.solrRequest;
    }

    private loadState() {
        let savedRequest: SolrRequest = this.breadcrumbsService.currentStep.data.solrRequest;
        this.solrRequest = savedRequest;
    }

    updateSelections() {
        this.selections = [];
        if (this.solrRequest.datasetStartDate && this.solrRequest.datasetStartDate != 'invalid') {
            this.selections.push(new DateSelectionBlock(
                    'from: ' + formatDate(this.solrRequest.datasetStartDate, 'dd/MM/yyy', 'en-US', 'UTC'),
                    () => this.solrRequest.datasetStartDate = null
            ));
        }
        if (this.solrRequest.datasetEndDate && this.solrRequest.datasetEndDate != 'invalid') {
            this.selections.push(new DateSelectionBlock(
                    'to: ' + formatDate(this.solrRequest.datasetEndDate, 'dd/MM/yyy', 'en-US', 'UTC'),
                    () => this.solrRequest.datasetEndDate = null
            ));
        }
        TextualFacetNames.forEach(facetName => {
            if (this.solrRequest[facetName] && Array.isArray(this.solrRequest[facetName])) {
                (this.solrRequest[facetName] as []).forEach(facetVal => {
                    this.selections = this.selections.concat(
                        new SimpleValueSelectionBlock(facetVal, () => {
                            this.solrRequest[facetName] = this.solrRequest[facetName].filter(val => val != facetVal);
                        })
                    );

                })
            }
        })

        if (this.solrRequest.sliceThickness?.hasBound()) {
            this.selections.push(new RangeSelectionBlock('S. Thickness', this.solrRequest.sliceThickness));
        }
        if (this.solrRequest.pixelBandwidth?.hasBound()) {
            this.selections.push(new RangeSelectionBlock('P. Bandwidth', this.solrRequest.pixelBandwidth));
        }
        if (this.solrRequest.magneticFieldStrength?.hasBound()) {
            this.selections.push(new RangeSelectionBlock('Mag. Strength', this.solrRequest.magneticFieldStrength));
        }
    }

    refreshTable(updatedFacetName?: string) {
        this.facetPageable = this.buildFacetPageable(updatedFacetName);
        if (this.tab != 'results') this.openResultTab();
        this.table.refresh(1);
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
        if (this.loaded && (date === null || (date && ('invalid' != date)))) {
            this.updateSelections();
            this.refreshTable();
        }
    }

    removeAllFacets() {
        this.selections.forEach(selection => selection.remove());
        this.selections = [];
        this.solrRequest = new SolrRequest();
    }

    removeSelection(index: number) {
        this.selections[index].remove();
        this.selections.splice(index, 1);
    }

    getPage(pageable: Pageable): Promise<SolrResultPage> {
        if (this.form.valid) {
            this.saveState();
            this.solrRequest.facetPaging = this.facetPageable ? this.facetPageable : this.buildFacetPageable();
            let facetPagingTmp: Map<String, FacetPageable> = new Map(this.solrRequest.facetPaging); // shallow copy
            let search = this.solrService.search(this.solrRequest, pageable).then(solrResultPage => {
                // populate criteria
                if (solrResultPage) {
                    this.pagingCriterion.forEach(criterionComponent => {
                        if (facetPagingTmp?.has(criterionComponent.facetName)) {
                            let facetPage: FacetResultPage = solrResultPage.facetResultPages.find(facetResPage => facetResPage.content[0]?.key?.name == criterionComponent.facetName)
                            if (!facetPage) facetPage = new FacetResultPage();
                            criterionComponent.refresh(facetPage);
                        }
                    });
                }
                this.firstPageLoaded = true;
                this.contentPage.push(solrResultPage);

                return solrResultPage;
            }).catch(reason => {
                if (reason?.error?.code == 422 && reason.error.message == 'solr query failed') {
                    this.syntaxError = true;
                    return new SolrResultPage();
                } else throw reason;
            });
            this.solrRequest.facetPaging = null;
            this.facetPageable = null;
            return search;
        } else {
            return Promise.resolve(new SolrResultPage());
        }
    }

    private buildFacetPageable(updatedFacetName?: string): Map<string, FacetPageable> {
        let map = new Map();
        if (!this.firstPageLoaded) { // dig into criterion save states before they load to avoid unneeded requests
            TextualFacetNames.forEach(facetName => {
                let hash: string = SolrPagingCriterionComponent.getHash(facetName, this.router.url);
                let prefStr: string = localStorage.getItem(hash);
                if (prefStr) {
                    let pref: FacetPreferences = JSON.parse(prefStr);
                    if (pref.open) {
                        map.set(facetName, new FacetPageable(1, SolrPagingCriterionComponent.PAGE_SIZE, pref.sortMode ? pref.sortMode : 'INDEX'));
                    }
                }
            });
        } else {
            if (this.pagingCriterion) { // add facet request
                this.pagingCriterion.forEach(criterion => {
                    if (criterion.open) {
                        if (updatedFacetName && updatedFacetName == criterion.facetName) {
                            map.set(criterion.facetName, criterion.getCurrentPageable());
                        } else {
                            map.set(criterion.facetName, criterion.getCurrentPageable(1));
                        }
                    }
                });
            }
        }
        return map;
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
                            this.consoleService.log('info', 'Dataset n°' + solrDocument.datasetId + 'was sucessfully deleted');
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
                    this.datasetService.deleteAll([...this.selectedDatasetIds]).then((deleted) => {
                        this.selectedDatasetIds = new Set();
                        if (this.tab == 'selected') this.selectionTable.refresh();
                        this.table.refresh().then(() => {
                            this.consoleService.log('info', 'Datasets sucessfully deleted', ['ids : ' + [...this.selectedDatasetIds].join(', ')]);
                        });
                    }).catch(reason => {
                        if(reason?.error?.code == 403) {
                            this.confirmDialogService.error('Impossible',
                                'Some of the selected datasets belong to studies that you don\'t administrate. '
                                + 'You cannot delete those datasets. Please select only datasets that you can administrate (see the yellow shield icon) and try again. '
                                +' If you really need to delete those datasets, contact an administrator for the corresponding study '
                                + ' and ask him to grant you the administrator role in the study.');
                        }
                        if(reason?.status == 422) {
                            let warn = 'At least on of the selected dataset is linked to other entities, it was not deleted.';
                            this.consoleService.log('warn', warn);
                            return false;
                        } else throw Error(reason);
                    });
                }
            });
    }

    protected openApplyStudyCard = () => {
        this.datasetAcquisitionService.getAllForDatasets([...this.selectedDatasetIds]).then(acquisitions => {
            if (this.role != 'admin') {
                let nonAdminAcqs: DatasetAcquisition[] = acquisitions?.filter(acq =>
                    !this.rights?.get(acq.examination?.study?.id)?.includes(StudyUserRight.CAN_ADMINISTRATE)
                );
                let studies: Set<string> = new Set();
                nonAdminAcqs.forEach(acq => studies.add(acq.examination?.study?.name));
                if (nonAdminAcqs.length > 0) {
                    this.confirmDialogService.error('Invalid selection', 'You don\'t have the right to apply studycards on data from studies you don\'t administrate. '
                        + 'Remove datasets that belongs to the following study(ies) from your selection : ' + [...studies].join(', '));
                } else {
                    this.router.navigate(['study-card/apply-on-datasets']).then(success => {
                        this.breadcrumbsService.currentStep.data.datasetIds = this.selectedDatasetIds;
                    });
                }
            } else {
                this.router.navigate(['study-card/apply-on-datasets']).then(success => {
                    this.breadcrumbsService.currentStep.data.datasetIds = this.selectedDatasetIds;
                });
            }
        });


    }

    private getCommonColumnDefs() {

        function dateRenderer(date: number) {
            if (date) {
                return formatDate(new Date(date),'dd/MM/yyyy', 'en-US', 'UTC');
            }
            return null;
        };
        let columnDefs: ColumnDefinition[] = [
            {headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Admin", type: "boolean", cellRenderer: row => this.hasAdminRight(row.data.studyId), awesome: "fa-solid fa-shield", color: "goldenrod", disableSorting: true},
            {headerName: "Name", field: "datasetName"},
            {headerName: "Tags", field: "tags"},
            {headerName: "Type", field: "datasetType"},
            {headerName: "Nature", field: "datasetNature"},
            {headerName: "Creation", field: "datasetCreationDate", type: "date", hidden: true, cellRenderer: (params: any) => dateRenderer(params.data.datasetCreationDate)},
            {headerName: "Study", field: "studyName",
                route: item => '/study/details/' + item.studyId
            },
            {headerName: "Subject", field: "subjectName",
                route: item => '/subject/details/' + item.subjectId
            },
            {headerName: "Center", field: "centerName",
                route: item => '/center/details/' + item.centerId
            },
            {headerName: "Exam", field: "examinationComment",
                route: item => '/examination/details/' + item.examinationId
            },
            {headerName: "Exam Date", field:"examinationDate", type: "date", cellRenderer: (params: any) => {
                return dateRenderer(params.data.examinationDate);
              }},
            {headerName: "Slice", field: "sliceThickness"},
            {headerName: "Pixel", field: "pixelBandwidth"},
            {headerName: "Mag. strength", field: "magneticFieldStrength"},
            {headerName: "OHIF Viewer", type: "button", awesome: "fa-solid fa-up-right-from-square",
              condition: item => (item.datasetType.includes("MR") || item.datasetType.includes("Pet") || item.datasetType.includes("Ct")),
              action: item => {
                window.open(environment.viewerUrl + '/viewer/1.4.9.12.34.1.8527.' + item.examinationId, '_blank');
              }
            }
        ];
        return columnDefs;
    }

    // Grid columns definition
    getColumnDefs(): ColumnDefinition[] {
        let columnDefs: ColumnDefinition[] = this.getCommonColumnDefs();
        if (this.role == 'admin') {
            columnDefs.push({headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: this.openDeleteConfirmDialog});
        } else if (this.role == 'expert') {
            columnDefs.push({headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: this.openDeleteConfirmDialog, condition: solrDoc => this.hasAdminRight(solrDoc.studyId)});
        }
        return columnDefs;
    }

    getSelectionColumnDefs(): ColumnDefinition[] {
        let columnDefs: ColumnDefinition[] = this.getCommonColumnDefs();
        columnDefs.unshift({ headerName: "", type: "button", awesome: "fa-solid fa-ban", action: item => {
            this.selectedDatasetIds.delete(item.id);
            this.selectionTable.refresh();
            this.prepareForCopy();
        }})

        return columnDefs;
    }

    getCustomActionsDefs(): any[] {
        let customActionDefs:any = [];
        customActionDefs.push(
            {title: "Clear selection", awesome: "fa-solid fa-snowplow", action: () => this.selectedDatasetIds = new Set(), disabledIfNoSelected: true},
            {title: "Delete selected", awesome: "fa-regular fa-trash", action: this.openDeleteSelectedConfirmDialog, disabledIfNoSelected: true},
            {title: "Apply Study Card", awesome: "fa-solid fa-shuffle", action: this.openApplyStudyCard, disabledIfNoSelected: true},
            {title: "Run a process", awesome: "fa-rocket", action: () => this.initExecutionMode() ,disabledIfNoSelected: true },
            {title: "Download", awesome: "fa-solid fa-download", action: () => this.downloadSelected(), disabledIfNoSelected: true},
            {title: "Copy selected ids", awesome: "fa-solid fa-copy", action: () => this.copyIds(), disabledIfNoSelected: true },
            {title: "Copy to study", awesome: "fa-solid fa-copy", action: () => this.copyToStudy(), disabledIfNoSelected: true }
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
            {title: "Delete selected", awesome: "fa-regular fa-trash", action: this.openDeleteSelectedConfirmDialog, disabledIfNoResult: true},
            {title: "Apply Study Card", awesome: "fa-solid fa-shuffle", action: this.openApplyStudyCard, disabledIfNoResult: true},
            {title: "Run a process", awesome: "fa-rocket", action: () => this.initExecutionMode() ,disabledIfNoResult: true },
            {title: "Download", awesome: "fa-solid fa-download", action: () => this.downloadSelected(), disabledIfNoResult: true},
            {title: "Copy selected ids", awesome: "fa-solid fa-copy", action: () => this.copyIds(), disabledIfNoResult: true },
            {title: "Copy to study", awesome: "fa-solid fa-copy", action: () => this.copyToStudy(), disabledIfNoResult: true }
        );
        return customActionDefs;
    }
    downloadSelected() {
        if (this.selectedDatasetIds && this.canDownload) {
            this.downloadService.downloadByIds([...this.selectedDatasetIds]);
        } else {
            this.consoleService.log('error', "Could not download data, please check your right on the studies for these datasets.")
        }
    }

    onSelectionChange (selection: Set<any>) {

        let downloadable = true;
        selection.forEach((datasetid) => {
            let selected: any = this.table?.page?.content?.find((element: any) => element.id == datasetid);
            let studyId
            if (selected) {
                studyId = selected.studyId;
                this.datasetStudymap.set(datasetid, studyId);
            } else {
                studyId = this.datasetStudymap.get(datasetid);
            }
            if (!this.hasDownloadRight(studyId)) {
                downloadable = false;
            }
        });
        this.canDownload = downloadable;

        this.selectedDatasetIds = selection;
        this.prepareForCopy();
    }

    prepareForCopy() {
        // Fill arrays with needed info for "copy to study" action
        this.selectedStudies = [];
        this.selectedLines = [];
        this.hasCopyRight = false;
        for (let page of this.contentPage) {
            for (let line of page.content) {
                if (this.selectedDatasetIds.has(Number(line.datasetId))) {
                    this.selectedLines.push(line);
                    if (!this.selectedStudies.includes(line.studyId)) {
                        this.selectedStudies.push(line.studyId);
                    }
                }
            }
        }
        this.hasCopyRight = this.selectedStudies.every(data => {
            return (this.hasAdminRight(Number(data)) == true)
        });
        if (this.selectedDatasetIds.size == 0) this.hasCopyRight = false;
    }

    rowClick(item): string {
        return '/dataset/details/' + item.datasetId;
    }

    getSelectedPage(pageable: Pageable): Promise<Page<any>> {
        if (this.selectedDatasetIds && this.selectedDatasetIds.size > 0) {
            return this.solrService.getByDatasetIds([...this.selectedDatasetIds], pageable);
        } else {
            return Promise.resolve(null);
        }
    }

    getFacetFieldPage(pageable: FacetPageable, facetName: string): Promise<FacetResultPage> {
        return this.solrService.getFacet(facetName, pageable, this.solrRequest);
    }

    initExecutionMode() {
        let noAdminStudies: Set<string> = new Set();
        this.datasetAcquisitionService.getAllForDatasets([...this.selectedDatasetIds]).then(acquisitions => {
            acquisitions.forEach(acq => {
                if (!this.hasAdminRight(acq.examination?.study?.id)) {
                    noAdminStudies.add(acq.examination?.study?.name);
                }
            });
            if(noAdminStudies.size > 0){
                this.confirmDialogService.error('Invalid selection', 'You don\'t have the right to run pipelines on studies you don\'t administrate. '
                    + 'Remove datasets that belongs to the following study(ies) from your selection : ' + [...noAdminStudies].join(', '));
            }else{
                this.processingService.setDatasets(this.selectedDatasetIds);
                this.router.navigate(['pipelines']);
            }
        });
    }

    copyIds() {
        this.clipboard.copy(Array.from(this.selectedDatasetIds || []).toString());
    }

    copyToStudy() {
        let modalRef: ComponentRef<DatasetCopyDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(DatasetCopyDialogComponent);
        modalRef.instance.title = "Copy of datasets to study";
        modalRef.instance.studies = this.studies;
        modalRef.instance.datasetsIds = Array.from(this.selectedDatasetIds);
        modalRef.instance.message = "You need admin rights on dataset's study AND destination study. Also, note that the dataset's center will be added to destination study.";
        modalRef.instance.statusMessage = 'Ready';
        modalRef.instance.ownRef = modalRef;
        modalRef.instance.canCopy = this.hasCopyRight;
        modalRef.instance.lines = this.selectedLines;
    }
}

export interface SelectionBlock {
    label: string;
    remove(): void;
}

export class SimpleValueSelectionBlock implements SelectionBlock {

    constructor(private _label: string, public remove: () => void) {}

    get label(): string {
        return this._label;
    }
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

export class RangeSelectionBlock implements SelectionBlock {

    constructor(private title: string, public range: Range) {}

    remove(): void {
        this.range.upperBound = null;
        this.range.lowerBound = null;
    }

    get label(): string {
        if (this.range.hasLowerBound() && this.range.hasUpperBound()) {
            return this.title + ' [' + this.range.lowerBound + ', ' + this.range.upperBound + ']';
        } else if (this.range.hasLowerBound() && !this.range.hasUpperBound()) {
            return this.title + ' > ' + this.range.lowerBound;
        } else if (!this.range.hasLowerBound() && this.range.hasUpperBound()) {
            return this.title + ' < ' + this.range.upperBound;
        }
    }
}
