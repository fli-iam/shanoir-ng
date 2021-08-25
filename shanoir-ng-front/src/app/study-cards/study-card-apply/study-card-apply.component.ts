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
import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { Dataset } from '../../datasets/shared/dataset.model';
import { DatasetService } from '../../datasets/shared/dataset.service';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { StudyCard } from '../shared/study-card.model';
import { StudyCardService } from '../shared/study-card.service';

@Component({
    selector: 'study-card-apply',
    templateUrl: 'study-card-apply.component.html',
    styleUrls: ['study-card-apply.component.css']
})
export class StudyCardApplyComponent {

    studycard: StudyCard;
    private loadedPromise: Promise<void>;
    private browserPaging: BrowserPaging<Dataset>;
    private datasets: Dataset[];
    columnsDefs: any = this.getColumnDefs();
    selected: Set<number> = new Set();


    constructor(
            private datasetService: DatasetService,
            private studycardService: StudyCardService,
            private activatedRoute: ActivatedRoute,
            breadcrumbsService: BreadcrumbsService,
            private confirmService: ConfirmDialogService) {
        breadcrumbsService.nameStep('Reapply Study Card');
        this.loadDatasets();
        this.loadStudyCard();
    }

    private loadDatasets(): Promise<void>  {
        const studycardId: number = +this.activatedRoute.snapshot.params['id'];
        this.loadedPromise = this.datasetService.getByStudycardId(studycardId).then((datasets) => {
            this.datasets = datasets;
            this.browserPaging = new BrowserPaging(datasets, this.columnsDefs);
        });
        return this.loadedPromise;
    }

    private loadStudyCard() {
        const studycardId: number = +this.activatedRoute.snapshot.params['id'];
        this.studycardService.get(studycardId).then(sc => this.studycard = sc);
    }

    getPage(pageable: FilterablePageable, forceRefresh: boolean = false): Promise<Page<Dataset>> {
        return this.loadedPromise.then(() => {
            if (forceRefresh) {
                return this.loadDatasets().then(() => this.browserPaging.getPage(pageable));
            } else {
                return this.browserPaging.getPage(pageable);
            }
        });
    }

    getColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        return [
            {headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"]},
            {headerName: "Type", field: "type", width: "50px", suppressSorting: true},
            {headerName: "Subject", field: "subject.name"},
            {headerName: "Study", field: "study.name"},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.creationDate)},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
    }

    private getAllDatasetsIds(): number[] {
        return this.datasets.map(ds => ds.id);
    }

    reapplyOnAll() {
        this.reapplyOn(this.getAllDatasetsIds());
    }

    reapplyOnSelected() {
        this.reapplyOn([...this.selected]);
    }

    private reapplyOn(datasetIds: number[]) {
        this.confirmService.confirm('Apply Study Card ?',
                'Would you like to apply the study card "' 
                + this.studycard.name
                + '" to ' + datasetIds.length
                + ' datasets? Note that any previous study card application will be permanentely overwriten by new values.'
        ).then(res => {
            if (res) {
                this.studycardService.applyStudyCardOn(this.studycard.id, datasetIds);
            }
        });
    }

}
