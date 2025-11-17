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
import { DatePipe } from '@angular/common';

import { Reference } from '../../../../preclinical/reference/shared/reference.model';
import { ReferenceService } from '../../../../preclinical/reference/shared/reference.service';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Pathology } from '../../pathology/shared/pathology.model';
import { PathologyService } from '../../pathology/shared/pathology.service';
import { PathologyModel } from '../../pathologyModel/shared/pathologyModel.model';
import { PathologyModelService } from '../../pathologyModel/shared/pathologyModel.service';
import { SubjectPathology } from '../shared/subjectPathology.model';
import { FormFooterComponent } from '../../../../shared/components/form-footer/form-footer.component';
import { SelectBoxComponent } from '../../../../shared/select/select.component';
import { DatepickerComponent } from '../../../../shared/date-picker/date-picker.component';

@Component({
    selector: 'subject-pathology',
    templateUrl: 'subject-pathology.component.html',
    imports: [FormsModule, ReactiveFormsModule, FormFooterComponent, SelectBoxComponent, DatepickerComponent, DatePipe]
})
export class SubjectPathologyComponent extends EntityComponent<SubjectPathology> {

    private allModels: PathologyModel[] = [];
    protected displayedModels: PathologyModel[] = []; 
    protected locations: Reference[] = [];
    protected pathologies: Pathology[] = [];

    constructor(
            private route: ActivatedRoute,
            private modelService: PathologyModelService,
            private referenceService: ReferenceService,
            private pathologyService: PathologyService) {
        super(route, 'subject-pathology');
    }

    get subjectPathology(): SubjectPathology { return this.entity; }
    set subjectPathology(subjectPathology: SubjectPathology) { this.entity = subjectPathology; }

    getService(): EntityService<SubjectPathology> {
        return null; // Not used because we override saveEntity
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.loadPathologies();
        this.loadModels();
        this.loadReferences();
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new SubjectPathology();
        this.loadPathologies();
        this.loadModels();
        this.loadReferences();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'pathologyModel': [this.subjectPathology.pathologyModel, Validators.required],
            'location': [this.subjectPathology.location, Validators.required],
            'startDate': [this.subjectPathology.startDate],
            'endDate': [this.subjectPathology.endDate]
        });
    }

    save(): Promise<SubjectPathology> {
        // Instead of saving the entity, we will emit it to the previous step
        if (this.breadcrumbsService.previousStep != null) {
            this.breadcrumbsService.previousStep.addPrefilled('newSubjectPathology', this.subjectPathology);
            this.goBack();
            return Promise.resolve(this.subjectPathology);
        }
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    protected goToAddPathologyModel() {
        this.navigateToAttributeCreateStep('/pathology-model/create', 'pathologyModel');
    }

    protected goToAddLocation() {
        this.navigateToAttributeCreateStep('/location/create', 'location');
    }

    protected loadPathologies() {
        this.pathologyService.getAll().then(pathologies => this.pathologies = pathologies);
    }

    protected loadModels() {
        this.modelService.getAll().then(models => this.allModels = models);
    }

    protected loadReferences() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_ANATOMY, PreclinicalUtils.PRECLINICAL_ANATOMY_LOCATION).then(locations => this.locations = locations);
    }

    protected refreshModelsByPathology(pathology: Pathology) {
        if (pathology) {
            this.displayedModels = this.allModels.filter(model => model.pathology?.id === pathology.id);
            this.subjectPathology.pathologyModel = this.displayedModels?.[0];
        } else {
            this.displayedModels = [];
        }
        this.form.updateValueAndValidity();
    }

}
