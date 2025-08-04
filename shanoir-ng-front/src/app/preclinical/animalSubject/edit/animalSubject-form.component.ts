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
import { Component, Input, KeyValueDiffer, KeyValueDiffers, ViewChild } from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import * as shajs from 'sha.js';

import { preventInitialChildAnimations, slideDown } from '../../../shared/animations/animations';
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { TableComponent } from '../../../shared/components/table/table.component';
import { IdName } from '../../../shared/models/id-name.model';
import { Option } from '../../../shared/select/select.component';
import { Study } from '../../../studies/shared/study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { ImagedObjectCategory } from '../../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from '../../../subjects/shared/subject.service';
import { ReverseSubjectNode } from '../../../tree/tree.model';
import { PathologyService } from '../../pathologies/pathology/shared/pathology.service';
import { SubjectPathologiesListComponent } from '../../pathologies/subjectPathology/list/subjectPathology-list.component';
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectPathologyService } from '../../pathologies/subjectPathology/shared/subjectPathology.service';
import { Reference } from '../../reference/shared/reference.model';
import { ReferenceService } from '../../reference/shared/reference.service';
import { ModesAware } from '../../shared/mode/mode.decorator';
import { SubjectTherapiesListComponent } from '../../therapies/subjectTherapy/list/subjectTherapy-list.component';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';
import { SubjectTherapyService } from '../../therapies/subjectTherapy/shared/subjectTherapy.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { AnimalSubject } from '../shared/animalSubject.model';
import { AnimalSubjectService } from '../shared/animalSubject.service';
import { PreclinicalSubject } from '../shared/preclinicalSubject.model';
import { Selection } from 'src/app/studies/study/tree.service';
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';
import { StudyRightsService } from 'src/app/studies/shared/study-rights.service';
import { TaskState } from 'src/app/async-tasks/task.model';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';


@Component({
    selector: 'animalSubject-form',
    templateUrl: 'animalSubject-form.component.html',
    styleUrls: ['../../../subjects/subject/subject.component.css', 'animalSubject-form.component.css'],
    animations: [slideDown, preventInitialChildAnimations],
    standalone: false
})

@ModesAware
export class AnimalSubjectFormComponent extends EntityComponent<PreclinicalSubject> {

    @ViewChild('subjectPathologiesTable', { static: false }) tablePathology: TableComponent;

    public readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    public studies: IdName[];
    private nameValidators = [Validators.required, Validators.minLength(2), Validators.maxLength(64)];
    species: Reference[] = [];
    strains: Reference[] = [];
    biotypes: Reference[] = [];
    providers: Reference[] = [];
    stabulations: Reference[] = [];
    references: Reference[] = [];
    hasDownloadRight: boolean = false;
    downloadState: TaskState = new TaskState();

    @Input() preFillData: Subject;
    @Input() displayPathologyTherapy: boolean = true;
    @ViewChild('therapiesComponent', { static: false }) therapiesComponent: SubjectTherapiesListComponent;
    @ViewChild('pathologiesComponent', { static: false }) pathologiesComponent: SubjectPathologiesListComponent;
    private subjectStudyList: SubjectStudy[] = [];
    private therapies: SubjectTherapy[] = [];
    private pathologies: SubjectPathology[] = [];
    private selectedStudy : IdName;
    differ: KeyValueDiffer<string, any>;

    catOptions: Option<ImagedObjectCategory>[] = [
        new Option<ImagedObjectCategory>(ImagedObjectCategory.LIVING_ANIMAL, 'Living animal'),
        new Option<ImagedObjectCategory>(ImagedObjectCategory.ANIMAL_CADAVER, 'Animal cadaver'),
        new Option<ImagedObjectCategory>(ImagedObjectCategory.PHANTOM, 'Phantom'),
        new Option<ImagedObjectCategory>(ImagedObjectCategory.ANATOMICAL_PIECE, 'Anatomical piece')
    ];

    genderOptions: Option<string>[] = [
        new Option<string>('F', 'Female'),
        new Option<string>('M', 'Male'),
    ];

    constructor(private route: ActivatedRoute,
            private animalSubjectService: AnimalSubjectService,
            private subjectService: SubjectService,
            private studyService: StudyService,
            private referenceService: ReferenceService,
            private subjectPathologyService: SubjectPathologyService,
            private subjectTherapyService: SubjectTherapyService,
            private differs: KeyValueDiffers,
            private studyRightsService: StudyRightsService,
            private downloadService: MassDownloadService) {

        super(route, 'preclinical-subject');
        this.differ = this.differs.find({}).create();

    }

    public GetModes(): any { return (<any>this).Modes; }

    public get preclinicalSubject(): PreclinicalSubject { return this.entity; }
    public set preclinicalSubject(preclinicalSubject: PreclinicalSubject) { this.entity = preclinicalSubject; }

    getService(): EntityService<PreclinicalSubject> {
        return this.animalSubjectService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromPreclinicalSubject(this.entity);
    }

    // private addToCache(key: string, toBeCached: any) {
    //     if (!this.breadcrumbsService.currentStep.isPrefilled(key))	{
    //         this.breadcrumbsService.currentStep.addPrefilled(key, []);
    //     }
    //     this.breadcrumbsService.currentStep.getPrefilledValue(key).push(toBeCached);
    // }

    // private getCache(key: string) {
    //     if (!this.breadcrumbsService.currentStep.isPrefilled(key))  {
    //        this.breadcrumbsService.currentStep.addPrefilled(key, []);
    //     }
    //     return this.breadcrumbsService.currentStep.getPrefilledValue(key);
    // }

    protected fetchEntity: () => Promise<PreclinicalSubject> = () => {
        return this.idPromise.then(id => {
            return this.getService().get(id).then(ps => {
                return Promise.all([
                    this.animalSubjectService.getAnimalSubject(ps.id),
                    this.subjectService.get(ps.id)
                ]).then(([animalSubject, subject]) => {
                    ps.animalSubject = animalSubject;
                    ps.subject = subject;
                    // subjectStudy
                    if (ps.subject.subjectStudyList && ps.subject.subjectStudyList.length > 0){
                        this.subjectStudyList = [];
                        for (let ss of ps.subject.subjectStudyList) {
                            let newSubjectStudy: SubjectStudy = this.copySubjectStudy(ss);
                            this.subjectStudyList.push(newSubjectStudy);
                        }
                        ps.subject.subjectStudyList = this.subjectStudyList;
                    }
                    return ps;
                });
            });
        });
    }

    init() {
        super.init();
        if (this.mode == 'create') {
            this.breadcrumbsService.currentStep.getPrefilledValue("entity").then( res => this.entity = res);
        }

    }
    initView(): Promise<void> {
        this.loadAllStudies();
        if (this.keycloakService.isUserAdmin()) {
            this.hasDownloadRight = true;
            return;
        } else {
            this.treeService.studyPromise.then(study => {
                return this.studyRightsService.getMyRightsForStudy(study.id).then(rights => {
                    this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
                });
            });
        }


        return this.loadData().then(() => {
            this.subjectTherapyService.getSubjectTherapies(this.preclinicalSubject).then(st => {
                this.preclinicalSubject.therapies = st;
            });
            this.subjectPathologyService.getSubjectPathologies(this.preclinicalSubject).then(sp => {
                this.preclinicalSubject.pathologies = sp;
            });
        });

    }

    initEdit(): Promise<void> {
        return this.loadData().then(() => {
            this.preclinicalSubject.animalSubject.specie = this.getReferenceById(this.preclinicalSubject.animalSubject.specie);
            this.preclinicalSubject.animalSubject.strain = this.getReferenceById(this.preclinicalSubject.animalSubject.strain);
            this.preclinicalSubject.animalSubject.biotype = this.getReferenceById(this.preclinicalSubject.animalSubject.biotype);
            this.preclinicalSubject.animalSubject.provider = this.getReferenceById(this.preclinicalSubject.animalSubject.provider);
            this.preclinicalSubject.animalSubject.stabulation = this.getReferenceById(this.preclinicalSubject.animalSubject.stabulation);
        });
    }

    initCreate(): Promise<void> {
        return new  Promise<void>(resolve => {
            this.loadData();
            this.preclinicalSubject = new PreclinicalSubject();
            this.preclinicalSubject.animalSubject = new AnimalSubject();
            this.preclinicalSubject.subject = new Subject();
            this.preclinicalSubject.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_ANIMAL;
            resolve();
        });
    }

    loadData(): Promise<void> {
        return Promise.all([
            this.referenceService.getReferencesByCategory(PreclinicalUtils.PRECLINICAL_CAT_SUBJECT).then(references => {
                this.references = references;
                this.sortReferences();
            }),
            this.loadAllStudies()
        ]).then();
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

    copySubjectStudy(subjectStudy: SubjectStudy): SubjectStudy{
    	let fixedSubjectStudy = new SubjectStudy();
    	fixedSubjectStudy.id = subjectStudy.id;
    	fixedSubjectStudy.subjectStudyIdentifier = subjectStudy.subjectStudyIdentifier;
    	fixedSubjectStudy.subjectType = subjectStudy.subjectType;
    	fixedSubjectStudy.physicallyInvolved = subjectStudy.physicallyInvolved;
    	fixedSubjectStudy.subject = this.copySubject(subjectStudy.subject);
    	fixedSubjectStudy.study = subjectStudy.study;
    	fixedSubjectStudy.subjectId = subjectStudy.subject.id;
    	fixedSubjectStudy.studyId = subjectStudy.study.id;
        fixedSubjectStudy.tags = subjectStudy.tags;
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

	copySubject(s: Subject): Subject{
		let subject = new Subject();
		subject.id = s.id;
		subject.name = s.name;
		return subject;
	}

    public animalSelected(): boolean {
        return this.preclinicalSubject && this.preclinicalSubject.subject && this.preclinicalSubject.subject.imagedObjectCategory != null
            && (this.preclinicalSubject.subject.imagedObjectCategory.toString() != "PHANTOM"
                && this.preclinicalSubject.subject.imagedObjectCategory.toString() != "ANATOMICAL_PIECE");
    }

    buildForm(): UntypedFormGroup {
        let animal: boolean = this.animalSelected();
        let subjectForm = this.formBuilder.group({
            'imagedObjectCategory': [this.preclinicalSubject.subject.imagedObjectCategory, [Validators.required]],
            'isAlreadyAnonymized': [],
            'name': [this.preclinicalSubject.subject.name, this.nameValidators.concat([this.registerOnSubmitValidator('unique', 'name')])],
            'specie': [this.preclinicalSubject.animalSubject.specie, animal ? [Validators.required] : []],
            'strain': [this.preclinicalSubject.animalSubject.strain, animal ? [Validators.required] : []],
            'biotype': [this.preclinicalSubject.animalSubject.biotype, animal ? [Validators.required] : []],
            'provider': [this.preclinicalSubject.animalSubject.provider, animal ? [Validators.required] : []],
            'stabulation': [this.preclinicalSubject.animalSubject.stabulation, animal ? [Validators.required] : []],
            'sex': [this.preclinicalSubject.subject.sex, animal ? [Validators.required] : []],
            'therapies': [this.preclinicalSubject.therapies],
            'pathologies': [this.preclinicalSubject.pathologies],
            'subjectStudyList': []
        });
        this.subscriptions.push(
            subjectForm.get('imagedObjectCategory').valueChanges.subscribe(val => {
                this.onChangeImagedObjectCategory(subjectForm);
            })
        );
        return subjectForm;
    }

    onChangeImagedObjectCategory(formGroup: UntypedFormGroup){
        let newCategory: ImagedObjectCategory = formGroup.get('imagedObjectCategory').value;
        if (newCategory != 'PHANTOM' && newCategory != 'ANATOMICAL_PIECE' && this.mode != 'view') {
            formGroup.get('specie').setValidators([Validators.required]);
            formGroup.get('strain').setValidators([Validators.required]);
            formGroup.get('biotype').setValidators([Validators.required]);
            formGroup.get('provider').setValidators([Validators.required]);
            formGroup.get('stabulation').setValidators([Validators.required]);
            formGroup.get('sex').setValidators([Validators.required]);
        } else {
            formGroup.get('specie').setValidators([]);
            formGroup.get('strain').setValidators([]);
            formGroup.get('biotype').setValidators([]);
            formGroup.get('provider').setValidators([]);
            formGroup.get('stabulation').setValidators([]);
            formGroup.get('sex').setValidators([]);
        }
        formGroup.get('specie').updateValueAndValidity();
        formGroup.get('strain').updateValueAndValidity();
        formGroup.get('biotype').updateValueAndValidity();
        formGroup.get('provider').updateValueAndValidity();
        formGroup.get('stabulation').updateValueAndValidity();
        formGroup.get('sex').updateValueAndValidity();
        this.reloadRequiredStyles();
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
        super.goToEdit(this.preclinicalSubject.id);
    }

    public save(): Promise<PreclinicalSubject> {
        if (this.preclinicalSubject.animalSubject.id){
            return this.updateSubject().then(() => {
                this.onSave.next(this.preclinicalSubject);
                this.chooseRouteAfterSave(this.entity.animalSubject);
                this.consoleService.log('info', 'Preclinical subject n°' + this.preclinicalSubject.animalSubject.id + ' successfully updated');
                return this.entity;
            }).catch(reason => {
                this.footerState.loading = false;
                this.catchSavingErrors(reason);
                return null;
            });
        }else{
            return this.addSubject().then(subject => {
                if (subject == null) {
                    return;
                }
                this.onSave.next(this.preclinicalSubject);
                this.chooseRouteAfterSave(this.preclinicalSubject);
                this.consoleService.log('info', 'New preclinical subject successfully saved with n° ' + this.preclinicalSubject.animalSubject.id);
                return subject;
            }).catch(reason => {
                this.footerState.loading = false;
                this.catchSavingErrors(reason);
                return null;
            });
        }
    }

    addSubject(): Promise<PreclinicalSubject> {
        if (!this.preclinicalSubject ) {
            return Promise.resolve(null);
        }

        this.preclinicalSubject.subject.identifier = this.generateSubjectIdentifier();

        return this.animalSubjectService.createPreclinicalSubject(this.preclinicalSubject).then((preclinicalSubject) => {
            this.preclinicalSubject.id = preclinicalSubject.id;
            this.preclinicalSubject.animalSubject = preclinicalSubject.animalSubject;
            this.preclinicalSubject.subject = preclinicalSubject.subject;
            //Then add pathologies
            // Create therapies and pathologies from breadcrumb cache
            let entity: any;
            this.breadcrumbsService.currentStep.getPrefilledValue(this.therapiesComponent.getEntityName() + "ToCreate").then(res => entity = res);
            if (entity) {
                for (let therapy of entity) {
                    this.subjectTherapyService.createSubjectTherapy(this.preclinicalSubject, therapy);
                }
            } else if (this.preclinicalSubject && this.preclinicalSubject.therapies) {
                for (let therapy of this.preclinicalSubject.therapies) {
                    this.subjectTherapyService.createSubjectTherapy(this.preclinicalSubject, therapy);
                }
            }
            this.breadcrumbsService.currentStep.getPrefilledValue(this.pathologiesComponent.getEntityName() + "ToCreate").then(res => entity = res);
            if (entity) {
                for (let pathology of entity) {
                    this.subjectPathologyService.createSubjectPathology(this.preclinicalSubject, pathology);
                }
            } else if (this.preclinicalSubject && this.preclinicalSubject.pathologies) {
                for (let pathology of this.preclinicalSubject.pathologies) {
                    this.subjectPathologyService.createSubjectPathology(this.preclinicalSubject, pathology);
                }
            }
            return this.preclinicalSubject;
        }, this.catchSavingErrors);
    }

    updateSubject(): Promise<AnimalSubject> {

        if (!(this.preclinicalSubject && this.preclinicalSubject.subject)) {
            return;
        }

        this.generateSubjectIdentifier();
        this.preclinicalSubject.subject.subjectStudyList = this.subjectStudyList;
        return this.subjectService.update(this.preclinicalSubject.id, this.preclinicalSubject.subject)
            .then(subject => {
                let entity:any;
                if (this.preclinicalSubject.animalSubject) {
                    this.animalSubjectService.updateAnimalSubject(this.preclinicalSubject.animalSubject).catch(this.catchSavingErrors);
                }
                // Create, Update, Delete therapies and pathologies from breadcrumb cache
                if (this.breadcrumbsService.currentStep.isPrefilled(this.therapiesComponent.getEntityName() + "ToCreate")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue(this.therapiesComponent.getEntityName() + "ToCreate").then(res => entity = res);
                    for (let therapy of entity) {
                        this.subjectTherapyService.createSubjectTherapy(this.preclinicalSubject, therapy);
                    }
                }
                if (this.breadcrumbsService.currentStep.isPrefilled(this.therapiesComponent.getEntityName() + "ToUpdate")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue(this.therapiesComponent.getEntityName() + "ToUpdate").then(res => entity = res);
                    for (let therapy of entity) {
                        this.subjectTherapyService.updateSubjectTherapy(this.preclinicalSubject, therapy);
                    }
                }
                if (this.breadcrumbsService.currentStep.isPrefilled(this.therapiesComponent.getEntityName() + "ToDelete")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue(this.therapiesComponent.getEntityName() + "ToDelete").then(res => entity = res);
                    for (let therapy of entity) {
                        this.subjectTherapyService.deleteSubjectTherapy(this.preclinicalSubject, therapy);
                    }
                }
                if (this.breadcrumbsService.currentStep.isPrefilled(this.pathologiesComponent.getEntityName() + "ToCreate")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue(this.pathologiesComponent.getEntityName() + "ToCreate").then(res => entity = res);
                    for (let pathology of entity) {
                        this.subjectPathologyService.createSubjectPathology(this.preclinicalSubject, pathology);
                    }
                }
                if (this.breadcrumbsService.currentStep.isPrefilled(this.pathologiesComponent.getEntityName() + "ToUpdate")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue(this.pathologiesComponent.getEntityName() + "ToUpdate").then(res => entity = res);
                    for (let pathology of entity) {
                        this.subjectPathologyService.updateSubjectPathology(this.preclinicalSubject, pathology);
                    }
                }
                if (this.breadcrumbsService.currentStep.isPrefilled(this.pathologiesComponent.getEntityName() + "ToDelete")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue(this.pathologiesComponent.getEntityName() + "ToDelete").then(res => entity = res);
                    for (let pathology of entity) {
                        this.subjectPathologyService.deleteSubjectPathology(this.preclinicalSubject, pathology);
                    }
                }
                return this.preclinicalSubject.animalSubject;
            }).catch(this.catchSavingErrors);
    }

    sortReferences() {
    if (this.references){
        let speciesToSet: Reference[] = [];
        let biotypesToSet: Reference[] = [];
        let strainsToSet: Reference[] = [];
        let providersToSet: Reference[] = [];
        let stabulationsToSet: Reference[] = [];

        for (let ref of this.references) {
            switch (ref.reftype) {
                case PreclinicalUtils.PRECLINICAL_SUBJECT_SPECIE:
                    speciesToSet.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_BIOTYPE:
                    biotypesToSet.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_STRAIN:
                    strainsToSet.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_PROVIDER:
                    providersToSet.push(ref);
                    break;
                case PreclinicalUtils.PRECLINICAL_SUBJECT_STABULATION:
                    stabulationsToSet.push(ref);
                    break;
                default:
                    break;
            }
        }
        this.species = speciesToSet;
        this.biotypes = biotypesToSet;
        this.strains = strainsToSet;
        this.providers = providersToSet;
        this.stabulations = stabulationsToSet;
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

    generateSubjectIdentifier(): string {
        let hash;
        if (this.preclinicalSubject && this.preclinicalSubject.subject) {
            hash = this.preclinicalSubject.subject.name
        }
        return this.getHash(hash);
    }

    getHash(stringToBeHashed: string): string {
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
    }

    ngDoCheck() {
        const change = this.differ.diff(this);
        if (change) {
          change.forEachChangedItem(item => {
            if (item.key=="entity") {
                this.updateStudiesList();
            }
          });
        }
    }

    updateStudiesList(){
        if (this.preclinicalSubject && this.preclinicalSubject.subject
            && this.preclinicalSubject.subject.subjectStudyList
            && this.preclinicalSubject.subject.subjectStudyList.length > 0){
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

    public validateForm(eventName: string) {
        if (["create", "delete"].indexOf(eventName) != -1) {
           this.form.get("therapies").updateValueAndValidity({onlySelf: false, emitEvent: true});
           this.form.get("pathologies").updateValueAndValidity({onlySelf: false, emitEvent: true});
           this.footerState.valid = this.form.valid;
        }
    }

    onSubjectNodeInit(node: ReverseSubjectNode) {
        this.breadcrumbsService.currentStep.data.subjectNode = node;
    }

    deleteBySubject(subject: Subject) {
        this.subjectService.deleteWithConfirmDialog("preclinical-subject", subject).then(deleted => {
            if(deleted){
                this.goToList();
            }
        });
    }

    download() {
        // TODO : select study
        this.downloadService.downloadAllByStudyIdAndSubjectId(this.treeService.study.id, this.preclinicalSubject.subject.id, this.downloadState);
    }
}
