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
import { AbstractControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { IdName } from '../../shared/models/id-name.model';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { Center } from '../../centers/shared/center.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ManufacturerModelPipe } from '../shared/manufacturer-model.pipe';

@Component({
    selector: 'acquisition-equipment-detail',
    templateUrl: 'acquisition-equipment.component.html'
})

export class AcquisitionEquipmentComponent extends EntityComponent<AcquisitionEquipment> {

    manufModels: ManufacturerModel[];
    centers: IdName[];
    datasetModalityTypeStr: string;
    private nonEditableCenter: boolean = false;
    private lastSubmittedManufAndSerial: ManufacturerAndSerial;

    get acqEquip(): AcquisitionEquipment { return this.entity; }
    set acqEquip(acqEquip: AcquisitionEquipment) { this.entity = acqEquip; }
    
    constructor(
            private route: ActivatedRoute, 
            private acqEquipService: AcquisitionEquipmentService, 
            private manufModelService: ManufacturerModelService,
            private centerService: CenterService,
            public manufacturerModelPipe: ManufacturerModelPipe) {

        super(route, 'acquisition-equipment');
    }

    getService(): EntityService<AcquisitionEquipment> {
        return this.acqEquipService;
    }

    async initView(): Promise<void> {
        this.acqEquip = await this.acqEquipService.get(this.id);
        this.updateAcquEq();
    }

    initEdit(): Promise<void> {
        this.getManufModels();
        return Promise.all([
            this.centerService.getCentersNames(),
            this.acqEquipService.get(this.id)
        ]).then(([centers, ae]) => {
            this.centers = centers;
            this.acqEquip = ae;
            this.updateAcquEq();
        });
    }

    async initCreate(): Promise<void> {
        this.entity = new AcquisitionEquipment();
        this.centerService.getCentersNames().then(centers => this.centers = centers);
        this.getManufModels();
    }

    private prefill() {
        this.nonEditableCenter = this.breadcrumbsService.currentStep.isPrefilled('center');
        if (this.nonEditableCenter) {
            this.acqEquip.center = this.breadcrumbsService.currentStep.getPrefilledValue('center');
        } else if (this.acqEquip.center) {
            // Clean center
            let centerSelected: Center = new Center();
            centerSelected.id = this.acqEquip.center.id;
            centerSelected.name = this.acqEquip.center.name;
            this.acqEquip.center = centerSelected;
        }
    }

    private updateAcquEq(): void {
        DatasetModalityType[this.acqEquip.manufacturerModel.datasetModalityType];
        let mod = DatasetModalityType.all().find(dsMod => dsMod.toString() == this.acqEquip.manufacturerModel.datasetModalityType);
        if (mod) this.datasetModalityTypeStr = DatasetModalityType.getLabel(mod);
    }

    buildForm(): FormGroup {
        this.prefill();
        let form: FormGroup = this.formBuilder.group({
            'serialNumber': [this.acqEquip.serialNumber, [this.manufAndSerialUnicityValidator, this.noSpacesStartAndEndValidator]],
            'manufacturerModel': [this.acqEquip.manufacturerModel, [Validators.required]],
            'center': [{value: this.acqEquip.center, disabled: this.nonEditableCenter}, Validators.required], 
        });
        this.registerManufAndSerialUnicityValidator(form);
        return form;
    }

    private getManufModels(manufModelId?: number): void {
        this.manufModelService.getAll()
            .then(manufModels => this.manufModels = manufModels);
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    openNewManufModel() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/manufacturer-model/create']).then(success => {
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    (currentStep.entity as AcquisitionEquipment).manufacturerModel = entity as ManufacturerModel;
                })
            );
        });
    }

    private registerManufAndSerialUnicityValidator(form: FormGroup) {
        this.onSubmitValidatedFields.push('serialNumber');
        this.subscribtions.push(
            form.get('manufacturerModel').valueChanges.subscribe(value => {
                form.get('serialNumber').updateValueAndValidity();
            })
        );
    }

    private manufAndSerialUnicityValidator = (control: AbstractControl): ValidationErrors | null => {
        if (this.saveError && this.saveError.hasFieldError('manufacturerModel - serialNumber', 'unique')
                && this.acqEquip.manufacturerModel.id == this.lastSubmittedManufAndSerial.manuf.id
                && this.acqEquip.serialNumber == this.lastSubmittedManufAndSerial.serial) {       
            return {unique: true};
        }
        return null;
    }

    private noSpacesStartAndEndValidator = (control: AbstractControl): ValidationErrors | null => {
        let valueStr: string = control.value;
        if (valueStr && (valueStr.startsWith(' ') || valueStr.endsWith(' '))) {
            return { spaces: true }
        }
        return null;
    }

    save(): Promise<void> {
        this.lastSubmittedManufAndSerial = new ManufacturerAndSerial(this.acqEquip.manufacturerModel, this.acqEquip.serialNumber);
        return super.save();
    }

    viewCenter(center: Center) {
        this.router.navigate(['center/details/' + center.id]);
    }
}

export class ManufacturerAndSerial {
    constructor(
        public manuf: ManufacturerModel,
        public serial: string
    ) {}
}

