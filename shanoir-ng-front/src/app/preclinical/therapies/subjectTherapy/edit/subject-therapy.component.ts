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
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Reference } from 'src/app/preclinical/reference/shared/reference.model';
import { ReferenceService } from 'src/app/preclinical/reference/shared/reference.service';
import { Frequency } from 'src/app/preclinical/shared/enum/frequency';

import { slideDown } from '../../../../shared/animations/animations';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { Therapy } from '../../therapy/shared/therapy.model';
import { TherapyService } from '../../therapy/shared/therapy.service';
import { SubjectTherapy } from '../shared/subjectTherapy.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { DatePipe } from '@angular/common';
import { FormFooterComponent } from '../../../../shared/components/form-footer/form-footer.component';
import { SelectBoxComponent } from '../../../../shared/select/select.component';
import { DatepickerComponent } from '../../../../shared/date-picker/date-picker.component';

@Component({
    selector: 'subject-pathology',
    templateUrl: 'subject-therapy.component.html',
    animations: [slideDown],
    imports: [FormsModule, ReactiveFormsModule, FormFooterComponent, SelectBoxComponent, DatepickerComponent, DatePipe]
})
export class SubjectTherapyComponent extends EntityComponent<SubjectTherapy> {

    protected therapies: Therapy[] = [];
    protected units: Reference[] = [];
    protected frequencies: Frequency[] = [];

    constructor(
            private route: ActivatedRoute,
            private therapyService: TherapyService,
            private referenceService: ReferenceService) {
        super(route, 'subject-pathology');
    }

    get subjectTherapy(): SubjectTherapy { return this.entity; }
    set subjectTherapy(subjectTherapy: SubjectTherapy) { this.entity = subjectTherapy; }

    getService(): EntityService<SubjectTherapy> {
        return null;
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.loadTherapies();
        this.loadUnits();
        this.loadFrequencies();
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.loadTherapies();
        this.loadUnits();
        this.loadFrequencies();
        this.entity = new SubjectTherapy();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'id': [this.subjectTherapy.id],
            'therapy': [this.subjectTherapy.therapy, Validators.required],
            'dose': [this.subjectTherapy.dose],
            'doseUnit': [this.subjectTherapy.doseUnit],
            'frequency': [this.subjectTherapy.frequency],
            'startDate': [this.subjectTherapy.startDate],
            'molecule': [this.subjectTherapy.molecule],
            'endDate': [this.subjectTherapy.endDate]
        });
    }

    save(): Promise<SubjectTherapy> {
        // Instead of saving the entity, we will emit it to the previous step
        if (this.breadcrumbsService.previousStep != null) {
            this.breadcrumbsService.previousStep.addPrefilled('newSubjectTherapy', this.subjectTherapy);
            this.goBack();
            return Promise.resolve(this.subjectTherapy);
        }
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    loadTherapies() {
        this.therapyService.getAll().then(therapies => this.therapies = therapies);
    }
    
    loadUnits() {
        this.referenceService.getReferencesByCategory(PreclinicalUtils.PRECLINICAL_CAT_UNIT).then(units => this.units = units);
    }

    loadFrequencies(): void {
        this.frequencies = Frequency.all();
    }

    protected goToAddTherapy() {
        this.navigateToAttributeCreateStep('/therapy/create', 'therapy');
    }

}
