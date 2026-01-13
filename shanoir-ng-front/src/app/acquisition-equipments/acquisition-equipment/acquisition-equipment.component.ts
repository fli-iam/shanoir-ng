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
import { AbstractControl, AsyncValidatorFn, UntypedFormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from "rxjs";

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Selection } from 'src/app/studies/study/tree.service';

import { CenterService } from '../../centers/shared/center.service';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { IdName } from '../../shared/models/id-name.model';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { Center } from '../../centers/shared/center.model';
import { ManufacturerModelPipe } from '../shared/manufacturer-model.pipe';

@Component({
    selector: 'acquisition-equipment-detail',
    templateUrl: 'acquisition-equipment.component.html',
    standalone: false
})

export class AcquisitionEquipmentComponent extends EntityComponent<AcquisitionEquipment> {

    public manufModels: ManufacturerModel[];
    public centers: IdName[];
    public centersFromStudyCard;
    public datasetModalityTypeStr: string;
    private currentManufAndSerialAndCenter: ManufacturerAndSerialAndCenter;
    private fromImport: string;

    get acqEquip(): AcquisitionEquipment { return this.entity; }
    set acqEquip(acqEquip: AcquisitionEquipment) { this.entity = acqEquip; }

    constructor(
            private route: ActivatedRoute,
            private acqEquipService: AcquisitionEquipmentService,
            private manufModelService: ManufacturerModelService,
            private centerService: CenterService,
            public manufacturerModelPipe: ManufacturerModelPipe,
            protected router: Router) {

        super(route);

        this.fromImport = this.router.getCurrentNavigation()?.extras?.state?.fromImport;
    }

    protected getRoutingName(): string {
        return 'examination';
    }

    getService(): EntityService<AcquisitionEquipment> {
        return this.acqEquipService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromEquipment(this.acqEquip);
    }

    async initView(): Promise<void> {
        this.updateAcquEq();
    }

    initEdit(): Promise<void> {
        this.currentManufAndSerialAndCenter = new ManufacturerAndSerialAndCenter(this.acqEquip.manufacturerModel, this.acqEquip.serialNumber, this.acqEquip.center);
        this.getManufModels();
        return Promise.all([
            this.centerService.getCentersNames()
        ]).then(([centers]) => {
            this.centers = centers;
            this.updateAcquEq();
        });
    }

    initCreate(): Promise<void> {
        this.entity = new AcquisitionEquipment();
        this.prefill();
        if (this.centersFromStudyCard == null) {
            this.centerService.getCentersNames().then(centers => {
                this.centers = centers
            });
        }
        else {
            this.centers = this.centersFromStudyCard;
        }
        this.getManufModels();
        return Promise.resolve();
    }

    private prefill() {
        if (this.breadcrumbsService.currentStep.isPrefilled('centers')) {
            this.breadcrumbsService.currentStep.getPrefilledValue('centers').then(res => {
                this.centersFromStudyCard = res;
            });
        }
        if (this.fromImport) {
            this.acqEquip.serialNumber = this.fromImport.split('-')[2] != "null" ? this.fromImport.split('-')[2] : "";
        }
    }

    private updateAcquEq(): void {
        const mod = DatasetModalityType.all().find(dsMod => dsMod.toString() == this.acqEquip.manufacturerModel.datasetModalityType);
        if (mod) this.datasetModalityTypeStr = DatasetModalityType.getLabel(mod);
    }

    buildForm(): UntypedFormGroup {
        const form: UntypedFormGroup = this.formBuilder.group({
            'serialNumber': [this.acqEquip.serialNumber, [this.noSpacesStartAndEndValidator]],
            'manufacturerModel': [this.acqEquip.manufacturerModel,[Validators.required]],
            'center': [this.acqEquip.center, [Validators.required]],
        },
        {
            asyncValidators: [this.uniqueEquipmentValidator]
        });
        return form;
    }

    private getManufModels(): void {
        this.manufModelService.getAll()
            .then(manufModels => this.manufModels = manufModels);
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    openNewManufModel() {
        this.navigateToAttributeCreateStep('/manufacturer-model/create', 'manufacturerModel');
    }

    private noSpacesStartAndEndValidator = (control: AbstractControl): ValidationErrors | null => {
        const valueStr: string = control.value;
        if (valueStr && (valueStr.startsWith(' ') || valueStr.endsWith(' '))) {
            return { spaces: true }
        }
        return null;
    }

    private uniqueEquipmentValidator: AsyncValidatorFn = async (form: AbstractControl): Promise<ValidationErrors | null> => {
        if (!form) return of(null);

        const serialNumber = form.get('serialNumber')?.value as string;
        const manufacturerModel = form.get('manufacturerModel')?.value as ManufacturerModel;
        const center = form.get('center')?.value as Center;

        if (!serialNumber || !manufacturerModel || !center) return null;
        if (serialNumber == this.currentManufAndSerialAndCenter?.serial && manufacturerModel.id == this.currentManufAndSerialAndCenter?.manuf.id && center.id == this.currentManufAndSerialAndCenter?.center.id) return null;
        
        try {
            if (typeof serialNumber == 'string') {
                const exists = await this.acqEquipService.checkDuplicate(serialNumber, manufacturerModel, center);
                return exists ? {unique: true} : null;
            }
        } catch {
            return null;
        }
    }

    viewCenter(center: Center) {
        this.router.navigate(['center/details/' + center.id]);
    }
}

export class ManufacturerAndSerialAndCenter {
    constructor(
        public manuf: ManufacturerModel,
        public serial: string,
        public center: Center
    ) {}
}
