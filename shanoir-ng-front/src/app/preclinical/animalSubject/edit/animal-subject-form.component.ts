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
import { Component, Input, KeyValueDiffer, KeyValueDiffers } from '@angular/core';
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import shajs from 'sha.js';

import { TaskState } from 'src/app/async-tasks/task.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { StudyRightsService } from 'src/app/studies/shared/study-rights.service';
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';
import { Selection } from 'src/app/studies/study/tree.service';

import { preventInitialChildAnimations, slideDown } from '../../../shared/animations/animations';
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { Option, SelectBoxComponent } from '../../../shared/select/select.component';
import { Study } from '../../../studies/shared/study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { ImagedObjectCategory } from '../../../subjects/shared/imaged-object-category.enum';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from '../../../subjects/shared/subject.service';
import { ReverseSubjectNode } from '../../../tree/tree.model';
import { isDarkColor } from "../../../utils/app.utils";
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectPathologyService } from '../../pathologies/subjectPathology/shared/subjectPathology.service';
import { Reference } from '../../reference/shared/reference.model';
import { ReferenceService } from '../../reference/shared/reference.service';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';
import { SubjectTherapyService } from '../../therapies/subjectTherapy/shared/subjectTherapy.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { AnimalSubject } from '../shared/animalSubject.model';
import { AnimalSubjectService } from '../shared/animalSubject.service';
import { PreclinicalSubject } from '../shared/preclinicalSubject.model';
import { NgClass } from '@angular/common';
import { FormFooterComponent } from '../../../shared/components/form-footer/form-footer.component';
import { TagInputComponent } from '../../../tags/tag.input.component';
import { CheckboxComponent } from '../../../shared/checkbox/checkbox.component';
import { SubjectPathologiesListComponent } from '../../pathologies/subjectPathology/list/subject-pathology-list.component';
import { SubjectTherapyListComponent } from '../../therapies/subjectTherapy/list/subject-therapy-list.component';


@Component({
    selector: 'animal-subject-form',
    templateUrl: 'animal-subject-form.component.html',
    styleUrls: ['../../../subjects/subject/subject.component.css', 'animal-subject-form.component.css'],
    animations: [slideDown, preventInitialChildAnimations],
    imports: [FormsModule, ReactiveFormsModule, NgClass, FormFooterComponent, SelectBoxComponent, TagInputComponent, CheckboxComponent, SubjectPathologiesListComponent, SubjectTherapyListComponent]
})

export class AnimalSubjectFormComponent extends EntityComponent<PreclinicalSubject> {

    public readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    private nameValidators = [Validators.required, Validators.minLength(2), Validators.maxLength(64)];
    species: Reference[] = [];
    strains: Reference[] = [];
    biotypes: Reference[] = [];
    providers: Reference[] = [];
    stabulations: Reference[] = [];
    references: Reference[] = [];
    hasDownloadRight: boolean = false;
    downloadState: TaskState = new TaskState();

    @Input() displayPathologyTherapy: boolean = true;
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

    subjectTypes: Option<string>[] = [
        new Option<string>('HEALTHY_VOLUNTEER', 'Healthy Volunteer'),
        new Option<string>('PATIENT', 'Patient'),
        new Option<string>('PHANTOM', 'Phantom')
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

    public get preclinicalSubject(): PreclinicalSubject { return this.entity; }
    public set preclinicalSubject(preclinicalSubject: PreclinicalSubject) { this.entity = preclinicalSubject; }

    getService(): EntityService<PreclinicalSubject> {
        return this.animalSubjectService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromPreclinicalSubject(this.entity);
    }

    protected fetchEntity: () => Promise<PreclinicalSubject> = () => {
        return this.idPromise.then(id => {
            return this.getService().get(id).then(ps => {
                return Promise.all([
                    this.animalSubjectService.getAnimalSubject(ps.id),
                    this.subjectService.get(ps.id)
                ]).then(([animalSubject, subject]) => {
                    ps.animalSubject = animalSubject;
                    ps.subject = subject;
                    return ps;
                });
            });
        });
    }

    initView(): Promise<void> {
        this.studyService.get(this.preclinicalSubject.subject?.study?.id).then(study => {
            this.preclinicalSubject.subject.study = study;
        });
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
        this.studyService.get(this.preclinicalSubject.subject?.study?.id).then(study => {
            this.preclinicalSubject.subject.study = study;
        });
        return this.loadData().then(() => {
            this.preclinicalSubject.animalSubject.specie = this.getReferenceById(this.preclinicalSubject.animalSubject.specie);
            this.preclinicalSubject.animalSubject.strain = this.getReferenceById(this.preclinicalSubject.animalSubject.strain);
            this.preclinicalSubject.animalSubject.biotype = this.getReferenceById(this.preclinicalSubject.animalSubject.biotype);
            this.preclinicalSubject.animalSubject.provider = this.getReferenceById(this.preclinicalSubject.animalSubject.provider);
            this.preclinicalSubject.animalSubject.stabulation = this.getReferenceById(this.preclinicalSubject.animalSubject.stabulation);
        });
    }

    initCreate(): Promise<void> {
        return this.loadData().then(() => {
            this.preclinicalSubject = new PreclinicalSubject();
            this.preclinicalSubject.animalSubject = new AnimalSubject();
            this.preclinicalSubject.subject = new Subject();
            this.preclinicalSubject.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_ANIMAL;
        });
    }

    loadData(): Promise<void> {
        return Promise.all([
            this.referenceService.getReferencesByCategory(PreclinicalUtils.PRECLINICAL_CAT_SUBJECT).then(references => {
                this.references = references;
                this.sortReferences();
            }),
        ]).then();
    }

	copySubject(s: Subject): Subject{
		const subject = new Subject();
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
        const animal: boolean = this.animalSelected();
        // Sub-group for subject properties
        const subjectGroup = this.formBuilder.group({
            'imagedObjectCategory': [this.preclinicalSubject.subject?.imagedObjectCategory, [Validators.required]],
            'isAlreadyAnonymized': [this.preclinicalSubject.subject?.isAlreadyAnonymized],
            'name': [this.preclinicalSubject.subject?.name, this.nameValidators.concat([this.registerOnSubmitValidator('unique', 'subject.name')])],
            'sex': [this.preclinicalSubject.subject?.sex, animal ? [Validators.required] : []],
            'studyIdentifier': [this.preclinicalSubject.subject.identifier],
            'physicallyInvolved': [this.preclinicalSubject.subject.physicallyInvolved],
            'tags': [this.preclinicalSubject.subject.tags],
            'subjectType': [this.preclinicalSubject.subject.subjectType], 
        });
        // Sub-group for animal subject properties
        const animalSubjectGroup = this.formBuilder.group({
            'specie': [this.preclinicalSubject.animalSubject?.specie, animal ? [Validators.required] : []],
            'strain': [this.preclinicalSubject.animalSubject?.strain, animal ? [Validators.required] : []],
            'biotype': [this.preclinicalSubject.animalSubject?.biotype, animal ? [Validators.required] : []],
            'provider': [this.preclinicalSubject.animalSubject?.provider, animal ? [Validators.required] : []],
            'stabulation': [this.preclinicalSubject.animalSubject?.stabulation, animal ? [Validators.required] : []]
        });
        // Main form with sub-groups
        const subjectForm = this.formBuilder.group({
            'subject': subjectGroup,
            'animalSubject': animalSubjectGroup,
            'therapies': [this.preclinicalSubject.therapies],
            'pathologies': [this.preclinicalSubject.pathologies]
        });
        // Subscribe to category changes to update validators
        this.subscriptions.push(
            subjectGroup.get('imagedObjectCategory').valueChanges.subscribe(() => {
                this.onChangeImagedObjectCategory(subjectForm);
            })
        );
        return subjectForm;
    }

    protected prefillProperties(): void {
        super.prefillProperties();
        this.breadcrumbsService.currentStep.getPrefilledValue('newSubjectPathology').then((newSubjectPathology: SubjectPathology) => {
            this.breadcrumbsService.currentStep.removePrefilled('newSubjectPathology');
            if (newSubjectPathology) {
                if (!this.preclinicalSubject.pathologies) {
                    this.preclinicalSubject.pathologies = [newSubjectPathology];
                } else {
                    this.preclinicalSubject.pathologies.push(newSubjectPathology);
                    this.preclinicalSubject.pathologies = this.preclinicalSubject.pathologies.slice(); // Force array update
                }
            }
        });
        this.breadcrumbsService.currentStep.getPrefilledValue('newSubjectTherapy').then((newSubjectTherapy: SubjectTherapy) => {
            this.breadcrumbsService.currentStep.removePrefilled('newSubjectTherapy');
            if (newSubjectTherapy) {
                if (!this.preclinicalSubject.therapies) {
                    this.preclinicalSubject.therapies = [newSubjectTherapy];
                } else {
                    this.preclinicalSubject.therapies.push(newSubjectTherapy);
                    this.preclinicalSubject.therapies = this.preclinicalSubject.therapies.slice(); // Force array update
                }
            }
        });
        this.breadcrumbsService.currentStep.getPrefilledValue('forceStudy').then((forceStudy: Study) => {
            if (forceStudy) {
                this.preclinicalSubject.subject.study = forceStudy;
            }
        });
    }
    
    onChangeImagedObjectCategory(formGroup: UntypedFormGroup){
        const subjectGroup = formGroup.get('subject') as UntypedFormGroup;
        const animalSubjectGroup = formGroup.get('animalSubject') as UntypedFormGroup;
        
        const newCategory: ImagedObjectCategory = subjectGroup.get('imagedObjectCategory').value;
        const animalSubjectRequiredProperties: string[] = ['specie', 'strain', 'biotype', 'provider', 'stabulation'];
        const subjectRequiredProperties: string[] = ['sex'];
        
        // Update validators based on category selection
        if (newCategory != 'PHANTOM' && newCategory != 'ANATOMICAL_PIECE' && this.mode != 'view') {
            // Animal categories require all animal fields
            animalSubjectRequiredProperties.forEach(prop => {
                animalSubjectGroup.get(prop).setValidators([Validators.required]);
            });
            subjectRequiredProperties.forEach(prop => {
                subjectGroup.get(prop).setValidators([Validators.required]);
            });
        } else {
            // Non-animal categories don't require animal fields
            animalSubjectRequiredProperties.forEach(prop => {
                animalSubjectGroup.get(prop).clearValidators();
                animalSubjectGroup.get(prop).setValue(null);
            });
            subjectRequiredProperties.forEach(prop => {
                subjectGroup.get(prop).clearValidators();
                subjectGroup.get(prop).setValue(null);
            });
        }
        animalSubjectRequiredProperties.forEach(prop => {
            animalSubjectGroup.get(prop).updateValueAndValidity();
        });
        subjectRequiredProperties.forEach(prop => {
            subjectGroup.get(prop).updateValueAndValidity();
        });
        this.reloadRequiredStyles();
    }

    goToRefPage(category: string, reftype: string): void {
        this.navigateToAttributeCreateStep('/preclinical-reference/create', 'animalSubject.' + reftype, null, { queryParams: {category: category, reftype: reftype} });
    }

    goToEdit(): void {
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
        } else {
            this.preclinicalSubject.subject = { ...this.preclinicalSubject.subject, study: { id: this.preclinicalSubject.subject.study.id } as Study };
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
            this.preclinicalSubject.subject.id = preclinicalSubject.subject.id;
            return this.preclinicalSubject;
        }, this.catchSavingErrors);
    }

    updateSubject(): Promise<AnimalSubject> {
        if (!(this.preclinicalSubject && this.preclinicalSubject.subject)) {
            return;
        }
        return this.subjectService.update(this.preclinicalSubject.id, this.preclinicalSubject.subject)
            .then(() => {
                if (this.preclinicalSubject.animalSubject) {
                    this.animalSubjectService.updateAnimalSubject(this.preclinicalSubject.animalSubject).catch(this.catchSavingErrors);
                }
                return this.preclinicalSubject.animalSubject;
            }).catch(this.catchSavingErrors);
    }

    sortReferences() {
        if (this.references){
            const speciesToSet: Reference[] = [];
            const biotypesToSet: Reference[] = [];
            const strainsToSet: Reference[] = [];
            const providersToSet: Reference[] = [];
            const stabulationsToSet: Reference[] = [];

            for (const ref of this.references) {
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
            for (const ref of this.references) {
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
        const hash = shajs('sha').update(stringToBeHashed).digest('hex');
        const hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
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
        this.downloadService.downloadAllByStudyIdAndSubjectId(this.treeService.study.id, this.preclinicalSubject.subject.id, this.downloadState);
    }

    getFontColor(colorInp: string): boolean {
        return isDarkColor(colorInp);
    }

    onIdentifierChange() {
        this.form.get('studyIdentifier').markAsDirty();
        this.form.get('studyIdentifier').updateValueAndValidity();
    }
}
