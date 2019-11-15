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
import { FormBuilder, FormGroup } from "@angular/forms";
import { BreadcrumbsService } from "../breadcrumbs/breadcrumbs.service";
import { MrDatasetNature } from "../datasets/dataset/mr/dataset.mr.model";
import { Dataset } from "../datasets/shared/dataset.model";
import { DatasetService } from "../datasets/shared/dataset.service";
import { Page, Pageable } from "../shared/components/table/pageable.model";
import { TableComponent } from "../shared/components/table/table.component";
import { DatasetModalityType } from "../shared/enums/dataset-modality-type";
import { Study } from "../studies/shared/study.model";
import { StudyService } from "../studies/shared/study.service";
import { Subject } from "../subjects/shared/subject.model";
import { SubjectService } from "../subjects/shared/subject.service";

@Component({
    selector: 'solr-search',
    templateUrl: 'solr.search.component.html',
    styleUrls: ['solr.search.component.css']
})

export class SolrSearchComponent{
    subjects: Subject[] = [];
    studies: Study[] = [];
    columnDefs: any[];
    form: FormGroup;
    formBuilder: FormBuilder;
    allMrDatasetNatures: any[];
    allDatasetModalityTypes: any[];
    selectedDatasetModalityTypes: any[] = [];
    selectedMrDatasetNatures: any[] = [];
    @ViewChild('table') table: TableComponent;

    // selectedAnimals = { Dog: false, Cat: false, Elephant: false };
    // animals = ['Dog', 'Cat', 'Elephant'];

    constructor(
            private datasetService: DatasetService,
            private studyService: StudyService,
            private subjectService: SubjectService,
            private breadcrumbsService: BreadcrumbsService) {
        
        // this.form = this.buildForm();
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('Solr Search'); 
        this.columnDefs = this.getColumnDefs();       
        this.allMrDatasetNatures = MrDatasetNature.getValueLabelJsonArray();
        this.allDatasetModalityTypes = DatasetModalityType.getValueLabelJsonArray(); 
        this.fetchStudies();
        this.fetchSubjects();
    }

    // buildForm(): FormGroup {
    //     let formGroup = this.formBuilder.group({
    //         'study': new FormControl(),
    //         'subject': new FormControl(),
    //         'datasetName': new FormControl(),
    //         'examinationComment': new FormControl(),
    //         'startDate': new FormControl(),
    //         'endDate': new FormControl()
    //     });
    //     return formGroup;
    // }
    
    private fetchSubjects() {
        this.subjectService.getAll().then(subjects => {
            this.subjects = subjects;
        });
    }
    
    private fetchStudies() {
        this.studyService.getAll().then(studies => {
            this.studies = studies;
        });
    }
    
    private getSubjectName(id: number): string {
        if (!this.subjects || this.subjects.length == 0 || !id) return id ? id+'' : '';
        for (let subject of this.subjects) { 
            if (subject.id == id) return subject.name;
        }
        throw new Error('Cannot find subject for id = ' + id);
    }
    
    private getStudyName(id: number): string {
        if (!this.studies || this.studies.length == 0 || !id) return id+'';
        for (let study of this.studies) {
            if (study.id == id) return study.name;
        }
        throw new Error('Cannot find study for id = ' + id);
    }

    getPage(pageable: Pageable): Promise<Page<Dataset>> {
        return this.datasetService.getPage(pageable);
    }
    
    // Grid columns definition
    getColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        return [
            {headerName: "Id", field: "id", type: "number", width: "30px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"]},
            {headerName: "Type", field: "type", width: "50px", suppressSorting: true},
            {headerName: "Subject", field: "subjectId", cellRenderer: (params: any) => this.getSubjectName(params.data.subjectId)},
            {headerName: "Study", field: "studyId", cellRenderer: (params: any) => this.getStudyName(params.data.studyId)},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: (params: any) => dateRenderer(params.data.creationDate)},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    private search() {
        
    }
}