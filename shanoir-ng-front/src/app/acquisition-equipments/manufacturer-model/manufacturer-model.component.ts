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
import { UntypedFormGroup, Validators, ValidatorFn } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { Manufacturer } from '../shared/manufacturer.model';
import { ManufacturerService } from '../shared/manufacturer.service';
import { Option } from '../../shared/select/select.component';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import {UnitOfMeasure} from "../../enum/unitofmeasure.enum";

@Component({
    selector: 'manufacturer-model-detail',
    templateUrl: 'manufacturer-model.component.html',
    standalone: false
})

export class ManufacturerModelComponent extends EntityComponent<ManufacturerModel> {

    manufs: Manufacturer[];
    datasetModalityTypes: Option<DatasetModalityType>[];

    constructor(
            private route: ActivatedRoute,
            private manufModelService: ManufacturerModelService,
            private manufService: ManufacturerService) {

        super(route, 'manufacturer-model');
        this.datasetModalityTypes = DatasetModalityType.options;
    }

    get manufModel(): ManufacturerModel { return this.entity; }
    set manufModel(manufModel: ManufacturerModel) { this.entity = manufModel; }

    getService(): EntityService<ManufacturerModel> {
        return this.manufModelService;
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.manufModel.manufacturer = this.getManufById(this.manufModel.manufacturer.id);
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.getManufs();
        this.entity = new ManufacturerModel();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.manufModel.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]],
            'manufacturer': [this.manufModel.manufacturer, Validators.required],
            'magneticField': [this.manufModel.magneticField, this.getMagneticFieldValidators()],
            'datasetModalityType': [this.manufModel.datasetModalityType, Validators.required]
        });
    }

    private getMagneticFieldValidators(): ValidatorFn | ValidatorFn[] {
        if (this.isMR) return Validators.required;
        else return;
    }

    onModalityChange(modality: string) {
        if (modality) {
            this.form.get('magneticField').setValidators(this.getMagneticFieldValidators());
            this.reloadRequiredStyles();
        }
    }

    public get isMR(): boolean {
        return this.manufModel && this.manufModel.datasetModalityType == DatasetModalityType.MR;
    }

    private getManufacturerModel(): Promise<void> {
        return this.manufModelService.get(this.id)
            .then(manufModel => {
                this.manufModel = manufModel;
            });
    }

    private getManufs(): Promise<void> {
        return this.manufService.getAll()
            .then(manufs => {
                this.manufs = manufs;
            });
    }

    private getManufById(id: number): Manufacturer {
        for (let manuf of this.manufs) {
            if (id == manuf.id) {
                return manuf;
            }
        }
        return null;
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    openNewManuf() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/manufacturer/create']).then(() => {
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    this.entity.manufacturer = entity as Manufacturer;
                })
            );
        });
    }

    getUnit(key: string) {
        return UnitOfMeasure.getLabelByKey(key);
    }

}
