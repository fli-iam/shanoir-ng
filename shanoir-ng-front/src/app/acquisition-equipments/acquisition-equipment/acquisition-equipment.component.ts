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
import {AbstractControl, AsyncValidatorFn, UntypedFormGroup, ValidationErrors, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Selection } from 'src/app/studies/study/tree.service';

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
import { ManufacturerModelPipe } from '../shared/manufacturer-model.pipe';
import { Selection } from 'src/app/studies/study/tree.service';
import {of, timer} from "rxjs";
import {catchError, map, switchMap} from "rxjs/operators";

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
    private nonEditableCenter: boolean = false;
    private lastSubmittedManufAndSerial: ManufacturerAndSerial;
    fromImport: string;

    get acqEquip(): AcquisitionEquipment { return this.entity; }
    set acqEquip(acqEquip: AcquisitionEquipment) { this.entity = acqEquip; }

    constructor(
            private route: ActivatedRoute,
            private acqEquipService: AcquisitionEquipmentService,
            private manufModelService: ManufacturerModelService,
            private centerService: CenterService,
            public manufacturerModelPipe: ManufacturerModelPipe,
            protected router: Router) {

        super(route, 'acquisition-equipment');

        this.fromImport = this.router.getCurrentNavigation()?.extras?.state?.fromImport;
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

    init() {
        super.init();
        if (this.mode == 'create') {
            this.breadcrumbsService.currentStep.getPrefilledValue("center").then( res => this.acqEquip.center = res);
        }
    }

    initEdit(): Promise<void> {
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
        if (this.breadcrumbsService.currentStep.isPrefilled('sc_center')) {
            this.breadcrumbsService.currentStep.getPrefilledValue('sc_center').then(res => {
                this.centersFromStudyCard = res;
                this.nonEditableCenter = true;
            });
        }
        if (this.breadcrumbsService.currentStep.isPrefilled('center')) {
            this.breadcrumbsService.currentStep.getPrefilledValue('center').then(res => {
                this.acqEquip.center = res;
            });
        }
        // this.nonEditableCenter = this.breadcrumbsService.currentStep.isPrefilled('center');
        if (this.acqEquip.center) {
            // Clean center
            const centerSelected: Center = new Center();
            centerSelected.id = this.acqEquip.center.id;
            centerSelected.name = this.acqEquip.center.name;
            this.acqEquip.center = centerSelected;
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
        let form: UntypedFormGroup = this.formBuilder.group({
            'serialNumber': [
                this.acqEquip.serialNumber,
                {
                    validators: [this.noSpacesStartAndEndValidator],
                    asyncValidators: [this.uniqueEquipmentValidator],
                    updateOn: 'change'
                }
            ],
            'manufacturerModel': [this.acqEquip.manufacturerModel, [Validators.required]],
            'center': [{value: this.acqEquip.center, disabled: this.nonEditableCenter}, Validators.required],
        });
        this.registerManufAndSerialUnicityValidator(form);
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
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/manufacturer-model/create']).then(() => {
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    this.entity.manufacturerModel = entity as ManufacturerModel;
                })
            );
        });
    }

    private registerManufAndSerialUnicityValidator(form: UntypedFormGroup) {
        this.onSubmitValidatedFields.push('serialNumber');
        this.subscriptions.push(
            form.get('manufacturerModel').valueChanges.subscribe(() => {
                form.get('serialNumber').updateValueAndValidity();
            })
        );
    }

    private manufAndSerialUnicityValidator = (): ValidationErrors | null => {
        if (this.saveError && this.saveError.hasFieldError('manufacturerModel - serialNumber', 'unique')
                && this.acqEquip.manufacturerModel.id == this.lastSubmittedManufAndSerial.manuf.id
                && this.acqEquip.serialNumber == this.lastSubmittedManufAndSerial.serial) {
            return {unique: true};
        }
        return null;
    }

    private noSpacesStartAndEndValidator = (control: AbstractControl): ValidationErrors | null => {
        const valueStr: string = control.value;
        if (valueStr && (valueStr.startsWith(' ') || valueStr.endsWith(' '))) {
            return { spaces: true }
        }
        return null;
    }

    private uniqueEquipmentValidator: AsyncValidatorFn = (control: AbstractControl) => {
        if (!control.parent) return of(null);

        const serialNumber = control.value;
        const manufacturerModel = control.parent.get('manufacturerModel')?.value;

        if (!serialNumber || !manufacturerModel) return of(null);

        const currentId = this.acqEquip?.id;

        return timer(100).pipe(
            switchMap(() => this.acqEquipService.checkDuplicate(serialNumber, manufacturerModel, currentId)),
            map(exists => (exists ? { unique: true } : null)),
            catchError(() => of(null))
        );
    };

    save(): Promise<AcquisitionEquipment> {
        this.lastSubmittedManufAndSerial = new ManufacturerAndSerial(this.acqEquip.manufacturerModel, this.acqEquip.serialNumber);
        return super.save();
    }

    viewCenter(center: Center) {
        this.router.navigate(['center/details/' + center.id]);
    }

    onCenterChange() {
        console.log("center change");
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onSerialNumberInput(event: Event) {
        const value = (event.target as HTMLInputElement).value;
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }
}

export class ManufacturerAndSerial {
    constructor(
        public manuf: ManufacturerModel,
        public serial: string
    ) {}
}

