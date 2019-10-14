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

import { Component,  Input, ViewChild, OnChanges } from '@angular/core';
import { DoCheck, KeyValueDiffers, KeyValueDiffer } from '@angular/core';
import { FormGroup,  Validators, FormControl } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';

import { PreclinicalSubject } from '../shared/preclinicalSubject.model';
import { AnimalSubject } from '../shared/animalSubject.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { AnimalSubjectService } from '../shared/animalSubject.service';
import { Reference }   from '../../reference/shared/reference.model';
import { ReferenceService } from '../../reference/shared/reference.service';
import { PathologyService } from '../../pathologies/pathology/shared/pathology.service';
import { SubjectPathologyService } from '../../pathologies/subjectPathology/shared/subjectPathology.service';
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectTherapyService } from '../../therapies/subjectTherapy/shared/subjectTherapy.service';
import { ImagedObjectCategory } from '../../../subjects/shared/imaged-object-category.enum';
import { ModesAware } from "../../shared/mode/mode.decorator";
import { Study } from '../../../studies/shared/study.model';
import { IdName } from '../../../shared/models/id-name.model';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { preventInitialChildAnimations, slideDown } from '../../../shared/animations/animations';
import * as AppUtils from '../../../utils/app.utils';
import * as shajs from 'sha.js';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { BrowserPaging } from '../../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../../shared/components/table/pageable.model';
import { TableComponent } from '../../../shared/components/table/table.component';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';
import { TherapyType } from '../../shared/enum/therapyType';
import { Frequency } from '../../shared/enum/frequency';
import { MsgBoxService } from '../../../shared/msg-box/msg-box.service';


@Component({
    selector: 'animalSubject-form',
    templateUrl: 'animalSubject-form.component.html',
    styleUrls: ['../../../subjects/subject/subject.component.css', 'animalSubject-form.component.css'],
    providers: [AnimalSubjectService, ReferenceService, PathologyService, SubjectPathologyService, SubjectTherapyService],
    animations: [slideDown, preventInitialChildAnimations]
})
    
@ModesAware
export class AnimalSubjectFormComponent extends EntityComponent<PreclinicalSubject> {

    @ViewChild('subjectPathologiesTable') tablePathology: TableComponent; 
    @ViewChild('subjectTherapiesTable') tableTherapy: TableComponent; 

    private readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    private studies: IdName[];
    private nameValidators = [Validators.required, Validators.minLength(2), Validators.maxLength(64)];
    species: Reference[] = [];
    strains: Reference[] = [];
    biotypes: Reference[] = [];
    providers: Reference[] = [];
    stabulations: Reference[] = [];
    references: Reference[] = [];

    @Input() preFillData: Subject;
    @Input() displayPathologyTherapy: boolean = true;
    private subjectStudyList: SubjectStudy[] = [];
    private selectedStudy : IdName;
    private hasNameUniqueError: boolean = false;


    public toggleFormSP: boolean = false;
    public createSPMode: boolean = false;
    public pathoSelected: SubjectPathology;
    private browserPagingPathology: BrowserPaging<SubjectPathology>;
    private columnDefsPathologies: any[];
    private subjectPathologiesPromise: Promise<any>;
    pathologiesToDelete: SubjectPathology[] = [];
    pathologiesToCreate: SubjectPathology[] = [];

    public toggleFormST: boolean = false;
    public createSTMode: boolean = false;
    public therapySelected: SubjectTherapy;
    private browserPagingTherapy: BrowserPaging<SubjectTherapy>;
    private columnDefsTherapies: any[];
    private subjectTherapiesPromise: Promise<any>;
    therapiesToDelete: SubjectTherapy[] = [];
    therapiesToCreate: SubjectTherapy[] = [];

    differ: KeyValueDiffer<string, any>;


    constructor(private route: ActivatedRoute,
            private animalSubjectService: AnimalSubjectService,
            private studyService: StudyService, 
            private referenceService: ReferenceService,
            private subjectPathologyService: SubjectPathologyService,
            private subjectTherapyService: SubjectTherapyService,
            private differs: KeyValueDiffers) {

        super(route, 'preclinical-subject');
        this.differ = this.differs.find({}).create();

    }

    public get preclinicalSubject(): PreclinicalSubject { return this.entity; }
    public set preclinicalSubject(preclinicalSubject: PreclinicalSubject) { this.entity = preclinicalSubject; }

    initView(): Promise<void> {
        return new  Promise<void>(resolve => {
            this.createColumnDefsPathologies();
            this.subjectPathologiesPromise = Promise.resolve().then(() => {
                this.browserPagingPathology = new BrowserPaging([], this.columnDefsPathologies);
            });
            this.createColumnDefsTherapies();
            this.subjectTherapiesPromise = Promise.resolve().then(() => {
                this.browserPagingTherapy = new BrowserPaging([], this.columnDefsTherapies);
            });
            this.preclinicalSubject = new PreclinicalSubject();
            this.preclinicalSubject.subject = new Subject();
            this.preclinicalSubject.animalSubject = new AnimalSubject();
            this.preclinicalSubject.animalSubject.id = this.id;
            this.animalSubjectService.getAnimalSubject(this.id).then(animalSubject => {
                this.animalSubjectService.getSubject(animalSubject.subjectId).then((subject) => {
                    this.preclinicalSubject.animalSubject = animalSubject;
                    this.preclinicalSubject.subject = subject;
                    // subjectStudy
                    if (this.preclinicalSubject.subject.subjectStudyList && this.preclinicalSubject.subject.subjectStudyList.length > 0){
                        this.subjectStudyList = [];
                        for (let study of this.preclinicalSubject.subject.subjectStudyList) {
                            let newSubjectStudy: SubjectStudy = this.getSubjectStudy(study);
                            this.subjectStudyList.push(newSubjectStudy);
                        }
                        this.preclinicalSubject.subject.subjectStudyList = this.subjectStudyList;
                    }
                    //
                    this.subjectTherapyService.getSubjectTherapies(this.preclinicalSubject).then(st => {
                        this.preclinicalSubject.therapies = st;
                        this.browserPagingTherapy.setItems(st);
                        this.tableTherapy.refresh();
                    });
                    this.subjectPathologyService.getSubjectPathologies(this.preclinicalSubject).then(sp => {
                        this.preclinicalSubject.pathologies = sp;
                        this.browserPagingPathology.setItems(sp);
                        this.tablePathology.refresh();
                    });
                    resolve();
                });
            });
        });
    }

    initEdit(): Promise<void> {
        this.loadData();
        this.createColumnDefsPathologies();
        this.subjectPathologiesPromise = Promise.resolve().then(() => {
            this.browserPagingPathology = new BrowserPaging([], this.columnDefsPathologies);
        });
        this.createColumnDefsTherapies();
        this.subjectTherapiesPromise = Promise.resolve().then(() => {
            this.browserPagingTherapy = new BrowserPaging([], this.columnDefsTherapies);
        });
        return new  Promise<void>(resolve => {
            this.preclinicalSubject = new PreclinicalSubject();
            this.preclinicalSubject.subject = new Subject();
            this.preclinicalSubject.animalSubject = new AnimalSubject();
            this.preclinicalSubject.animalSubject.id = this.id;
            this.animalSubjectService.getAnimalSubject(this.id).then(animalSubject => {
                animalSubject.specie = this.getReferenceById(animalSubject.specie);
                animalSubject.strain = this.getReferenceById(animalSubject.strain);
                animalSubject.biotype = this.getReferenceById(animalSubject.biotype);
                animalSubject.provider = this.getReferenceById(animalSubject.provider);
                animalSubject.stabulation = this.getReferenceById(animalSubject.stabulation);
                this.animalSubjectService.getSubject(animalSubject.subjectId).then((subject) => {
                    this.preclinicalSubject.animalSubject = animalSubject;
                    this.preclinicalSubject.subject = subject;
                    // subjectStudy
                    if (this.preclinicalSubject.subject.subjectStudyList && this.preclinicalSubject.subject.subjectStudyList.length > 0){
                        this.subjectStudyList = [];
                        for (let study of this.preclinicalSubject.subject.subjectStudyList) {
                            let newSubjectStudy: SubjectStudy = this.getSubjectStudy(study);
                            this.subjectStudyList.push(newSubjectStudy);
                        }
                        this.preclinicalSubject.subject.subjectStudyList = this.subjectStudyList;
                    }
                    //
                    this.subjectTherapyService.getSubjectTherapies(this.preclinicalSubject).then(st => {
                        this.preclinicalSubject.therapies = st;
                        this.browserPagingTherapy.setItems(st);
                        this.tableTherapy.refresh();
                    });
                    this.subjectPathologyService.getSubjectPathologies(this.preclinicalSubject).then(sp => {
                        this.preclinicalSubject.pathologies = sp;
                        this.browserPagingPathology.setItems(sp);
                        this.tablePathology.refresh();
                    });
                    resolve();
                });
            });
        });
    }

    initCreate(): Promise<void> {
        return new  Promise<void>(resolve => {
            this.createColumnDefsPathologies();
            this.subjectPathologiesPromise = Promise.resolve().then(() => {
                this.browserPagingPathology = new BrowserPaging([], this.columnDefsPathologies);
            });
            this.createColumnDefsTherapies();
            this.subjectTherapiesPromise = Promise.resolve().then(() => {
                this.browserPagingTherapy = new BrowserPaging([], this.columnDefsTherapies);
            });
            this.loadData();
            this.preclinicalSubject = new PreclinicalSubject();
            this.preclinicalSubject.subject = new Subject();
            this.preclinicalSubject.animalSubject = new AnimalSubject();
            this.preclinicalSubject.subject.preclinical = true;
            this.preclinicalSubject.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_ANIMAL;
            resolve();
        });
    }

    loadData() {
        this.referenceService.getReferencesByCategory(PreclinicalUtils.PRECLINICAL_CAT_SUBJECT).then(references => {
            this.references = references;
            this.sortReferences();
        });
        this.loadAllStudies();
    }
    
    loadAllStudies(): void {
        this.studyService.getStudiesNames()
            .then(studies => {
                this.studies = studies;
                this.updateStudiesList();
            })
            .catch((error) => {
                // TODO: display error
                console.error("error getting study list!");
        });
    }

    
    getSubjectStudy(subjectStudy: SubjectStudy): SubjectStudy{
    	let fixedSubjectStudy = new SubjectStudy();
    	fixedSubjectStudy.id = subjectStudy.id;
    	fixedSubjectStudy.subjectStudyIdentifier = subjectStudy.subjectStudyIdentifier;
    	fixedSubjectStudy.subjectType = subjectStudy.subjectType;
    	fixedSubjectStudy.physicallyInvolved = subjectStudy.physicallyInvolved;
    	fixedSubjectStudy.subject = this.getSubject();
    	fixedSubjectStudy.study = subjectStudy.study;
    	fixedSubjectStudy.subjectId = this.preclinicalSubject.subject.id;
    	fixedSubjectStudy.studyId = subjectStudy.study.id;
    	return fixedSubjectStudy;
    }
    
    getStudyById(id: number): Study{
    	if (this.studies && this.studies.length > 0){
    		for (let s of this.studies){
    			if (s.id === id){
    				let study: Study = new Study();
    				study.id = s.id;
    				study.name = s.name;
    				return study;
    			}
    		}
    	}
    	return null;
    }

	getSubject(): Subject{
		let subject = new Subject();
		subject.id = this.preclinicalSubject.subject.id;
		subject.name = this.preclinicalSubject.subject.name;
		return subject;
	}
	
     displaySex(): boolean {
        if (this.animalSelected()) {
        	return true;
        } else {
            return false;
        }
    }
    
    public animalSelected(): boolean {
        return this.preclinicalSubject && this.preclinicalSubject.subject && this.preclinicalSubject.subject.imagedObjectCategory != null
            && (this.preclinicalSubject.subject.imagedObjectCategory.toString() != "PHANTOM"
                && this.preclinicalSubject.subject.imagedObjectCategory.toString() != "ANATOMICAL_PIECE");
    }

    buildForm(): FormGroup {
        let sexFC : FormControl;
        if (this.animalSelected()) {
            sexFC = new FormControl(this.preclinicalSubject.subject.sex, [Validators.required]);
        } else {
            sexFC = new FormControl(this.preclinicalSubject.subject.sex);
        }
        let subjectForm = this.formBuilder.group({
            'imagedObjectCategory': [this.preclinicalSubject.subject.imagedObjectCategory, [Validators.required]],
            'isAlreadyAnonymized': [],
            'name': [this.preclinicalSubject.subject.name, this.nameValidators.concat([this.registerOnSubmitValidator('unique', 'name')])],
            'specie': [this.preclinicalSubject.animalSubject.specie, [Validators.required]],
            'strain': [this.preclinicalSubject.animalSubject.strain, [Validators.required]],
            'biotype': [this.preclinicalSubject.animalSubject.biotype, [Validators.required]],
            'provider': [this.preclinicalSubject.animalSubject.provider, [Validators.required]],
            'stabulation': [this.preclinicalSubject.animalSubject.stabulation, [Validators.required]],
            'sex': sexFC,
            'subjectStudyList': []
        });
        return subjectForm;

    }


    
    onChangeImagedObjectCategory(){
    	if (!this.animalSelected()){
        	this.setSex();
        }
        this.buildForm();
    }

    //params should be category and then reftype
    goToRefPage(...params: string[]): void {
        let category;
        let reftype;
        if (params && params[0]) category = params[0];
        if (params && params[1]) reftype = params[1];
        if (category && !reftype) this.router.navigate(['/preclinical-reference/create'], { queryParams: { category: category } });
        if (category && reftype) this.router.navigate(['/preclinical-reference/create'], { queryParams: {category: category, reftype: reftype } });
    }

    goToEdit(id?: number): void {
        super.goToEdit(this.preclinicalSubject.animalSubject.id);
    }

    protected save(): Promise<void> {
        return new  Promise<void>(resolve => {
            if (this.preclinicalSubject.animalSubject.id){
                this.updateSubject().then(() => {
                    this.onSave.next(this.preclinicalSubject);
                    this.chooseRouteAfterSave(this.entity);
                    this.msgBoxService.log('info', 'The preclinical-subject n°' + this.preclinicalSubject.animalSubject.id + ' has been successfully updated');
                });
            }else{
                this.addSubject().then( () => {
                    this.onSave.next(this.preclinicalSubject);
                    this.chooseRouteAfterSave(this.entity);
                    this.msgBoxService.log('info', 'The new preclinical-subject has been successfully saved under the number ' + this.preclinicalSubject.animalSubject.id);
                });
                
            }
            resolve();
        });
    }

    protected chooseRouteAfterSave(entity: PreclinicalSubject) {
        this.breadcrumbsService.currentStep.notifySave(entity);
        if (this.breadcrumbsService.previousStep && this.breadcrumbsService.previousStep.isWaitingFor(this.breadcrumbsService.currentStep)) {
            this.breadcrumbsService.goBack();
        }
        else {
            this.goToView(entity.animalSubject.id);
        }
    }

    addSubject(): Promise<void> {
        if (!this.preclinicalSubject ) { 
            return Promise.resolve();
        }
        return new  Promise<void>(resolve => {
            this.preclinicalSubject.subject.identifier = this.generateSubjectIdentifier();
            if (!this.animalSelected()){
                this.setSex();
            }
            Promise.resolve(this.animalSubjectService.createSubject(this.preclinicalSubject.subject))
            .then((subject) => {
                this.preclinicalSubject.subject = subject;
                this.preclinicalSubject.animalSubject.subjectId = subject.id;
                this.animalSubjectService.createAnimalSubject(this.preclinicalSubject.animalSubject)
                .then((animalSubject) => {
                    this.preclinicalSubject.id = animalSubject.id;
                    this.preclinicalSubject.animalSubject = animalSubject;
                    //Then add pathologies
                    if (this.preclinicalSubject && this.preclinicalSubject.pathologies) {
                        for (let patho of this.preclinicalSubject.pathologies) {
                            //patho.subject = subject;
                            this.subjectPathologyService.createSubjectPathology(this.preclinicalSubject, patho);
                        }
                    }
                    //Then add therapies
                    if (this.preclinicalSubject && this.preclinicalSubject.therapies) {
                        for (let therapy of this.preclinicalSubject.therapies) {
                            this.subjectTherapyService.createSubjectTherapy(this.preclinicalSubject, therapy);
                        }
                    }
                    resolve();
                });
            });
        });
    }

    updateSubject(): Promise<void> {
        return new  Promise<void>(resolve => {
            if (this.preclinicalSubject && this.preclinicalSubject.subject){	
                this.generateSubjectIdentifier();
                this.preclinicalSubject.subject.subjectStudyList = this.subjectStudyList;
                this.animalSubjectService.updateSubject(this.preclinicalSubject.subject.id, this.preclinicalSubject.subject)
                    .then(subject => {
                        if (this.preclinicalSubject.animalSubject){
                             this.animalSubjectService.updateAnimalSubject(this.preclinicalSubject.animalSubject);
                        }
                        if (this.therapiesToDelete) {
                            for (let therapy of this.therapiesToDelete) {
                                this.subjectTherapyService.deleteSubjectTherapy(this.preclinicalSubject, therapy);
                            }
                        }
                        if (this.therapiesToCreate) {
                            for (let therapy of this.therapiesToCreate) {
                                this.subjectTherapyService.createSubjectTherapy(this.preclinicalSubject, therapy);
                            }
                        }
                        if (this.pathologiesToDelete) {
                            for (let pathology of this.pathologiesToDelete) {
                                this.subjectPathologyService.deleteSubjectPathology(this.preclinicalSubject, pathology);
                            }
                        }
                        if (this.pathologiesToCreate) {
                            for (let pathology of this.pathologiesToCreate) {
                                this.subjectPathologyService.createSubjectPathology(this.preclinicalSubject, pathology);
                            }
                        }
                        resolve();
                    }, (error: any) => {
                        this.manageRequestErrors(error);
                    }
                );
            }
        });
    }
    
    setSex(): void {
    	this.preclinicalSubject.subject.sex = 'M';
    }

    sortReferences() {
    if (this.references){
        for (let ref of this.references) {
            switch (ref.reftype) {
                case PreclinicalUtils.PRECLINICAL_SUBJECT_SPECIE:
                    this.species.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_BIOTYPE:
                    this.biotypes.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_STRAIN:
                    this.strains.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_PROVIDER:
                    this.providers.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_STABULATION:
                    this.stabulations.push(ref);
                    break;
                default:
                    break;
            }
        }
        }
    }

    getReferenceById(reference: any): Reference {
        if (reference) {
            for (let ref of this.references) {
                if (reference.id == ref.id) {
                    return ref;
                }
            }
        }
        return null;
    }
    
    
    
    onStudySelect() {
        this.selectedStudy.selected = true;
        let newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        newSubjectStudy.study = new Study();
        newSubjectStudy.study.id = this.selectedStudy.id;
        newSubjectStudy.study.name = this.selectedStudy.name;
        this.subjectStudyList.push(newSubjectStudy);
        this.preclinicalSubject.subject.subjectStudyList = this.subjectStudyList;
    }
    
    
    removeSubjectStudy(subjectStudy: SubjectStudy):void {
        for (let study of this.studies) {
            if (subjectStudy.study.id == study.id) study.selected = false;
        }
        const index: number = this.subjectStudyList.indexOf(subjectStudy);
        if (index !== -1) {
            this.subjectStudyList.splice(index, 1);
        }
    }
    
    
    generateSubjectIdentifier(): string {
        let hash;
        if (this.preclinicalSubject && this.preclinicalSubject.subject) {
            hash = this.preclinicalSubject.subject.name
        }
        return this.getHash(hash);
    }

    getHash(stringToBeHashed: string): string {
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = "";
        hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
    }
    
    
    private manageRequestErrors(error: any): void {
        this.hasNameUniqueError = AppUtils.hasUniqueError(error, 'name');
    }

    getPagePathology(pageable: FilterablePageable): Promise<Page<SubjectPathology>> {
        return new Promise((resolve) => {
            this.subjectPathologiesPromise.then(() => {
                resolve(this.browserPagingPathology.getPage(pageable));
            });
        });
    }

    
    private createColumnDefsPathologies() {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        function checkNullValueReference(reference: any) {
            if (reference) {
                return reference.value;
            }
            return '';
        };

        this.columnDefsPathologies = [
            { headerName: "Pathology", field: "pathology.name" },
            { headerName: "PathologyModel", field: "pathologyModel.name" },
            {
                headerName: "Location", field: "location.value", type: "reference", cellRenderer: function(params: any) {
                    return checkNullValueReference(params.data.location);
                }
            },
            {
                headerName: "Start Date", field: "startDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End Date", field: "endDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.endDate);
                }
            },
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.columnDefsPathologies.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.editSubjectPathology(item) });
        }
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.columnDefsPathologies.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeSubjectPathology(item) });
        }
    }

    goToAddPathology(){
        this.pathoSelected = new SubjectPathology();
        this.createSPMode = true;
        if(this.toggleFormSP==false){
            this.toggleFormSP = true;
        }else if(this.toggleFormSP==true){
            this.toggleFormSP = false;
        }else{
            this.toggleFormSP = true;
        }
    }

    private editSubjectPathology = (item: SubjectPathology) => {
        this.pathoSelected = item;
        this.toggleFormSP = true;
        this.createSPMode = false;
    }

    private removeSubjectPathology = (item: SubjectPathology) => {
        const index: number = this.preclinicalSubject.pathologies.indexOf(item);
        if (index !== -1) {
            this.preclinicalSubject.pathologies.splice(index, 1);
        }
        this.pathologiesToDelete.push(item);
        this.browserPagingPathology.setItems(this.preclinicalSubject.pathologies);
        this.tablePathology.refresh();
    }

    refreshDisplayPathology(subjectPathology: SubjectPathology){
        this.toggleFormSP = false;
        this.createSPMode = false;
        if (subjectPathology && subjectPathology != null && !subjectPathology.id ){
            this.pathologiesToCreate.push(subjectPathology);
        }
        this.browserPagingPathology.setItems(this.preclinicalSubject.pathologies);
        this.tablePathology.refresh();
    }
    

    getPageTherapy(pageable: FilterablePageable): Promise<Page<SubjectTherapy>> {
        return new Promise((resolve) => {
            this.subjectTherapiesPromise.then(() => {
                resolve(this.browserPagingTherapy.getPage(pageable));
            });
        });
    }


    private createColumnDefsTherapies() {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        function checkNullValueReference(reference: any) {
            if (reference) {
                return reference.value;
            }
            return '';
        };
        function checkNullValue(value: any) {
            if (value) {
                return value;
            }
            return '';
        };

        this.columnDefsTherapies = [
            { headerName: "Therapy", field: "therapy.name" },
            {
                headerName: "Type", field: "therapy.therapyType", type: "Enum", cellRenderer: function(params: any) {
                    return TherapyType[params.data.therapy.therapyType];
                }
            },
            {
                headerName: "Dose", field: "dose", type: "dose", cellRenderer: function(params: any) {
                    return checkNullValue(params.data.dose);
                }
            },
            {
                headerName: "Dose Unit", field: "dose_unit.value", type: "reference", cellRenderer: function(params: any) {
                    return checkNullValueReference(params.data.dose_unit);
                }
            },
            {
                headerName: "Type", field: "frequency", type: "Enum", cellRenderer: function (params: any) {
                    return Frequency[params.data.frequency];
                }
            },
            {
                headerName: "Start Date", field: "startDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End Date", field: "endDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.endDate);
                }
            }     
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.columnDefsTherapies.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.editSubjectTherapy(item) });
        }
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.columnDefsTherapies.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeSubjectTherapy(item) });
        }
    }

    goToAddTherapy(){
        this.therapySelected = new SubjectTherapy();
        this.createSTMode = true;
        if(this.toggleFormST==false){
            this.toggleFormST = true;
        }else if(this.toggleFormST==true){
            this.toggleFormST = false;
        }else{
            this.toggleFormST = true;
        }
    }

    private editSubjectTherapy = (item: SubjectTherapy) => {
        this.therapySelected = item;
        this.toggleFormST = true;
        this.createSTMode = false;
    }

    private removeSubjectTherapy = (item: SubjectTherapy) => {
        const index: number = this.preclinicalSubject.therapies.indexOf(item);
        if (index !== -1) {
            this.preclinicalSubject.therapies.splice(index, 1);
        }
        this.therapiesToDelete.push(item);
        this.browserPagingTherapy.setItems(this.preclinicalSubject.therapies);
        this.tableTherapy.refresh();
    }

    refreshDisplayTherapy(subjectTherapy: SubjectTherapy){
        this.toggleFormST = false;
        this.createSTMode = false;
        if (subjectTherapy && subjectTherapy != null && !subjectTherapy.id ){
            this.therapiesToCreate.push(subjectTherapy);
        }
        this.browserPagingTherapy.setItems(this.preclinicalSubject.therapies);
        this.tableTherapy.refresh();
    }

    ngDoCheck() {
        const change = this.differ.diff(this);
        if (change) {
          change.forEachChangedItem(item => {
            if(item.key=="entity"){
                this.updateStudiesList();
            }
          });
        }
    }

    updateStudiesList(){
        if (this.preclinicalSubject && this.preclinicalSubject.subject && this.preclinicalSubject.subject.subjectStudyList && this.preclinicalSubject.subject.subjectStudyList.length > 0){
            for(let st of this.preclinicalSubject.subject.subjectStudyList){
                if (this.studies && this.studies.length > 0){
                    for (let s of this.studies){
                        if (s.id ==st.study.id){
                            s.selected = true;
                        }
                    }
                }
            }
        }
    }



}